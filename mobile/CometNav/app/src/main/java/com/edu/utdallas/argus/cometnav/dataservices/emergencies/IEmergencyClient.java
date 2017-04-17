package com.edu.utdallas.argus.cometnav.dataservices.emergencies;

import org.json.JSONArray;

import java.util.Map;

/**
 * Created by Michelle on 4/15/2017.
 */

public interface IEmergencyClient {
    public void receiveEmergencies(JSONArray emergencies);
    public Map<Integer, Emergency> getEmergenciesMap();
}