package com.sharesmile.share.leaderboard.global.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;
import com.sharesmile.share.utils.Utils;

import java.io.Serializable;

/**
 * Created by piyush on 9/6/16.
 */
public class LeaderBoardData implements UnObfuscable, Serializable{

    @SerializedName("user_id")
    private long userid;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("social_thumb")
    private String imageUrl;

    @SerializedName("profile_picture")
    private String profilePictureUrl;

    @SerializedName("distance")
    private float distance;

    @SerializedName("ranking")
    private int rank;

    @SerializedName("amount")
    private float amount;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getImageUrl() {
        if(profilePictureUrl!=null && profilePictureUrl.length()>0)
            return Urls.getImpactProfileS3BucketUrl()+profilePictureUrl;
        else
        return imageUrl;
    }

    public float getDistance() {
        return distance;
    }

    public long getUserid() {
        return userid;
    }

    public int getRank() {
        return rank;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public BaseLeaderBoardItem getLeaderBoardDbObject()
    {
        BaseLeaderBoardItem lb = new BaseLeaderBoardItem(
                getUserid(), Utils.dedupName(getFirstName(), getLastName()) , getImageUrl(),
                getDistance(), getRank(), getAmount());
        return lb;
    }

}
