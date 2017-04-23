package com.edu.utdallas.argus.cometnav.dataservices.emergencies;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edu.utdallas.argus.cometnav.dataservices.DataServices;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gtucker on 4/15/2017.
 * Updated by Michelle
 * <p>
 * EmergencyService class is responsible polling the server periodically to see if an emergency has
 * been issued and the app needs to evacuate out.
 */

public class EmergencyService extends IntentService {
    private static final String TAG = "EmergencyService";
    private Timer emergencyTimer;
    private TimerTask timerTask;
    private final Handler handler = new Handler();
    private EmergencyClient emergencyClient;
    private List<Emergency> emergencyList = new ArrayList<Emergency>();
    private List<Location> emergencyLocations = new ArrayList<Location>();
    private Map<Integer,Integer> emergencyListIndex = new HashMap<Integer, Integer>();
    private Map<Integer,Integer> emergencyLocListIndex = new HashMap<Integer, Integer>();
    private Map<Integer,Integer> emergencyToLocIndex = new HashMap<Integer, Integer>();

    public static final String EMERGENCY_LOCATIONS = "EMERGENCY_LOCATIONS";
    public static final String EMERGENCY_LIST = "EMERGENCY_LIST";

    public EmergencyService() {
        super(TAG);
        emergencyClient = new EmergencyClient(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "Handling intent for Emergency Service.");

        //Start polling the server for emergencies
        try {
            startEmergencyTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown when pulling the server", e);
        }
    }

    private void startEmergencyTimer() {
        //set a new Timer
        emergencyTimer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first second the TimerTask will run every 5 seconds
        emergencyTimer.schedule(timerTask, 1000, 5000); //
    }

    private void stopEmergencyTimer() {
        //stop the timer, if it's not already null
        if (emergencyTimer != null) {
            emergencyTimer.cancel();
            emergencyTimer = null;
        }
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        //Calling server to get emergencies
                        DataServices.getEmergencies(emergencyClient);


                    }
                });
            }
        };
    }

    public void processUpdatedEmergencies() {
        //Get the list of emergencies
        List<Emergency> receivedEmList = emergencyClient.getEmergencies();
        List<Location> receivedEmLocations = emergencyClient.getEmergencyLocations();

        //Tmp list will contain the deltas from newEmergencies and current emergencies
        List<Emergency> newEmList = new ArrayList<Emergency>();
        List<Location> newEmLocations = new ArrayList<Location>();

        boolean allEmergenciesCleared = false;

        if ( emergencyList.isEmpty() ) {
            //Congrats, the empty list now has all the emergencies in it
            Log.d(TAG, "EmergencyList empty. Setting it and newEmList to receivedEmList");

            // Loop through individually to build up index maps for faster access in the future
            for (int i = 0; i < receivedEmList.size(); i++) {
                emergencyList.add(receivedEmList.get(i));
                emergencyListIndex.put(receivedEmList.get(i).getEmergencyId(),i);
                emergencyLocations.add(receivedEmLocations.get(i));
                emergencyLocListIndex.put(receivedEmLocations.get(i).getLocationId(),i);
            }
            newEmList.addAll(emergencyList);
            newEmLocations.addAll(emergencyLocations);
        }else if (emergencyList.equals(receivedEmList)){
            Log.d(TAG, "New emergency list equals current emergency list...");
            //Do Nothing
        }
        else if (receivedEmList.isEmpty()) {
            emergencyList.clear();
            emergencyLocations.clear();
            Log.d(TAG, "No more emergencies.  Clear the list.");
            allEmergenciesCleared = true;
        }
        else{
            //Filter based on emergencies you already have
            for (Emergency newEmergency : receivedEmList) {

                if( !emergencyList.contains(newEmergency) ){
                    // Check index and see if this emergency is known and updated
                    if (emergencyListIndex.get(newEmergency.getEmergencyId()) != null) {
                        int existingIndex = emergencyListIndex.get(newEmergency.getEmergencyId());

                        Log.d(TAG, "Replacing emergency with updated emergency. New Emergency: " + newEmergency.toString()
                                + " ::: Orig Emergency: " + emergencyList.get(existingIndex).toString());

                        // Add new entry for broadcast
                        newEmList.add(newEmergency);

                        // Pull forward existing emergency location entry to correspond with updated emergency
                        newEmLocations.add(emergencyLocations.get(emergencyLocListIndex.get(newEmergency.getLocationId())));

                        // Remove original (and index) and replace with new for the master list
                        emergencyList.remove(existingIndex);
                        emergencyListIndex.remove(newEmergency.getEmergencyId());

                        emergencyList.add(newEmergency);
                        emergencyListIndex.put(newEmergency.getEmergencyId(),emergencyList.size()-1);



                    } else {
                        // Add new and index
                        newEmList.add(newEmergency);
                        emergencyList.add(newEmergency);
                        emergencyListIndex.put(newEmergency.getEmergencyId(),emergencyList.size()-1);

                        // Find corresponding location object
                        for (Location newLocation : receivedEmLocations) {
                            // Add new location and index
                            if (newLocation.getLocationId() == newEmergency.getLocationId()) {
                                newEmLocations.add(newLocation);
                                emergencyLocations.add(newLocation);
                                emergencyLocListIndex.put(newLocation.getLocationId(),emergencyLocations.size()-1);
                            }
                        }
                    }
                }
            }
        }

        Log.i(TAG, "newEmList: " + emergencyList.toString());
        Log.i(TAG, "TmpEmergencies: " + newEmList.toString());

        Log.i(TAG, "emergencyLocations: " + emergencyLocations.toString());
        Log.i(TAG, "newEmLocations: " + newEmLocations.toString());

        //Only broadcast if something changed...
        if (!newEmList.isEmpty() || (newEmList.isEmpty() && allEmergenciesCleared) ) {
            Log.d(TAG, "Broadcasting delta emergencies out..." + newEmList.toString());

            //Broadcast the new/udpated emergencies to whoever is listening
            Intent localIntent = new Intent("EMERGENCY_ACTION");
            localIntent.putParcelableArrayListExtra(EMERGENCY_LIST, (ArrayList<? extends Parcelable>) newEmList);
            localIntent.putParcelableArrayListExtra(EMERGENCY_LOCATIONS, (ArrayList<? extends Parcelable>) newEmLocations);

            sendBroadcast(localIntent);

            //Clear the list out
            newEmList.clear();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        //Cleanup as needed
        Log.i(TAG, "EmergencyService Destroyed");
    }
}
