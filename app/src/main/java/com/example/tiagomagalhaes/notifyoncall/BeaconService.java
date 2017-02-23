package com.example.tiagomagalhaes.notifyoncall;

import android.app.Service;
import android.content.Intent;
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

public class BeaconService extends Service implements BeaconConsumer{
    /** indicates how to behave if the service is killed */
    int mStartMode;

    /** interface for clients that bind */
    IBinder mBinder;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    private BeaconManager beaconManager;
    private static BeaconService beaconServiceRunningInstance;
    public static final String BEACON_UPDATE = "com.example.tiagomagalhaes.nearestbeacon";




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


}
