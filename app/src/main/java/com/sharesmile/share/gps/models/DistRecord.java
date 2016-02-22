package com.sharesmile.share.gps.models;

import android.location.Location;

import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class DistRecord implements UnObfuscable, Serializable{

    private static final String TAG = "DistRecord";

    private Location point;
    private DistRecord prevRecord;
    private float dist; // in meters
    private float interval; // in secs
    private float speed; // in m/s
    private float bearing; // bearing of point in degrees, irrespective of the prevRecord


    public DistRecord(Location point){
        this.point = point;
    }

    public DistRecord(Location point, DistRecord prevRecord, float dist){
        this.point = point;
        this.prevRecord = prevRecord;
        bearing = point.getBearing();
        // distanceTo() method, though not blocking but is very computationally intensive
        this.dist = dist;
        interval = ((float) (point.getTime() - prevRecord.getPoint().getTime())) / 1000;
        speed = dist / interval;
    }

    @Override
    public String toString() {
        return "DistRecord{" +
                "point=" + point +
                ", prevRecord=" + prevRecord +
                ", dist=" + dist +
                ", interval=" + interval +
                ", speed=" + speed +
                ", bearing=" + bearing +
                '}';
    }

    public boolean isSource(){
        if (prevRecord == null){
            return true;
        }
        return false;
    }

    public Location getPoint() {
        return point;
    }

    public float getDist() {
        return dist;
    }

    public float getInterval() {
        return interval;
    }

    public float getSpeed() {
        return speed;
    }

    public float getBearing() {
        return bearing;
    }

    public DistRecord getPrevRecord() {
        return prevRecord;
    }
}
