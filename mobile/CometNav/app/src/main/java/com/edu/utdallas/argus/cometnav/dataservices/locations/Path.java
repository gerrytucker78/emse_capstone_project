package com.edu.utdallas.argus.cometnav.dataservices.locations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gtucker on 4/16/2017.
 */

public class Path {
    private int startId;
    private int endId;
    private int weight;

    private static final String JSON_START_ID = "start_id";
    private static final String JSON_END_ID = "end_id";
    private static final String JSON_WEIGHT = "weight";

    public static List<Path> createPaths(JSONArray paths) {
        List<Path> results = new ArrayList<Path>();

        try {

            for (int i = 0; i < paths.length(); i++) {
                JSONObject jsonPath = paths.getJSONObject(i);
                Path path = new Path();
                path.setStartId(jsonPath.getInt(JSON_START_ID));
                path.setEndId(jsonPath.getInt(JSON_END_ID));
                path.setWeight(jsonPath.getInt(JSON_WEIGHT));

                results.add(path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }

    public int getStartId() {
        return startId;
    }

    public void setStartId(int startId) {
        this.startId = startId;
    }

    public int getEndId() {
        return endId;
    }

    public void setEndId(int endId) {
        this.endId = endId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
