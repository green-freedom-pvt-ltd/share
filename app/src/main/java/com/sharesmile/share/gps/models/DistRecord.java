package com.sharesmile.share.gps.models;

import android.location.Location;

import com.sharesmile.share.core.config.ClientConfig;
import com.sharesmile.share.core.config.Config;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class DistRecord implements UnObfuscable, Serializable{

    private static final String TAG = "DistRecord";

    private Location location;
    private Location prevLocation;
    private long timeStamp;
    private long prevTimeStamp;
    private float dist; // in meters
    private double interval; // in millis
    private float speed; // in m/s
    private float bearing; // bearing of location in degrees, irrespective of the prevLocation


    public DistRecord(Location location){
        this.location = location;
        this.timeStamp = DateUtil.getServerTimeInMillis();
    }

    public DistRecord(Location location, Location prevLocation,
                      long prevTimeStamp, float dist){
        this.location = location;
        this.prevLocation = prevLocation;
        this.timeStamp = DateUtil.getServerTimeInMillis();
        this.prevTimeStamp = prevTimeStamp;
        bearing = location.getBearing();
        // distanceTo() method, though not blocking but is very computationally intensive
        this.dist = dist;
        interval = getElapsedTimeMs();
        speed = (getElapsedTimeMs() == 0) ? 0 : ((dist * 1000) / getElapsedTimeMs());
    }

    private long getElapsedTimeMs(){
        return (timeStamp - prevTimeStamp);
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

    public boolean isFirstRecordAfterResume(){
        if (location != null && prevLocation == null){
            return true;
        }
        return false;
    }

    public Location getLocation() {
        return location;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public boolean isTooOld(){
        return (DateUtil.getServerTimeInMillis() - getTimeStamp() > Config.CURRENT_SPEED_VALIDITY_THRESHOLD_INTERVAL);
    }

    public float getDist() {
        return dist;
    }

    /**
     * @return interval in millis between two geolocation points in this DistRecord
     */
    public long getInterval() {
        return Math.round(interval);
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

    public void normaliseStepCountWrtStepCount(int currentSteps, int numStepsAtPreviousRecord,
                                               float totalDistance){
        Logger.d(TAG, "normaliseStepCountWrtStepCount");
        int stepsInInterval = currentSteps - numStepsAtPreviousRecord;
        long secsInInterval = getInterval() / 1000;
        float distInInterval = getDist();

        if (stepsInInterval <= 0){
            // Can't perform normalisation if we don't have step count
            return;
        }

        if (secsInInterval > ClientConfig.getInstance().MIN_INTERVAL_FOR_DISTANCE_NORMALISATION){
            // Can't perform normalisation if time interval b/w two consecutive GPS points is less than min threshold
            return;
        }

        float strideLengthSoFarInWorkout = (numStepsAtPreviousRecord == 0) ?
                ClientConfig.getInstance().GLOBAL_AVERAGE_STRIDE_LENGTH
                : (totalDistance - distInInterval) / numStepsAtPreviousRecord;

        float strideLengthInInterval = distInInterval / stepsInInterval;

        Logger.d(TAG, "normaliseStepCountWrtStepCount: strideLengthInInterval = " + strideLengthInInterval
                + ", strideLengthSoFarInWorkout = " + strideLengthSoFarInWorkout);
        if (strideLengthInInterval < ClientConfig.getInstance().GLOBAL_STRIDE_LENGTH_LOWER_LIMIT){
            // Stride length in this interval is too low, that means less distance is being recorded
            // We need to normalise it with the strideLength recorded so far in the workout
            float normalisedStrideLength = Utils.getNormalizedStrideLength(strideLengthSoFarInWorkout);
            dist = stepsInInterval * normalisedStrideLength;
            speed = (getElapsedTimeMs() == 0) ? 0 : ((dist * 1000) / getElapsedTimeMs());
            String message = "Normalised distance from " + distInInterval + " to " + dist;
            Logger.d(TAG, message);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DistRecord that = (DistRecord) o;

        if (Float.compare(that.dist, dist) != 0) return false;
        if (Double.compare(that.interval, interval) != 0) return false;
        if (!location.equals(that.location)) return false;
        return prevLocation != null ? prevLocation.equals(that.prevLocation) : that.prevLocation == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = location.hashCode();
        result = 31 * result + (prevLocation != null ? prevLocation.hashCode() : 0);
        result = 31 * result + (dist != +0.0f ? Float.floatToIntBits(dist) : 0);
        temp = Double.doubleToLongBits(interval);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
