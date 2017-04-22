package com.edu.utdallas.argus.cometnav.dataservices.beacons;

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
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Michelle on 3/25/2017.
 *
 * Intent service to run in the background to find BLE EddyStone beacons
 *
 */

public class BeaconManagerService extends IntentService implements BeaconConsumer {
    private static final String TAG="BeaconManagerService";
    protected static final String CometNavRegion = "CometNav"; //Specifies Eddystone region for CometNav beacons

    /**
     * Parameters used for qualitative analysis of beacon data
     */

    // Number of iterations before beacon list is published
    private static final int BEACON_VERIFY_ITERATIONS = 4;

    // Percentage of BEACON_VERIFY_ITERATIONS beacon was seen in before being considered for publishing
    // Note: Given that these are integers, the percentage is rounded up
    private static final double BEACON_VERIFY_THRESHOLD = .2;

    // Average Distance threshold over the BEACON_VERIFY_ITERATIONS that a beacon must be less than or equal to
    // for publishing.
    // Note: This is used in conjunction with the BEACON_VERIFY_THRESHOLD check as well
    private static final double BEACON_DISTANCE_THRESHOLD = 4;

    // Occasionally no beacons are found.  To avoid constant drop in / drop out of beacons, this
    // variable is used to ensure that a minimum number of iterations must occur in a row without
    // beacons before publishing the empty list.
    private static final int NO_BEACON_VERIFY_ITERATIONS = 5;

    // Counters to keep up with verification threshold and no beacon found threshold
    private static int beaconVerifyCount = 1;
    private static int noBeaconCount = 0;

    // Score contains that number of times a beacon is seen in the iterations

    // Sum of distances for each iteration by beacon.  This will be used after all iterations
    // are complete to determine average distance
    private static Map<Beacon, Double> beaconDistance = new HashMap<Beacon, Double>();

    // Beacon Manager Configuration
    //Null Beacon Namespace and Beacon Instance so we see all beacons
    //Note: The namespace and beacon instance can be specified if you want to only find a specific set of beacons
    private Region region=new Region(CometNavRegion, null, null, null);
    private BeaconManager beaconManager;

    // Running list of beacons that has been last broadcast

    /**
     * Default Constructor
     */
    public BeaconManagerService(){
        super("BeaconManagerService");
    }

    /**
     * Method to launch beacon service
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Start scan for beacons
        //Note: This class is initialized when the intent service is created
        onBeaconServiceConnect();
    }

    /**
     * Main Beacon Service method that contains the logic for retrieving / filtering beacon
     * information and broadcasting this to the Android application
     */
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

            beaconManager.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    //If we had beacons previously and don't this time, throw out one poll. This helps
                    //guard against erroneous full beacon loss
                    if (previousCount != 0 && beacons.size() == 0) {
                        previousCount = 0;
                        return;
                    }
                    previousCount = beacons.size();

                    List<CometNavBeacon> beaconArrayList = new ArrayList<CometNavBeacon>(thresholdBeacons(beacons));

