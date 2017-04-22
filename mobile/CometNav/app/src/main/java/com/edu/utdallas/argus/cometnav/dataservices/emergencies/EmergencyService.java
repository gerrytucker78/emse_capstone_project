package com.edu.utdallas.argus.cometnav.dataservices.emergencies;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edu.utdallas.argus.cometnav.dataservices.DataServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gtucker on 4/15/2017.
 * Updated by Michelle
 *
 * EmergencyService class is responsible polling the server periodically to see if an emergency has
 * been issued and the app needs to evacuate out.
 */

public class EmergencyService extends IntentService{
    private static final String TAG="EmergencyService";
    private Timer emergencyTimer;
    private TimerTask timerTask;
    private final Handler handler = new Handler();
    private EmergencyClient emergencyClient = new EmergencyClient();
    private List<Emergency> emergencyList = new ArrayList<Emergency>();


    public EmergencyService(){
        super(TAG);
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

    private void startEmergencyTimer()
    {
        //set a new Timer
        emergencyTimer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first second the TimerTask will run every 5 seconds
        emergencyTimer.schedule(timerTask, 1000, 5000); //
    }

    private void stopEmergencyTimer()
    {
        //stop the timer, if it's not already null
        if (emergencyTimer != null)
        {
            emergencyTimer.cancel();
            emergencyTimer = null;
        }
    }

    private void initializeTimerTask()
    {
        timerTask = new TimerTask()
        {
            public void run()
            {
                handler.post(new Runnable()
                {
                    public void run()
                    {
                        //Calling server to get emergencies
                        DataServices.getEmergencies(emergencyClient);

                        //Get the list of emergencies
                        List<Emergency> newEmergenciesList = emergencyClient.getEmergenciesMap();

                        //Tmp list will contain the deltas from newEmergencies and current emergencies
                        List<Emergency> tmpList = new ArrayList<Emergency>();

                        if ( emergencyList.isEmpty() ) {
                            //Congrats, the empty list now has all the emergencies in it
                            Log.d(TAG, "EmergencyList empty. Setting it and tmpList to newEmergenciesList");
                            emergencyList.addAll(newEmergenciesList);
                            tmpList.addAll(emergencyList);
                        }else if (emergencyList.equals(newEmergenciesList)){
                            Log.d(TAG, "New emergency list equals current emergency list...");
                            //Do Nothing
                        }
                        else{
                            //Filter based on emergencies you already have
                            for (Emergency newEmergency : newEmergenciesList) {
                                if( !emergencyList.contains(newEmergency) ){
                                    //Iterate over the emergencyList to find the object and replace it
                                    for (int i = 0; i < emergencyList.size(); i++){
                                        if(newEmergency.getEmergencyId() == emergencyList.get(i).getEmergencyId()){
                                            Log.d(TAG, "Replacing emergency with updated emergency. New Emergency: " + newEmergency.toString()
                                            + " ::: Orig Emergency: " + emergencyList.get(i).toString());
                                            tmpList.add(newEmergency);
                                            emergencyList.remove(i); //Remove original emergency
                                            emergencyList.add(newEmergency); //Add the updated emergency
                                        }
                                    }
                                }
                            }
                        }

                        Log.i(TAG, "Emergiencies: " + emergencyList.toString());
                        Log.i(TAG, "TmpEmergencies: " + tmpList.toString());

                        //Only broadcast if something changed...
                        if (!tmpList.isEmpty()) {
                            Log.d(TAG, "Broadcasting delta emergencies out..." + tmpList.toString());

                            //Broadcast the new/udpated emergencies to whoever is listening
                            Intent localIntent = new Intent("EMERGENCY_ACTION");
                            localIntent.putParcelableArrayListExtra("EMERGENCY_LIST", (ArrayList<? extends Parcelable>) tmpList);
                            sendBroadcast(localIntent);

                            //Clear the list out
                            tmpList.clear();
                         }
        }
        });
        }
        };
        }

@Override
public void onDestroy() {
        super.onDestroy();

        //Cleanup as needed
        Log.i(TAG, "EmergencyService Destroyed");
        }
        }
