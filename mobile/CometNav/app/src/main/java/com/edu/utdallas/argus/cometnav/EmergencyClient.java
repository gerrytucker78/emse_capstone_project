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

public class EmergencyClient {
    private Map<Integer,Emergency> emergencies = new HashMap();

    public void receiveEmergencies(JSONArray emergencies) {
        try
        {

            for(int i = 0; i < emergencies.length(); i++)
            {
                JSONObject rawEm = emergencies.getJSONObject(i);
                Emergency em = new Emergency();
                em.setEmergencyId(rawEm.getInt("location_id"));

                String dateStr = rawEm.getString("birthdate");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                em.setEnd(sdf.parse(rawEm.getString("emergency_end")));
                em.setLocationId(rawEm.getInt("location_id"));
                em.setNotes(rawEm.getString("emergency_notes"));
                em.setStart(sdf.parse(rawEm.getString("emergency_start")));
                em.setType(rawEm.getString("emergency_type"));
                em.setUpdate(sdf.parse(rawEm.getString("emergency_last_update")));

                this.getEmergencies().put(em.getEmergencyId(), em);
            }
        }
        catch (JSONException e)
        {
            Log.d("Emergency Client", e.getMessage().toString());
        } catch (ParseException e) {
            Log.d("Emergency Client", e.getMessage().toString());
        }
        Log.d("Emergency Client", "Emergencies updated: " + this.getEmergencies().size());


    }

    public Map<Integer, Emergency> getEmergencies() {
        return emergencies;
    }
}
