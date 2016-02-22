package com.sharesmile.share.gps.models;

import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class WorkoutData implements UnObfuscable, Serializable{

    private static final String TAG = "WorkoutData";

    private float distance; // in m
    private float time; // in secs

    public WorkoutData() {
    }

    public float getDistance() {
        return distance;
    }

    public float getTime() {
        return time;
    }

    public float getAvgSpeed() {
        if (time > 0){
            return (distance / time);
        }else
            return 0;
    }

    public void addDistance(float distanceInRecord) {
        this.distance = distance + distanceInRecord;
    }

    public void setTime(float time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "WorkoutData{" +
                "distance=" + distance +
                ", time=" + time +
                ", avgSpeed=" + getAvgSpeed() +
                '}';
    }
}

