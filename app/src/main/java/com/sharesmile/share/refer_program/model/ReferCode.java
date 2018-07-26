package com.sharesmile.share.refer_program.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

public class ReferCode implements UnObfuscable{
    @SerializedName("referrer_user_id")
    private int referrerUserId;
    @SerializedName("referrer_name")
    private int referrerName;
}
