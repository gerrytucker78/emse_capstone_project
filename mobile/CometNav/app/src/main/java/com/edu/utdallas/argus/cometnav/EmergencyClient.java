package com.edu.utdallas.argus.cometnav;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gtucker on 4/11/2017.
 */

public class EmergencyClient  implements EmergencyClientInterface {
    private static final String TAG="EmergencyClient";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private Map<Integer,Emergency> emergenciesMap = new HashMap();

    @Override
    public void receiveEmergencies(JSONArray emergencies) {
        try
        {
            for(int i = 0; i < emergencies.length(); i++)
            {
                JSONObject rawEm = emergencies.getJSONObject(i);
                Emergency em = new Emergency();
                em.setEmergencyId(rawEm.getInt("location_id"));

                if(!rawEm.getString("emergency_end").equalsIgnoreCase("null")){
                    //Only parse the date if the value isn't "null"
                    em.setEnd(sdf.parse(rawEm.getString("emergency_end")));
                }

                em.setLocationId(rawEm.getInt("location_id"));
                em.setNotes(rawEm.getString("emergency_notes"));

                if(!rawEm.getString("emergency_start").equalsIgnoreCase("null")){
                    //Only parse the date if the value isn't null
                    em.setStart(sdf.parse(rawEm.getString("emergency_start")));
                }

                em.setType(rawEm.getString("emergency_type"));

                if(!rawEm.getString("emergency_last_update").equalsIgnoreCase("null")) {
                    em.setUpdate(sdf.parse(rawEm.getString("emergency_last_update")));
                }

                emergenciesMap.put(em.getEmergencyId(), em);
            }
        }
        catch (JSONException e)
        {
            Log.d(TAG, "JSONException: " + e.getMessage().toString());
        } catch (ParseException e) {
            Log.d(TAG, "PasrseException: " + e.getMessage().toString());
        }
        Log.d(TAG, "Emergencies updated: " + this.getEmergenciesMap().size());

    }

    @Override
    public Map<Integer, Emergency> getEmergenciesMap() {
        return emergenciesMap;
    }


}
