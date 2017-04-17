package com.edu.utdallas.argus.cometnav.dataservices.locations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gtucker on 4/16/2017.
 */

public class Location {
    private int locationId;
    private int floor;
    private String name;
    private Location.Type type;
    private String map;
    private int pixelLocX;
    private int pixelLocY;

    private static final String JSON_LOCATION_ID = "location_id";
    private static final String JSON_FLOOR = "floor";
    private static final String JSON_NAME = "name";
    private static final String JSON_TYPE = "type";
    private static final String JSON_MAP = "map";
    private static final String JSON_PIXEL_LOC_X = "pixel_loc_x";
    private static final String JSON_PIXEL_LOC_Y = "pixel_loc_y";

    public static List<Location> createLocations(JSONArray locations) {
        List<Location> results = new ArrayList<Location>();

        try {

            for (int i = 0; i < locations.length(); i++) {
                JSONObject jsonLocation = locations.getJSONObject(i);
                Location location = new Location();
                location.setLocationId(jsonLocation.getInt(JSON_LOCATION_ID));
                location.setFloor(jsonLocation.getInt(JSON_FLOOR));
                location.setName(jsonLocation.getString(JSON_NAME));
                location.setType(jsonLocation.getString(JSON_TYPE));
                location.setMap(jsonLocation.getString(JSON_MAP));
                location.setPixelLocX(jsonLocation.getInt(JSON_PIXEL_LOC_X));
                location.setPixelLocY(jsonLocation.getInt(JSON_PIXEL_LOC_Y));

                results.add(location);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(String type) {
        this.type = Type.valueOf(type);
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public int getPixelLocX() {
        return pixelLocX;
    }

    public void setPixelLocX(int pixelLocX) {
        this.pixelLocX = pixelLocX;
    }

    public int getPixelLocY() {
        return pixelLocY;
    }

    public void setPixelLocY(int pixelLocY) {
        this.pixelLocY = pixelLocY;
    }

    public String toString() {
        String result = JSON_LOCATION_ID + ": " + this.locationId +
                ", " + JSON_NAME + ": " + this.name +
                ", " + JSON_TYPE + ": " + this.type +
                ", " + JSON_PIXEL_LOC_X + ": " + this.pixelLocX +
                ", " + JSON_PIXEL_LOC_Y + ": " + this.pixelLocY +
                ", " + JSON_FLOOR + ": " + this.floor;

        return result;
    }

    public enum Type {
        BLOCKED_AREA,
        ROOM,
        HALL,
        EXIT,
        STAIRS,
        EMERGENCY,
        UNKNOWN
    }

    ;

}
