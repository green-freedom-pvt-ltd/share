package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;

/**
 * Created by piyush on 9/9/16.
 */
public class LastWeekDistance implements UnObfuscable, Serializable {

    @SerializedName("last_week_distance")
    private float lastWeekDistance;

    public float getLastWeekDistance() {
        return lastWeekDistance;
    }

    public void setLastWeekDistance(float lastWeekDistance) {
        this.lastWeekDistance = lastWeekDistance;
    }
}
