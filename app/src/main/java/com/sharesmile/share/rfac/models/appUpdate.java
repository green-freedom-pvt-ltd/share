package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;

/**
 * Created by Shine on 21/09/16.
 */

public class AppUpdate implements Serializable, UnObfuscable {

    @SerializedName("app_version")
    public int app_version;

    @SerializedName("force_update")
    public boolean force_update = false;
}
