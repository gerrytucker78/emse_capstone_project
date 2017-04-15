package com.edu.utdallas.argus.cometnav;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Michelle on 3/25/2017.
 *
 * Intent service to run in the background to find BLE EddyStone beacons
 *
 */

public class BeaconManagerService extends IntentService implements BeaconConsumer{
    private static final String TAG="BeaconManagerService";
    protected static final String CometNavRegion = "CometNav"; //Specifies Eddystone region for CometNav beacons

    //Null Beacon Namespace and Beacon Instance so we see all beacons
    //Note: The namespace and beacon instance can be specified if you want to only find a specific set of beacons
    private Region region=new Region(CometNavRegion, null, null, null);
    private BeaconManager beaconManager;
    private static Set<Beacon> beaconsList=new HashSet<Beacon>();


    public BeaconManagerService(){
        super("BeaconManagerService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Start scan for beacons
        //Note: This class is initialized when the intent service is created
        onBeaconServiceConnect();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "Entered region:" + region.getId1());
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "Exited region " + region.getId1());
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.i(TAG, "Region Status: Region: " + region.getId1() + " Status: " + Integer.toString(i));
            }
        });

        try {
            beaconManager.setRegionStatePeristenceEnabled(false);
            startRanging();
            beaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    Intent localIntent = new Intent("BEACON_ACTION");
                    beaconsList.clear(); //Empty all the beacons, that way we don't list beacons that we can't see anymore
                    beaconsList.addAll(beacons);

                    List<Beacon> beaconArrayList = new ArrayList<Beacon>(beaconsList);

                    localIntent.putParcelableArrayListExtra("BEACON_LIST", (ArrayList<? extends Parcelable>) beaconArrayList);
                    //this doesn't work for some reason. Shrug.
                    //LocalBroadcastManager.getInstance(BeaconManagerService.this).sendBroadcast(localIntent);
                    sendBroadcast(localIntent);
//                    if(beaconsList.size() > 0) {
//                        for (Beacon b : beaconsList) {
//                            Log.d(TAG, "Beacon Bluetooth Address: " + b.getBluetoothAddress()
//                                    + " ID1: " + b.getId1()
//                                    + " ID2: " + b.getId2()
//                            );
//                        }
//                    }else{
//                        Log.d(TAG, "No beacons detected at this time");
//                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"Exception Error when ",e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Setup the beacon manager to see the Eddy Stone Beacons
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        beaconManager.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    /**
     * Stop Scanning
     */
    public void stopRanging(){
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            beaconManager.stopMonitoringBeaconsInRegion(region);
            Log.i(TAG, "BeaconManagerService - Ranging stoped!!");
        } catch (RemoteException e) {
            Log.e(TAG,"Error BeaconManagerService - StopRanging",e);
        }
    }

    /**
     * Start Scanning
     */
    public void startRanging(){
        try {
            Log.i(TAG, "BeaconManagerService - Ranging in progess!");
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.startMonitoringBeaconsInRegion(region);
            Log.i(TAG, "BeaconManagerService - Ranging done already... ok");
        } catch (RemoteException e) {
            Log.e(TAG,"Error BeaconManagerService - StartRanging",e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public BeaconManagerService getService() {
            return BeaconManagerService.this;
        }
    }
}
