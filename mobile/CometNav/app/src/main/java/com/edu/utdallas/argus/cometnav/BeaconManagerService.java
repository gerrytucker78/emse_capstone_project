package com.edu.utdallas.argus.cometnav;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Michelle on 3/25/2017.
 *
 * Intent service to run in the background to find BTL EddyStone beacons
 *
 */

public class BeaconManagerService extends IntentService implements BeaconConsumer{
    private static final String TAG="BeaconManagerService";
    protected static final String CometNavRegion = "CometNav"; //Specifies Eddystone region for CometNav beacons
    private Region region=new Region(CometNavRegion, null, null, null);
    private BeaconManager beaconManager;
    private static ArrayList<Beacon> beaconsList=new ArrayList<Beacon>();


    public BeaconManagerService(){
        super("BeaconManagerService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        onBeaconServiceConnect();
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    Intent localIntent = new Intent("BEACON_ACTION");
                    beaconsList.clear();
                    beaconsList.addAll(beacons);
                    Collections.sort(beaconsList,new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon lhs, Beacon rhs) {
                            return Double.compare(lhs.getDistance(), rhs.getDistance());
                        }
                    });
                    localIntent.putParcelableArrayListExtra("BEACON_LIST",beaconsList);
                    LocalBroadcastManager.getInstance(BeaconManagerService.this).sendBroadcast(localIntent);
                }
            });

            Log.i (TAG, "BEACONS IN ARRAY: " + beaconsList.toString());
        } catch (RemoteException e) {
            Log.e(TAG,"RemoteException Error when ",e);
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
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v"));
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
        } catch (RemoteException e) {
            Log.e(TAG,"Error BeaconService - StopRanging",e);
        }
    }

    /**
     * Start Scanning
     */
    public void startRanging(){
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG,"Error BeaconService - StartRanging",e);
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
