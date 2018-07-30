package com.sharesmile.share.profile.badges.model;

import com.google.gson.annotations.SerializedName;

public class Title {
    @SerializedName("title_id")
    private int titleId;
    @SerializedName("title")
    private String title;
    @SerializedName("category_id")
    private int categoryId;
    @SerializedName("goal_n_stars")
    private int goalNStars;
    @SerializedName("image")
    private String image;
    @SerializedName("winning_message")
    private String winningMessage;
    @SerializedName("description")
    private String desc1;
    @SerializedName("description_2")
    private String desc2;
    @SerializedName("description_3")
    private String desc3;
    @SerializedName("share_title_content")
    private String shareMessage;

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getGoalNStars() {
        return goalNStars;
    }

    public void setGoalNStars(int goalNStars) {
        this.goalNStars = goalNStars;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getWinningMessage() {
        return winningMessage;
    }

    public void setWinningMessage(String winningMessage) {
        this.winningMessage = winningMessage;
    }

    public String getDesc1() {
        return desc1;
    }

    public void setDesc1(String desc1) {
        this.desc1 = desc1;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public String getDesc3() {
        return desc3;
    }

    public void setDesc3(String desc3) {
        this.desc3 = desc3;
    }

    public String getShareMessage() {
        return shareMessage;
    }

    public void setShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
    }

    @Override
    public String toString() {
        return "titleId : "+titleId+
                ",title : "+title+
                ",categoryId : "+categoryId+
                ",goalNStars : "+goalNStars+
                ",image : "+image+
                ",winningMessage : "+winningMessage+
                ",desc1 : "+desc1+
                ",desc2 : "+desc2+
                ",desc3 : "+desc3+
                ",shareMessage : "+shareMessage
                ;
    }
}
