package com.sharesmile.share.gps.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari on 8/23/17.
 */

public class WorkoutPoint implements UnObfuscable, Parcelable {

    private float accuracy; // in meters
    private double latitude;
    private double longitude;
    private long timeStamp; // in millis
    private float bearing; // in degrees
    private double altitude; // in meters
    private float gpsSpeed; // in m/s
    private int cumulativeStepCount; // cumulative step count
    private float cumulativeDistance;
    private int cumulativeNumSpikes;

    public WorkoutPoint(){
    }

    public WorkoutPoint(WorkoutPoint source){
        accuracy = source.accuracy;
        latitude = source.latitude;
        longitude = source.longitude;
        timeStamp = source.timeStamp;
        bearing = source.bearing;
        altitude = source.altitude;
        gpsSpeed = source.gpsSpeed;
        cumulativeStepCount = source.cumulativeStepCount;
        cumulativeDistance = source.cumulativeDistance;
        cumulativeNumSpikes = source.cumulativeNumSpikes;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getGpsSpeed() {
        return gpsSpeed;
    }

    public void setGpsSpeed(float gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
    }

    public int getCumulativeStepCount() {
        return cumulativeStepCount;
    }

    public void setCumulativeStepCount(int cumulativeStepCount) {
        this.cumulativeStepCount = cumulativeStepCount;
    }

    public float getCumulativeDistance() {
        return cumulativeDistance;
    }

    public void setCumulativeDistance(float cumulativeDistance) {
        this.cumulativeDistance = cumulativeDistance;
    }

    public int getCumulativeNumSpikes() {
        return cumulativeNumSpikes;
    }

    public void setCumulativeNumSpikes(int cumulativeNumSpikes) {
        this.cumulativeNumSpikes = cumulativeNumSpikes;
    }

    protected WorkoutPoint(Parcel in) {
        accuracy = in.readFloat();
        latitude = in.readDouble();
        longitude = in.readDouble();
        timeStamp = in.readLong();
        bearing = in.readFloat();
        altitude = in.readDouble();
        gpsSpeed = in.readFloat();
        cumulativeStepCount = in.readInt();
        cumulativeDistance = in.readFloat();
        cumulativeNumSpikes = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(accuracy);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(timeStamp);
        dest.writeFloat(bearing);
        dest.writeDouble(altitude);
        dest.writeFloat(gpsSpeed);
        dest.writeInt(cumulativeStepCount);
        dest.writeFloat(cumulativeDistance);
        dest.writeInt(cumulativeNumSpikes);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WorkoutPoint> CREATOR = new Parcelable.Creator<WorkoutPoint>() {
        @Override
        public WorkoutPoint createFromParcel(Parcel in) {
            return new WorkoutPoint(in);
        }

        @Override
        public WorkoutPoint[] newArray(int size) {
            return new WorkoutPoint[size];
        }
    };

}
