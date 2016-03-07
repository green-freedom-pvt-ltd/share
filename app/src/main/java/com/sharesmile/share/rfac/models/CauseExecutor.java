package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */
public class CauseExecutor implements UnObfuscable {

    private static final String TAG = "CauseExecutor";

    @SerializedName("executor_type")
    private String type;

    @SerializedName("executor_id")
    private int id;

    @SerializedName("executor_name")
    private String name;

    @SerializedName("executor_contact_email")
    private String email;

    @SerializedName("executor_phone_number")
    private String phoneNumber;

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
