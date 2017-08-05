package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.core.UnObfuscable;

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

    @SerializedName("distance")
    private float distance;

    @SerializedName("ranking")
    private int rank;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getImageUrl() {
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

    public LeaderBoard getLeaderBoardDbObject()
    {
        LeaderBoard lb = new LeaderBoard((long) getUserid(), getFirstName(), getLastName(),
                getImageUrl(), getDistance(), getRank());
        return lb;
    }

}
