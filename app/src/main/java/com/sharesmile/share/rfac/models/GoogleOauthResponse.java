package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

/**
 * Created by Shine on 19/07/16.
 */
public class GoogleOauthResponse implements UnObfuscable {

    @SerializedName("access_token")
    public String access_token;

    @SerializedName("token_type")
    public String token_type;

    @SerializedName("expires_in")
    public String expires_in;

    @SerializedName("id_token")
    public String id_token;

}
