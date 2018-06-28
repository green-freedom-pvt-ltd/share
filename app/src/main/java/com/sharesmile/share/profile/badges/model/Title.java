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
    @SerializedName("desc")
    private String desc;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "titleId : "+titleId+
                ",title : "+title+
                ",categoryId : "+categoryId+
                ",goalNStars : "+goalNStars+
                ",image : "+image+
                ",winningMessage : "+winningMessage+
                ",desc : "+desc
                ;
    }
}
