package com.example.tiagomagalhaes.notifyoncall;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.Collection;


/**
 * Created by tiagomagalhaes on 10/02/2017.
 */

public class BeaconService extends Service implements BeaconConsumer, SensorEventListener{

    private SensorManager sensorManager;
    double ax,ay,az;   // these are the acceleration in x,y and z axis
    private static int counter;

    /** indicates how to behave if the service is killed */
    int mStartMode;

    /** interface for clients that bind */
    IBinder mBinder;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    private BeaconManager beaconManager;
    private static BeaconService beaconServiceRunningInstance;
    public static final String BEACON_UPDATE = "com.example.tiagomagalhaes.nearestbeacon";
    public static final String ACCELEROMETER_UPDATE = "com.example.tiagomagalhaes.accelerometer";




    public static BeaconService  getInstace(){
        return beaconServiceRunningInstance;
    }

    /** Called when the service is being created. */
    @Override
    public void onCreate() {
        Log.d("SERVICE","CREATED");
        beaconServiceRunningInstance= this;

        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);


        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        ax = 0.0;
        ay = 0.0;
        az = 0.0;
        counter = 0;
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return mStartMode;

    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        beaconManager.unbind(this);
        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {

    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        beaconManager.unbind(this);

    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(
                new RangeNotifier() {
                    @Override
                    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                        if(beacons.size()> 0){
                            Intent intent = new Intent(BeaconService.BEACON_UPDATE);
                            // You can also include some extra data.
                            intent.putExtra("ID", beacons.iterator().next().getIdentifiers().toString());
                            intent.putExtra("DIST", beacons.iterator().next().getDistance());
                            intent.putExtra("NR", ""+beacons.size());
                            //LocalBroadcastManager.getInstance(BeaconService.getInstace()).sendBroadcast(intent);
                            BeaconService.getInstace().sendBroadcast(intent);
                            Log.d("ID",beacons.iterator().next().getIdentifiers().toString());
                            //Log.d("SERVICE","BROADCAST_SENT");

                        }
                        else {
                            Intent intent = new Intent(BeaconService.BEACON_UPDATE);
                            intent.putExtra("NR", ""+beacons.size());
                            //LocalBroadcastManager.getInstance(BeaconService.getInstace()).sendBroadcast(intent);
                            BeaconService.getInstace().sendBroadcast(intent);

                            //Log.d("SERVICE","BROADCAST_SENT_NO_BEACON");
                        }
                    }
                }
        );

        try {
            //beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));

        } catch (RemoteException e) {    }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        counter +=1;
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            //update values
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];

            //send update
            if((counter % 3)== 0) {
                Log.d("DATA_ACC",ax + ":" + ay + ":" + az);
                counter=0;
                Intent intent = new Intent(BeaconService.ACCELEROMETER_UPDATE);
                intent.putExtra("X", ax);
                intent.putExtra("Y", ay);
                intent.putExtra("Z", az);
                BeaconService.getInstace().sendBroadcast(intent);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
