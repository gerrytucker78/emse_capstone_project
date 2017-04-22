package com.edu.utdallas.argus.cometnav.dataservices.emergencies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Created by gtucker on 4/11/2017.
 */

public class Emergency implements Parcelable {
    private static final String TAG = "Emergency";
    private int emergencyId;
    private int locationId;
    private String type;
    private String notes;
    private Date start;
    private Date end;
    private Date update;
    //Thu Apr 20 02:08:48 CDT 2017
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");

    public Emergency(){
        //Default constructor is blank
        //But has to be defined since we overloaded it
    }

    protected Emergency(Parcel in) {
        Log.d(TAG, "Creating Emergency from Parcel. Values of Parcel: " + in.toString());
        this.emergencyId = in.readInt();
        this.locationId = in.readInt();
        this.type = in.readString();
        this.notes = in.readString();

        try {
            //Dates have to be converted from string back to DATE
            this.start = sdf.parse(in.readString());
            String tmp = in.readString(); //Should be the end date if it exists...
            this.end = ( !tmp.equalsIgnoreCase("null") ? sdf.parse(tmp) : null);
            tmp = in.readString();
            this.update = ( !tmp.equalsIgnoreCase("null") ? sdf.parse(tmp) : null);
        }catch (ParseException e) {
            Log.d(TAG, "PasrseException: " + e.getMessage().toString());
        }
    }

    public String toString(){
        return "Emergency:::emergencyId=" + this.emergencyId + "::locationId=" + this.locationId
                + "::type=" + this.type + "::notes=" + this.notes + "::start=" + this.start
                + "::end=" + (this.end != null ? end.toString() : "null")
                + "::update=" + (this.update != null ? update.toString() : "null");
    }


    public int getEmergencyId() {
        return emergencyId;
    }

    public void setEmergencyId(int emergencyId) {
        this.emergencyId = emergencyId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getUpdate() {
        return update;
    }

    public void setUpdate(Date update) {
        this.update = update;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.emergencyId);
        dest.writeInt(this.locationId);
        dest.writeString(this.type);
        dest.writeString(this.notes);
        //Dates have to be converted to a string...
        dest.writeString(this.start.toString());
        dest.writeString( (this.end != null ? this.end.toString() : "null") );
        dest.writeString( (this.update != null ? this.update.toString() : "null") );
    }

    public static final Parcelable.Creator<Emergency> CREATOR = new Parcelable.Creator<Emergency>() {
        @Override
        public Emergency createFromParcel(Parcel source) {
            return new Emergency(source);
        }

        @Override
        public Emergency[] newArray(int size) {
            return new Emergency[size];
        }
    };

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Emergency)) {
            return false;
        }
        Emergency that = (Emergency) other;

        return   (this.emergencyId == that.emergencyId )
                && (this.locationId == that.locationId)
                && (this.type.equals(that.type))
                && (this.notes.equals(that.notes))
                && (this.start.equals(that.start))
                && ((this.end == null && that.end == null) || this.end.equals(that.end))
                && ((this.update == null && that.update == null) || this.update.equals(that.update));
    }

    @Override
    public int hashCode() {
        return Objects.hash(emergencyId, locationId, type, notes, start, end, update);
    }
}
