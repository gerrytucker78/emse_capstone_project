package com.edu.utdallas.argus.cometnav;

import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Daniel on 4/14/2017.
 */

public class CometNavBeacon extends Beacon {

    private int name;
    private int floor;
    private int xLoc;
    private int yLoc;

    public CometNavBeacon(JSONObject beaconJson)
    {
        try
        {
            setName(Integer.parseInt(beaconJson.getString("name").substring(12), 16));
            setFloor(beaconJson.getInt("floor"));
            setxLoc(beaconJson.getInt("pixel_loc_x"));
            setyLoc(beaconJson.getInt("pixel_loc_y"));
        }
        catch (JSONException | NumberFormatException e)
        {
            Log.d("Beacon", e.toString());
        }
    }

    public String toString()
    {
        return name + "," + floor + "," + xLoc + "," + yLoc;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getxLoc() {
        return xLoc;
    }

    public void setxLoc(int xLoc) {
        this.xLoc = xLoc;
    }

    public int getyLoc() {
        return yLoc;
    }

    public void setyLoc(int yLoc) {
        this.yLoc = yLoc;
    }
}
