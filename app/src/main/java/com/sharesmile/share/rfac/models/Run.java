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

    @SerializedName("start_time_epoch")
    Long startTimeEpoch;

    @SerializedName("end_time")
    String endTime;

    @SerializedName("end_time_epoch")
    String endTimeEpoch;

    @SerializedName("distance")
    float distance;

    @SerializedName("run_amount")
    float runAmount;

    @SerializedName("run_duration")
    String runDuration;

    @SerializedName("no_of_steps")
    float numSteps;

    @SerializedName("avg_speed")
    float avgSpeed;

    @SerializedName("is_flag")
    boolean is_flag = false;

    @SerializedName("client_run_id")
    String clientRunId;

    @SerializedName("start_location_lat")
    double startLocationLat;

    @SerializedName("start_location_long")
    double startLocationLong;

    @SerializedName("end_location_lat")
    double endLocationLat;

    @SerializedName("end_location_long")
    double endLocationLong;

    @SerializedName("version")
    long version;

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

    public Date getDate() {
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

    public String getClientRunId() {
        return clientRunId;
    }

    public void setClientRunId(String clientRunId) {
        this.clientRunId = clientRunId;
    }

    public double getStartLocationLat() {
        return startLocationLat;
    }

    public void setStartLocationLat(double startLocationLat) {
        this.startLocationLat = startLocationLat;
    }

    public double getStartLocationLong() {
        return startLocationLong;
    }

    public void setStartLocationLong(double startLocationLong) {
        this.startLocationLong = startLocationLong;
    }

    public double getEndLocationLat() {
        return endLocationLat;
    }

    public void setEndLocationLat(double endLocationLat) {
        this.endLocationLat = endLocationLat;
    }

    public double getEndLocationLong() {
        return endLocationLong;
    }

    public void setEndLocationLong(double endLocationLong) {
        this.endLocationLong = endLocationLong;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public float getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(float numSteps) {
        this.numSteps = numSteps;
    }

    public Long getStartTimeEpoch() {
        return startTimeEpoch;
    }

    public void setStartTimeEpoch(Long startTimeEpoch) {
        this.startTimeEpoch = startTimeEpoch;
    }

    public String getEndTimeEpoch() {
        return endTimeEpoch;
    }

    public void setEndTimeEpoch(String endTimeEpoch) {
        this.endTimeEpoch = endTimeEpoch;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Run{" +
                "id=" + id +
                ",\n\t startTime='" + startTime + '\'' +
                ",\n\t entTime='" + endTime + '\'' +
                ",\n\t distance=" + distance +
                ",\n\t runDuration='" + runDuration + '\'' +
                ",\n\t numSteps=" + numSteps +
                ",\n\t is_flag=" + is_flag +
                ",\n\t clientRunId='" + clientRunId + '\'' +
                ",\n\t version='" + version + '\'' +
                "\n}";
    }
}
