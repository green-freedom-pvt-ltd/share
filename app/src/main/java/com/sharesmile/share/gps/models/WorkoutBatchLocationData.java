package com.sharesmile.share.gps.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ankitmaheshwari on 8/23/17.
 */

public class WorkoutBatchLocationData implements UnObfuscable, Serializable {

    @SerializedName("run_id")
    private long runId;

    @SerializedName("client_run_id")
    private String clientRunId;

    @SerializedName("batch_num")
    private int batchNum;

    @SerializedName("start_time_epoch")
    private long startTimeEpoch;

    @SerializedName("end_time_epoch")
    private long endTimeEpoch;

    @SerializedName("location_array")
    private List<WorkoutPoint> locationArray;

    public long getRunId() {
        return runId;
    }

    public void setRunId(long runId) {
        this.runId = runId;
    }

    public String getClientRunId() {
        return clientRunId;
    }

    public void setClientRunId(String clientRunId) {
        this.clientRunId = clientRunId;
    }

    public int getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(int batchNum) {
        this.batchNum = batchNum;
    }

    public long getStartTimeEpoch() {
        return startTimeEpoch;
    }

    public void setStartTimeEpoch(long startTimeEpoch) {
        this.startTimeEpoch = startTimeEpoch;
    }

    public long getEndTimeEpoch() {
        return endTimeEpoch;
    }

    public void setEndTimeEpoch(long endTimeEpoch) {
        this.endTimeEpoch = endTimeEpoch;
    }

    public List<WorkoutPoint> getLocationArray() {
        return locationArray;
    }

    public void setLocationArray(List<WorkoutPoint> locationArray) {
        this.locationArray = locationArray;
    }
}
