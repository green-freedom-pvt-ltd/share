package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Utils;

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

    @SerializedName("cause_id")
    int causeId;

    @SerializedName("start_time")
    String startTime;

    @SerializedName("start_time_epoch")
    Long startTimeEpoch;

    @SerializedName("end_time")
    String endTime;

    @SerializedName("end_time_epoch")
    Long endTimeEpoch;

    @SerializedName("distance")
    float distance; // In Kms

    @SerializedName("run_amount")
    float runAmount; // In Rupees

    @SerializedName("run_duration")
    String runDuration;

    @SerializedName("run_duration_epoch")
    Long runDurationEpoch;

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

    @SerializedName("calories_burnt")
    double calories;

    @SerializedName("team_id")
    int teamId;

    @SerializedName("num_spikes")
    int numSpikes;

    @SerializedName("num_updates")
    int numUpdates;

    @SerializedName("app_version")
    String appVersion ;

    @SerializedName("os_version")
    int osVersion;

    @SerializedName("device_id")
    String deviceId ;

    @SerializedName("device_name")
    String deviceName ;

    @SerializedName("estimated_steps")
    int estimatedSteps ;

    @SerializedName("estimated_distance")
    double estimatedDistance ;

    @SerializedName("estimated_calories")
    double estimatedCalories ;

    @SerializedName("google_fit_steps")
    int googleFitSteps ;

    @SerializedName("google_fit_distance")
    double googleFitDistance ;

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

    public int getCauseId() {
        return causeId;
    }

    public void setCauseId(int causeId) {
        this.causeId = causeId;
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

    public Long getEndTimeEpoch() {
        return endTimeEpoch;
    }

    public void setEndTimeEpoch(Long endTimeEpoch) {
        this.endTimeEpoch = endTimeEpoch;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public Long getRunDurationEpoch() {
        return runDurationEpoch;
    }

    public void setRunDurationEpoch(Long runDurationEpoch) {
        this.runDurationEpoch = runDurationEpoch;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getNumSpikes() {
        return numSpikes;
    }

    public void setNumSpikes(int numSpikes) {
        this.numSpikes = numSpikes;
    }

    public int getNumUpdates() {
        return numUpdates;
    }

    public void setNumUpdates(int numUpdates) {
        this.numUpdates = numUpdates;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(int osVersion) {
        this.osVersion = osVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getEstimatedSteps() {
        return estimatedSteps;
    }

    public void setEstimatedSteps(int estimatedSteps) {
        this.estimatedSteps = estimatedSteps;
    }

    public double getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public double getEstimatedCalories() {
        return estimatedCalories;
    }

    public void setEstimatedCalories(double estimatedCalories) {
        this.estimatedCalories = estimatedCalories;
    }

    public int getGoogleFitSteps() {
        return googleFitSteps;
    }

    public void setGoogleFitSteps(int googleFitSteps) {
        this.googleFitSteps = googleFitSteps;
    }

    public double getGoogleFitDistance() {
        return googleFitDistance;
    }

    public void setGoogleFitDistance(double googleFitDistance) {
        this.googleFitDistance = googleFitDistance;
    }

    public String extractRelevantInfoAsString(){
        return "Workout: " + Utils.formatWithOneDecimal(distance) + " km" + ", "
                + numSteps + " steps" + ", duration: " + runDuration;
    }

    @Override
    public String toString() {
        return "Run{" +
                "id=" + id +
                ",\n\t startTime='" + startTime + '\'' +
                ",\n\t entTime='" + endTime + '\'' +
                ",\n\t date='" + getDate() + '\'' +
                ",\n\t distance=" + distance +
                ",\n\t amount=" + runAmount +
                ",\n\t runDuration='" + runDuration + '\'' +
                ",\n\t avgSpeed='" + avgSpeed + '\'' +
                ",\n\t numSteps=" + numSteps +
                ",\n\t is_flag=" + is_flag +
                ",\n\t clientRunId='" + clientRunId + '\'' +
                ",\n\t version='" + version + '\'' +
                ",\n\t calories='" + calories + '\'' +
                ",\n\t startTimeEpoch='" + startTimeEpoch + '\'' +
                ",\n\t endTimeEpoch='" + endTimeEpoch + '\'' +
                ",\n\t startLocationLatitude='" + startLocationLat + '\'' +
                ",\n\t startLocationLongitude='" + startLocationLong + '\'' +
                ",\n\t endLocationLatitude='" + endLocationLat + '\'' +
                ",\n\t startLocationLongitude='" + endLocationLong + '\'' +
                ",\n\t teamId='" + teamId + '\'' +
                ",\n\t numSpikes='" + numSpikes + '\'' +
                ",\n\t numUpdates='" + numUpdates + '\'' +
                ",\n\t appVersion='" + appVersion + '\'' +
                ",\n\t osVersion='" + osVersion + '\'' +
                ",\n\t deviceId='" + deviceId + '\'' +
                ",\n\t deviceName='" + deviceName + '\'' +
                "\n}";
    }
}
