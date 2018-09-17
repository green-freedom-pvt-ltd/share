package com.sharesmile.share.refer_program.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

public class ReferrerDetails implements UnObfuscable {

    @SerializedName("referrer_user_id")
    private int referalId;
    @SerializedName("referrer_name")
    private String referalName;
    @SerializedName("referrer_social_thumb")
    private String referrerSocialThumb;
    @SerializedName("referrer_profile_picture")
    private String referrerProfilePicture;

    public int getReferalId() {
        return referalId;
    }

    public void setReferalId(int referalId) {
        this.referalId = referalId;
    }

    public String getReferalName() {
        return referalName;
    }

    public void setReferalName(String referalName) {
        this.referalName = referalName;
    }

    public String getReferrerSocialThumb() {
        return referrerSocialThumb;
    }

    public void setReferrerSocialThumb(String referrerSocialThumb) {
        this.referrerSocialThumb = referrerSocialThumb;
    }

    public String getReferrerProfilePicture() {
        return referrerProfilePicture;
    }

    public void setReferrerProfilePicture(String referrerProfilePicture) {
        this.referrerProfilePicture = referrerProfilePicture;
    }

}
