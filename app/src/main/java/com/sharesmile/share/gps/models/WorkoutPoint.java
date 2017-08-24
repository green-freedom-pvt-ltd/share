package com.sharesmile.share.gps.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari on 8/23/17.
 */

public class WorkoutPoint implements UnObfuscable, Parcelable {

    @SerializedName("acc")
    private float accuracy; // in meters

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    @SerializedName("ts")
    private long timeStamp; // in millis

    @SerializedName("ber")
    private float bearing; // in degrees

    @SerializedName("alt")
    private double altitude; // in meters

    @SerializedName("spd")
    private float gpsSpeed; // in m/s

    @SerializedName("stc")
    private int cumulativeStepCount; // cumulative step count

    @SerializedName("dis")
    private float cumulativeDistance;

    @SerializedName("nspk")
    private int cumulativeNumSpikes;

    @SerializedName("flag")
    private Boolean flagged;

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
        if (source.flagged != null){
            flagged = source.flagged;
        }
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

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
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
        byte flaggedVal = in.readByte();
        flagged = flaggedVal == 0x02 ? null : flaggedVal != 0x00;
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
        if (flagged == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (flagged ? 0x01 : 0x00));
        }
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
