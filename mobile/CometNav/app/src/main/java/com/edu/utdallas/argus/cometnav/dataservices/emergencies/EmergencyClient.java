package com.edu.utdallas.argus.cometnav.dataservices.emergencies;

import android.util.Log;

import com.edu.utdallas.argus.cometnav.dataservices.locations.Location;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gtucker on 4/11/2017.
 */

public class EmergencyClient implements IEmergencyClient {
    private static final String TAG = "EmergencyClient";

    //private Map<Integer,Emergency> emergencies = new HashMap();
    private List<Emergency> emergencies = new ArrayList<Emergency>();
    private List<Location> emergencyLocations = new ArrayList<Location>();
    private EmergencyService es = null;

    public EmergencyClient(EmergencyService es) {
        this.es = es;
    }

    @Override
    public void receiveEmergencies(JSONArray emergencies, JSONArray emergencyLocations) {
        this.emergencies.clear();
        this.getEmergencyLocations().clear();

        this.emergencies = Emergency.createEmergencies(emergencies);
        this.emergencyLocations = Location.createLocations(emergencyLocations);

        Log.d(TAG, "Emergencies updated: " + this.emergencies.size());
        Log.d(TAG, "Emergency Locations updated: " + this.getEmergencyLocations().size());

        this.es.processUpdatedEmergencies();
    }

    public List<Emergency> getEmergencies() {
        return emergencies;
    }


    public List<Location> getEmergencyLocations() {
        return emergencyLocations;
    }
}
