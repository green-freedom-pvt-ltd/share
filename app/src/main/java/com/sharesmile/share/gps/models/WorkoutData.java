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
    private float time; // in secs
    private int totalSteps;
    private List<LatLng> points;

    public WorkoutData() {
        points = new ArrayList<>();
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

    private void addDistance(float distanceInRecord) {
        this.distance = distance + distanceInRecord;
    }

    public void setSource(Location source){
        points.add(0, new LatLng(source.getLatitude(), source.getLongitude()));
    }

    private void addPoint(Location point){
        points.add(new LatLng(point.getLatitude(), point.getLongitude()));
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
                ", totalSteps=" + getTotalSteps() +
                ", points=" + TextUtils.join("|", points) +
                '}';
    }

    protected WorkoutData(Parcel in) {
        distance = in.readFloat();
        time = in.readFloat();
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
        dest.writeFloat(time);
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

