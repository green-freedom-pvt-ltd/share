package com.sharesmile.share.gps.models;

import android.location.Location;
import android.os.Build;

import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class DistRecord implements UnObfuscable, Serializable{

    private static final String TAG = "DistRecord";

    private Location location;
    private Location prevLocation;
    private float dist; // in meters
    private float interval; // in secs
    private float speed; // in m/s
    private float bearing; // bearing of location in degrees, irrespective of the prevLocation


    public DistRecord(Location location){
        this.location = location;
    }

    /**
     * Constructor which uses speed from location objects to calculate distance
     * @param location
     * @param prevLocation
     */
    public DistRecord(Location location, Location prevLocation){
        this.location = location;
        this.prevLocation = prevLocation;
        bearing = location.getBearing();
        this.speed = (location.getSpeed() + prevLocation.getSpeed()) / 2;
        interval = ((float) (getElapsedTimeMs())) / 1000;
        this.dist = speed*interval;
    }

    public DistRecord(Location location, Location prevLocation, float dist){
        this.location = location;
        this.prevLocation = prevLocation;
        bearing = location.getBearing();
        // distanceTo() method, though not blocking but is very computationally intensive
        this.dist = dist;
        interval = ((float) (getElapsedTimeMs())) / 1000;
        speed = dist / interval;
    }

    private long getElapsedTimeMs(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            return (location.getElapsedRealtimeNanos() - prevLocation.getElapsedRealtimeNanos()) / 1000000;
        }else{
            return (location.getTime() - prevLocation.getTime());
        }
    }

    @Override
    public String toString() {
        return "DistRecord{" +
                "  location=" + location +
                ", prevLocation=" + prevLocation +
                ", dist=" + dist +
                ", interval=" + interval +
                ", speed=" + speed +
                ", bearing=" + bearing +
                '}';
    }

    public boolean isSource(){
        if (prevLocation == null){
            return true;
        }
        return false;
    }

    public Location getLocation() {
        return location;
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

    public Location getPrevLocation() {
        return prevLocation;
    }
}
