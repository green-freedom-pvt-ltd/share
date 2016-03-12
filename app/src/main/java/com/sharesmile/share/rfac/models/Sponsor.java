package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari1 on 09/03/16.
 */
public class Sponsor implements UnObfuscable {

    private static final String TAG = "Sponsor";

    @SerializedName("sponsor_type")
    private String type;

    @SerializedName("sponsor_id")
    private int id;

    @SerializedName("sponsor_name")
    private String name;

    @SerializedName("sponsor_logo")
    private String logoUrl;

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}
