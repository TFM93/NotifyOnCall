package com.example.tiagomagalhaes.notifyoncall;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {//implements BeaconConsumer{

    private static MainActivity mainActivityRunningInstance;


    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 2;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 3;


    protected   TextView id;
    protected   TextView dist;
    protected   TextView nr_connected;
    protected   TextView ax;
    protected   TextView ay;
    protected   TextView az;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityRunningInstance =this;
        setContentView(R.layout.activity_main);

        checkPerm();                                        //check App permissions

        enableBluetooth();                                  //enable bluetooth


        id = (TextView)findViewById(R.id.id);
        dist = (TextView)findViewById(R.id.dist);
        nr_connected = (TextView)findViewById(R.id.nr);
        ax = (TextView)findViewById(R.id.ax);
        ay = (TextView)findViewById(R.id.ay);
        az = (TextView)findViewById(R.id.az);

        Intent serviceIntent = new Intent(this,BeaconService.class);
        startService(serviceIntent);

    }


    public static MainActivity  getInstace(){
        return mainActivityRunningInstance;
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiverFromManifest(MyReceiver.class,this);//unregister BroadcastReceiver
        super.onDestroy();
    }

    private void checkPerm() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);

            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH);

            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_ADMIN)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    unregisterReceiverFromManifest(MyReceiver.class,this);//unregister BroadcastReceiver
                    this.finishAffinity();//close app
                }
            }
            return;
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }

                    });
                    builder.show();
                    unregisterReceiverFromManifest(MyReceiver.class,this);//unregister BroadcastReceiver
                    this.finishAffinity();//close app
                }
            }
            return;
            case MY_PERMISSIONS_REQUEST_BLUETOOTH:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since bluetooth access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }

                    });
                    builder.show();
                    unregisterReceiverFromManifest(MyReceiver.class,this);//unregister BroadcastReceiver
                    this.finishAffinity();//close app
                }
            }
            return;
            case MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since bluetooth access access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }

                    });
                    builder.show();
                    unregisterReceiverFromManifest(MyReceiver.class,this);//unregister BroadcastReceiver
                    this.finishAffinity();//close app
                }
            }
            return;
        }
    }



    /**
     * Enable bluetooth
     * @return True after Bluetooth is enabled
     */
    public static boolean enableBluetooth() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            if(!bluetoothAdapter.isEnabled()){
                bluetoothAdapter.enable();
                return true;
            }
            else {
                return true;
            }
        }
        return false;
    }
    /**
     * unregister broadcast receiver
     * @param clazz
     * @param context
     */
    private void unregisterReceiverFromManifest(Class<? extends BroadcastReceiver> clazz, final Context context) {
        final ComponentName component = new ComponentName(context, clazz);
        final int status = context.getPackageManager().getComponentEnabledSetting(component);
        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
        }
    }

    public void updateUI(final String beacon_id, final double beacon_dist, final String nr) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(beacon_id != null)
                    MainActivity.this.id.setText("ID: ".concat(beacon_id));
                MainActivity.this.dist.setText("BEACON IS AT " + beacon_dist + " METERS.");
                MainActivity.this.nr_connected.setText(nr.concat(" BEACONS IN RANGE"));
            }
        });
    }

    public void updateUIAcc(final double x, final double y, final double z) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.ax.setText("X: " + x);
                MainActivity.this.ay.setText("Y: " + y);
                MainActivity.this.az.setText("Z: " + z);
            }
        });
    }
}
