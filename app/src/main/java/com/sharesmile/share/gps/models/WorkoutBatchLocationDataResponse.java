package com.sharesmile.share.gps.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ankitmaheshwari on 9/15/17.
 */

public class WorkoutBatchLocationDataResponse {

    @SerializedName("run_location_id")
    int runLocationId;

    public int getRunLocationId() {
        return runLocationId;
    }

    public void setRunLocationId(int runLocationId) {
        this.runLocationId = runLocationId;
    }
}
