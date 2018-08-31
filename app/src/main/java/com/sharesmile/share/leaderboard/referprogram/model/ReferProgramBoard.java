package com.sharesmile.share.leaderboard.referprogram.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;

public class ReferProgramBoard implements UnObfuscable{

    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("meals_shared")
    private int mealsShared;

    @SerializedName("rank")
    private int rank;

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("social_thumb")
    private String socialThumb;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getMealsShared() {
        return mealsShared;
    }

    public void setMealsShared(int mealsShared) {
        this.mealsShared = mealsShared;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getSocialThumb() {
        return socialThumb;
    }

    public void setSocialThumb(String socialThumb) {
        this.socialThumb = socialThumb;
    }

    public BaseLeaderBoardItem getLeaderBoardDbObject()
    {
        String profilePicture = getProfilePicture();
        if(profilePicture.length()==0) {
            profilePicture = getSocialThumb();
        } else {
            profilePicture = Urls.getImpactProfileS3BucketUrl() + profilePicture;
        }
        BaseLeaderBoardItem lb = new BaseLeaderBoardItem(
                getUserId(), getUserName() , profilePicture,
                0, getRank(), getMealsShared());
        return lb;
    }
}
