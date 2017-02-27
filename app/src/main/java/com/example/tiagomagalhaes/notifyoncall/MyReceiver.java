package com.example.tiagomagalhaes.notifyoncall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Handler;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by tiagomagalhaes on 09/02/2017.
 */

public class MyReceiver extends BroadcastReceiver {

    private static int last_state = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming = false;
    private static String beacon_id = null;
    private static double beacon_dist = Double.MAX_VALUE;
    private static int no_beacon_occurencies=0;//consecutive no beacons in range to accept it


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BeaconService.BEACON_UPDATE)) {
            //Log.d("BROADCAST_RECEIVER", intent.getAction());
            if (!intent.getStringExtra("NR").equals("0")) //if beacon in range
                updateNearestBeacon(intent.getStringExtra("ID"), intent.getDoubleExtra("DIST", Double.MAX_VALUE), intent.getStringExtra("NR"));
            else {
                updateNoBeacon();
            }
        }
        else if(intent.getAction().equals(BeaconService.ACCELEROMETER_UPDATE)){
            Log.d("BROADCAST_RECEIVER", intent.getAction());
            updateAccUI(intent.getDoubleExtra("X", Double.MAX_VALUE),intent.getDoubleExtra("Y", Double.MAX_VALUE),intent.getDoubleExtra("Z", Double.MAX_VALUE));
        }
        else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            int state = 0;
            if (stateStr != null) {
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }


                checkCallStateChanges(context, state);
            }
        }
    }

    private void updateAccUI(double x, double y, double z) {
        MainActivity instance = MainActivity.getInstace();
        if(instance != null){
            instance.updateUIAcc(x,y,z);
        }
    }

    private void updateNearestBeacon(String id, double dist, String nr) {
        //Reset no beacon occurencies
        no_beacon_occurencies=0;
        //update main_activity
        MainActivity instance = MainActivity.getInstace();
        if (instance != null) {
            if (dist <= beacon_dist) { // if distance is less, update
                beacon_dist = dist;
                beacon_id = id;
                instance.updateUI(beacon_id, beacon_dist, nr);
            } else if (id.equals(beacon_id)) { // if same id update the distance (for the cases where the distance is bigger)
                beacon_dist = dist;
                instance.updateUI(null, beacon_dist, nr);
            }
            //instance.id.setText("ID: ".concat(id));
            //instance.dist.setText("BEACON IS AT " + dist + " METERS.");
            //instance.nr_connected.setText(nr.concat(" BEACONS IN RANGE"));
        }
    }

    private void updateNoBeacon() {
        Log.d("NO_BEACON","update no beacon");
        no_beacon_occurencies +=1;
        Log.d("NO_BEACON",no_beacon_occurencies+" occurencies");
        if((no_beacon_occurencies)>7){
        beacon_id = null;
        beacon_dist = Double.MAX_VALUE;
        MainActivity instance = MainActivity.getInstace();
        if (instance != null) {
            instance.id.setText("No Beacon");
            instance.dist.setText("No Beacon");
            instance.nr_connected.setText("0");
        }
    }}


    private void checkCallStateChanges(Context context, int state) {
        if (last_state == state) {
            return;
        }
        switch (state){
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(last_state != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                }
                else
                {
                    isIncoming = true;
                    Toast.makeText(context, "CALL ANSWERED", Toast.LENGTH_SHORT).show();
                    if (beacon_id != null) {

                        sendUDPtoNetwork(context,beacon_id,1234,"192.168.1.205");
                        sendRequest();
                        Toast.makeText(context, "Video switched to nearest Device", Toast.LENGTH_SHORT).show();

                    }
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(last_state == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    //onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    //onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    sendUDPtoNetwork(context,beacon_id + ":::call ended",1235,"192.168.1.205");
                    Toast.makeText(context, "call ended", Toast.LENGTH_SHORT).show();
                }
                else{
                    //onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
/*        else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            if (last_state == TelephonyManager.CALL_STATE_RINGING) {
                Toast.makeText(context, "CALL ANSWERED", Toast.LENGTH_SHORT).show();
                if (beacon_id != null) {

                    sendUDPtoNetwork(context,beacon_id,1234,"192.168.1.205");
                    sendRequest();
                    Toast.makeText(context, "Video switched to nearest Device", Toast.LENGTH_SHORT).show();

                }
            }
        }*/

        last_state = state;//update the state
    }

    private void sendRequest() {

    }
    @SuppressWarnings("deprecation")
    private void sendUDPtoNetwork(final Context context,final String message, final int port, final String host) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
                //String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                //Log.d("UDP_ip",""+ip);
                //Log.d("UDP_server_add",""+wm.getDhcpInfo().serverAddress);
                String messageStr = message;
                try {
                    DatagramSocket s = new DatagramSocket();
                    if (!s.getBroadcast()) s.setBroadcast(true);
                    InetAddress local = InetAddress.getByName(host);
                    int msg_length = messageStr.length();
                    byte[] message = messageStr.getBytes();
                    DatagramPacket p = new DatagramPacket(message, msg_length, local, port);
                    Log.d("UDP_p_socketaddr",p.getSocketAddress().toString());
                    Log.d("UDP_p_addr",p.getAddress().toString());
                    Log.d("UDP_p_data",p.getData().toString());
                    Log.d("UDP_p_len",msg_length+"");
                    s.send(p);
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getNearestBeacon() {
        return beacon_id;
    }
}

