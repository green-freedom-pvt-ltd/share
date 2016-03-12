package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */
public class Partner implements UnObfuscable {

    private static final String TAG = "Partner";

    @SerializedName("partner_type")
    private String type;

    @SerializedName("partner_id")
    private int id;

    @SerializedName("partner_name")
    private String name;

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
