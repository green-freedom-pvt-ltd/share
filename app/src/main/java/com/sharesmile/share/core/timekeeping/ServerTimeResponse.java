package com.sharesmile.share.core.timekeeping;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari on 6/9/17.
 */

public class ServerTimeResponse implements UnObfuscable, Serializable {

    @SerializedName("time_epoch")
    private long timeEpoch;
    @SerializedName("time_standard")
    private String timeStandard;

    public long getTimeEpoch() {
        return timeEpoch;
    }

    public String getTimeStandard() {
        return timeStandard;
    }
}


