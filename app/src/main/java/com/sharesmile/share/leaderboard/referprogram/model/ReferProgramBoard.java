package com.sharesmile.share.leaderboard.referprogram.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;
import com.sharesmile.share.utils.Utils;

public class ReferProgramBoard implements UnObfuscable{

    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("meals_shared")
    private int mealsShared;

    @SerializedName("rank")
    private int rank;

    @SerializedName("profile_pic")
    private String profilePic;

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

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public BaseLeaderBoardItem getLeaderBoardDbObject()
    {
        BaseLeaderBoardItem lb = new BaseLeaderBoardItem(
                getUserId(), getUserName() , getProfilePic(),
                0, getRank(), getMealsShared());
        return lb;
    }
}
