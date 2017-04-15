package com.edu.utdallas.argus.cometnav;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
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
    private Map<Integer,Emergency> emergenciesMap = new HashMap();


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
                        //Calling server to get emergiencies
                        DataServices.getEmergencies(emergencyClient);
                        emergenciesMap = emergencyClient.getEmergenciesMap();
                        Log.i(TAG, "Emergiencies: " + emergenciesMap.toString());
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
