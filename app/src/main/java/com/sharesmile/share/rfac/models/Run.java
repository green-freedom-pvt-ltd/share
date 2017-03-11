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

    @SerializedName("run_amount")
    float runAmount;

    @SerializedName("run_duration")
    String runDuration;

    @SerializedName("avg_speed")
    float avgSpeed;

    @SerializedName("is_flag")
    boolean is_flag = false;

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

    public float getRunAmount() {
        return runAmount;
    }

    public void setRunAmount(float runAmount) {
        this.runAmount = runAmount;
    }

    public String getRunDuration() {
        return runDuration;
    }

    public void setRunDuration(String runDuration) {
        this.runDuration = runDuration;
    }

    public boolean isFlag() {
        return is_flag;
    }

    public void setIsFlag(boolean is_flag) {
        this.is_flag = is_flag;
    }
}
