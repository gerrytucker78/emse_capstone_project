package com.edu.utdallas.argus.cometnav;

/**
 * Created by Daniel on 4/14/2017.
 */

public class CurrentLocation {

    private int xLoc = 0;
    private int yLoc = 0;
    private int floor = 0;
    private double radius = 0;

    public String toString()
    {
        return xLoc + "," + yLoc + "," + floor + "," + radius;
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

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