                    Intent localIntent = new Intent("BEACON_ACTION");
                    localIntent.putParcelableArrayListExtra("BEACON_LIST", (ArrayList<? extends Parcelable>) beaconArrayList);
                    sendBroadcast(localIntent);
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"Exception Error when ",e);
        }
    }

    //Used for Averaging
    private static Map<Beacon, Integer> beaconScore = new HashMap<Beacon, Integer>();
    //Used for averaging
    private static Set<CometNavBeacon> beaconList = new HashSet<CometNavBeacon>();

    private void averageBeacons(Collection<Beacon> beacons)
    {
        Log.i(TAG,"Range Beacons in Region");
        /**
          * Simple algorithm for now to determine total # of counts we have seen the sensor
          */
        for (Beacon b : beacons) {
            Object value = beaconScore.get(b);
            int currentScore;

            if (value == null) {
                currentScore = 0;
            } else {
                currentScore = (Integer)value;
            }
            currentScore++;
            beaconScore.put(b, currentScore);

            // Build up average distance
            Object distValue = beaconDistance.get(b);
            double distTotal= 0;

            if (distValue == null) {
                distTotal = b.getDistance();
            } else {
                distTotal = distTotal + b.getDistance();
            }

            beaconDistance.put(b, distTotal);
        }

        if (beaconVerifyCount == BEACON_VERIFY_ITERATIONS) {
            Intent localIntent = new Intent("BEACON_ACTION");
            beaconList.clear(); //Empty all the beacons, that way we don't list beacons that we can't see anymore

            /**
              * Loop through beacon scores for the beacons seen and determine if they stay in
              */
            for (Beacon b : beaconScore.keySet()) {
                Integer count = beaconScore.get(b);

                if (count >= Math.round(BEACON_VERIFY_ITERATIONS * BEACON_VERIFY_THRESHOLD) ) {

                    /**
                      * Select the Beacon and use it's average distance
                      */
                    CometNavBeacon cnBeacon = new CometNavBeacon(b);
                    cnBeacon.setDistance(beaconDistance.get(b)/count);

                    beaconList.add(cnBeacon);
                    Log.i(TAG,"Beacon " + b.getId1() + " made the cut: " + count + " with an average distance of " + cnBeacon.getDistance());

                } else {
                    Log.i(TAG,"Beacon " + b.getId1()+ " DID NOT MAKE the cut: " + count + " with an average distance of " + beaconDistance.get(b)/count);
                }
            }

            beaconScore.clear();

            List<CometNavBeacon> beaconArrayList = new ArrayList<CometNavBeacon>(beaconList);

            localIntent.putParcelableArrayListExtra("BEACON_LIST", (ArrayList<? extends Parcelable>) beaconArrayList);
            sendBroadcast(localIntent);
            beaconVerifyCount = 1;
        } else {
            beaconVerifyCount++;
            Log.i(TAG, "Still checking " + beaconVerifyCount);
        }
    }

    //Used for thresholding. beaconMap contains the map of beacon name to the beacon object
    private static HashMap<Integer, CometNavBeacon> beaconMap = new HashMap<Integer, CometNavBeacon>();
    //Used for thresholding. Stores the counts of how many times a beacon has been seen in the last
    //BEACON_THRESHOLD_COUNT iterations.
    private static Map<Integer, Integer> beaconCount = new HashMap<Integer, Integer>();

    private static final int BEACON_GAIN_THRESHOLD = 3;
    private static final int BEACON_LOSS_THRESHOLD = 4;

    /**
     * The previous count of beacons. Used to throw out false scans where no beacon is returned.
     */
    private int previousCount = 0;

    private Collection<CometNavBeacon> thresholdBeacons(Collection<Beacon> beacons)
    {
        HashSet<Integer> foundBeacons = new HashSet<Integer>();
        for (Beacon b : beacons)
        {
            CometNavBeacon cnBeacon = new CometNavBeacon(b);
            //This beacon has passed the gain threshold and is considered a "found" beacon
            if (beaconMap.containsKey(cnBeacon.getName()))
            {
                //update the distance
                beaconMap.get(cnBeacon.getName()).setDistance(cnBeacon.getDistance());
                //If the beacon is in our list, increment the count
                int count = beaconCount.get(cnBeacon.getName());
                count++;
                if (count >= BEACON_LOSS_THRESHOLD)
                    count = BEACON_LOSS_THRESHOLD;
                beaconCount.put(cnBeacon.getName(), count);
                foundBeacons.add(cnBeacon.getName());
                Log.d(TAG, "1Beacon count: " + cnBeacon.getName() + " " + count + " " + cnBeacon.getDistance());
            }
            else
            {
                //If our beacon isn't in the list, see if we've seen it before.
                if (beaconCount.get(cnBeacon.getName()) != null)
                {
                    //increment the score
                    int count = beaconCount.get(cnBeacon.getName());
                    count++;
                    if (count >= BEACON_GAIN_THRESHOLD)
                        count = BEACON_GAIN_THRESHOLD;
                    Log.d(TAG, "2Beacon count: " + cnBeacon.getName() + " " + count + " " + cnBeacon.getDistance());
                    if (count == BEACON_GAIN_THRESHOLD)
                    {
                        //If we've seen this beacon threshold_count times in a row, add to beaconList
                        if (beaconMap.put(cnBeacon.getName(), cnBeacon) == null) {
                            foundBeacons.add(cnBeacon.getName());
                            Log.d(TAG, "Adding beacon: " + cnBeacon.getName());
                        }
                    }
                    beaconCount.put(cnBeacon.getName(), count);
                }
                else
                {
                    //First time seeing it, add beacon
                    beaconCount.put(cnBeacon.getName(), 1);
                    Log.d(TAG, "3Beacon count: " + cnBeacon.getName() + " " + 1 + " " + cnBeacon.getDistance());
                }
            }
        }
        HashSet<Integer> deleteBeacons = new HashSet<Integer>();
        for (CometNavBeacon cnBeacon : beaconMap.values())
        {
            if (!foundBeacons.contains(cnBeacon.getName()))
            {
                //We didn't see this beacon this time around - decrement count
                int count = beaconCount.get(cnBeacon.getName());
                count--;
                if (count <= 0)
                    count = 0;
                Log.d(TAG, "4Beacon count: " + cnBeacon.getName() + " " + count + " " + cnBeacon.getDistance());
                if (count == 0)
                {
                    //Missed THRESHOLD_COUNT times in a row - remove from list
                    deleteBeacons.add(cnBeacon.getName());
                    beaconCount.remove(cnBeacon.getName());
                    Log.d(TAG, "Removed from list: " + cnBeacon.getName());
                }
                beaconCount.put(cnBeacon.getName(), count);
            }
        }
        //Can't delete in the for loop as it throws a concurrent modification
        //exception
        for (int beaconId : deleteBeacons)
        {
            beaconMap.remove(beaconId);
        }

        return beaconMap.values();
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
        beaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
//        try {
//            beaconManager.setForegroundScanPeriod(500l); // 500 mS
//            beaconManager.setForegroundBetweenScanPeriod(0l); // 0ms
//            beaconManager.updateScanPeriods();
//        }
//        catch (RemoteException e) {
//            Log.e(TAG, "Cannot talk to service");
//        }
        //beaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        //RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000l);
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
