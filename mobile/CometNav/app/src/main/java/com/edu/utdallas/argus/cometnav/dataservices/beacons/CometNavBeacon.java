package com.edu.utdallas.argus.cometnav.dataservices.beacons;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Created by Daniel on 4/14/2017.
 */

public class CometNavBeacon implements Parcelable {

    private int name = 0;
    private String strName = "";
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

    private LinkedList<Double> prevDistMtrs = new LinkedList<Double>();

    /**
     * distance in meters
     */
    private double distMtr;

    /**
     * Creates a CometNavBeacon from a Beacon object
     * @param beacon
     */
    public CometNavBeacon(Beacon beacon)
    {
        try
        {
            //Add 2 to the cut length for the "0x"
            strName = beacon.getId1().toString();
            setName(Integer.parseInt(beacon.getId1().toString().substring(STR_CUT_LENGTH + 2), 16));
            //Log.d("Beacon", Integer.toString(getName()));
            //the beacon's distance is in meters
            setDistMtr(beacon.getDistance());
        }
        catch (NumberFormatException e)
        {
            Log.d("Beacon", "EXCEPTION in beacon construction: " + e.toString());
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
            setName(Integer.parseInt(beaconJson.getString("name").substring(STR_CUT_LENGTH), 16));
            setFloor(beaconJson.getInt("floor"));
            setxLoc(beaconJson.getInt("pixel_loc_x"));
            setyLoc(beaconJson.getInt("pixel_loc_y"));
        }
        catch (JSONException | NumberFormatException | StringIndexOutOfBoundsException e)
        {
            Log.d("Navigation", "EXCEPTION in beacon construction: " + e.toString());
        }
    }

    public String toString()
    {
        return name + "," + getDistance() + "," + floor + "," + xLoc + "," + yLoc;
    }

    public int getName() {
        return name;
    }

    public String getStrName() {
        return strName;
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
        return getDistMtr() * PIXEL_METER_CONV_CONST;
    }

    public void setDistMtr(double distance) {
//        double avg = getAvgDist();
//        if (avg != 0 && Math.abs(distance - avg) > (avg * .5))
//        {
//            Log.d("Beacon", "Throwing out large discrepancy in distance for " + getStrName() + ", avg " + avg + " distance " + distance);
//            //throw out large discrepancies in distances
//        }
//        else
//        {
            prevDistMtrs.add(distance);
            if (prevDistMtrs.size() > 10)
                prevDistMtrs.pop();
            this.distMtr = distance;
        //}
    }

    public double getDistMtr() {
        //Do a running average
        return getAvgDist();
        //return distMtr;
    }

    private double getAvgDist() {
        if (prevDistMtrs.size() == 0)
            return 0;
        double sum = 0.0;
        int counter = 0;
        for (double d : prevDistMtrs) {
            sum += d;
            counter++;
        }
        return sum/counter;
    }

    @Override
    public boolean equals(final Object object) {
        Log.d("Dan", "Hitting equals!");
        boolean result = false;
        if (object instanceof CometNavBeacon) {
            CometNavBeacon otherBeacon = (CometNavBeacon) object;
            result = (this.name == otherBeacon.name);
        }
        return result;
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
        dest.writeList(this.prevDistMtrs);
        //dest.writeDouble(this.distance);

    }

    protected CometNavBeacon(Parcel in) {
        this.name = in.readInt();
        this.floor = in.readInt();
        this.xLoc = in.readInt();
        this.yLoc = in.readInt();
        //this.distance = in.readDouble();
        in.readList(this.prevDistMtrs, null);
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
