package com.sharesmile.share.gps.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.core.UnObfuscable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class WorkoutData implements UnObfuscable, Parcelable{

    private static final String TAG = "WorkoutData";

    private float distance; // in m
    private long beginTimeStamp; // in millisecs
    private float recordedTime; // in secs
    private int totalSteps;
    private List<LatLng> points;

    public WorkoutData(long beginTimeStamp) {
        this.beginTimeStamp = beginTimeStamp;
        points = new ArrayList<>();
    }

    public float getDistance() {
        return distance;
    }

    /**
     * @return time interval for which distance has been recorded
     */
    public float getRecordedTime() {
        return recordedTime;
    }

    /**
     * @return elapsed time since the beginning of workout in secs
     */
    public float getElapsedTime(){
        return (System.currentTimeMillis() - beginTimeStamp) / 1000;
    }

    public float getAvgSpeed() {
        if (recordedTime > 0){
            return (distance / recordedTime);
        }else
            return 0;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void addRecord(DistRecord record){
        addDistance(record.getDist());
        addPoint(record.getLocation());
    }

    public void addSteps(int numSteps){
        totalSteps = totalSteps + numSteps;
    }

    public void setTotalSteps(int total){
        totalSteps = total;
    }

    public void addDistance(float distanceInRecord) {
        this.distance = distance + distanceInRecord;
    }

    public void setSource(Location source){
        points.add(0, new LatLng(source.getLatitude(), source.getLongitude()));
    }

    private void addPoint(Location point){
        points.add(new LatLng(point.getLatitude(), point.getLongitude()));
    }

    public void setRecordedTime(float recordedTime) {
        this.recordedTime = recordedTime;
    }

    @Override
    public String toString() {
        return "WorkoutData{" +
                "distance=" + distance +
                ", recordedTime=" + recordedTime +
                ", avgSpeed=" + getAvgSpeed() +
                ", totalSteps=" + getTotalSteps() +
                ", points=" + TextUtils.join("|", points) +
                '}';
    }

    protected WorkoutData(Parcel in) {
        distance = in.readFloat();
        recordedTime = in.readFloat();
        if (in.readByte() == 0x01) {
            points = new ArrayList<LatLng>();
            in.readList(points, LatLng.class.getClassLoader());
        } else {
            points = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(distance);
        dest.writeFloat(recordedTime);
        if (points == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(points);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WorkoutData> CREATOR = new Parcelable.Creator<WorkoutData>() {
        @Override
        public WorkoutData createFromParcel(Parcel in) {
            return new WorkoutData(in);
        }

        @Override
        public WorkoutData[] newArray(int size) {
            return new WorkoutData[size];
        }
    };
}

