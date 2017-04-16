package com.edu.utdallas.argus.cometnav.dataservices.beacons;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Daniel on 4/14/2017.
 */

public class CometNavBeacon implements Parcelable {

    private int name = 0;
    private int floor = 0;
    private int xLoc = 0;
    private int yLoc = 0;
    private final Integer STR_CUT_LENGTH = 14;
    private final double PIXEL_METER_CONV_CONST = 373/101;
    private final double PIXEL_FEET_CONV_CONST = 373/332;
    /**
     * Distance in pixels.
     */
    private double distance;

    /**
     * Creates a CometNavBeacon from a Beacon object
     * @param beacon
     */
    public CometNavBeacon(Beacon beacon)
    {
        try
        {
            Log.d("Navigation", beacon.getId1().toString());
            Log.d("Navigation", beacon.getId1().toString().substring(STR_CUT_LENGTH + 2));
            //Add 2 to the cut length for the "0x"
            Log.d("Navigation", Integer.toString(Integer.parseInt(beacon.getId1().toString().substring(STR_CUT_LENGTH + 2), 16)));
            setName(Integer.parseInt(beacon.getId1().toString().substring(STR_CUT_LENGTH + 2), 16));
            Log.d("Navigation", name + " ");
            //the beacon's distance is either in meters or feet, I think meters for now
            setDistance(beacon.getDistance() * PIXEL_METER_CONV_CONST);
        }
        catch (NumberFormatException e)
        {
            Log.d("Navigation", "EXCEPTION in beacon construction: " + e.toString());
        }
    }

    /**
     * Creates a CometNavBeacon from a JSON object containing a beacon.
     * @param beaconJson
     */
    public CometNavBeacon(JSONObject beaconJson)
    {
        try
        {
            Log.d("Navigation", beaconJson.getString("name"));
            Log.d("Navigation", beaconJson.getString("name").substring(STR_CUT_LENGTH));
            Log.d("Navigation", Integer.toString(Integer.parseInt(beaconJson.getString("name").substring(STR_CUT_LENGTH), 16)));
            setName(Integer.parseInt(beaconJson.getString("name").substring(STR_CUT_LENGTH), 16));
            Log.d("Navigation", name + " ");
            setFloor(beaconJson.getInt("floor"));
            setxLoc(beaconJson.getInt("pixel_loc_x"));
            setyLoc(beaconJson.getInt("pixel_loc_y"));
        }
        catch (JSONException | NumberFormatException e)
        {
            Log.d("Navigation", "EXCEPTION in beacon construction: " + e.toString());
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.name);
        dest.writeInt(this.floor);
        dest.writeInt(this.xLoc);
        dest.writeInt(this.yLoc);
        dest.writeDouble(this.distance);
    }

    protected CometNavBeacon(Parcel in) {
        this.name = in.readInt();
        this.floor = in.readInt();
        this.xLoc = in.readInt();
        this.yLoc = in.readInt();
        this.distance = in.readDouble();
    }

    public static final Parcelable.Creator<CometNavBeacon> CREATOR = new Parcelable.Creator<CometNavBeacon>() {
        @Override
        public CometNavBeacon createFromParcel(Parcel source) {
            return new CometNavBeacon(source);
        }

        @Override
        public CometNavBeacon[] newArray(int size) {
            return new CometNavBeacon[size];
        }
    };
}
