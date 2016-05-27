package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;
import com.sharesmile.share.utils.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Shine on 15/05/16.
 */
public class Run implements UnObfuscable, Serializable {

    @SerializedName("run_id")
    long id;
    @SerializedName("cause_run_title")
    String causeName;

    @SerializedName("start_time")
    String startTime;

    @SerializedName("distance")
    float distance;

    @SerializedName("avg_speed")
    float avgSpeed;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCauseName() {
        return causeName;
    }

    public void setCauseName(String causeName) {
        this.causeName = causeName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(float avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public Date getData() {
        return DateUtil.getDefaultFormattedDate(getStartTime());
    }
}
