package com.sharesmile.share.profile.badges.model;

import com.google.gson.annotations.SerializedName;

public class Badge {

    @SerializedName("badge_id")
    private Integer badgeId;
    @SerializedName("badge_type")
    private String type;
    @SerializedName("badge_category")
    private String category;
    @SerializedName("badge_name")
    private String name;
    @SerializedName("stars_count")
    private Integer noOfStars;
    @SerializedName("badge_image")
    private String imageUrl;
    @SerializedName("description1")
    private String description1;
    @SerializedName("description2")
    private String description2;
    @SerializedName("description3")
    private String description3;
    @SerializedName("description_inprogress")
    private String descriptionInProgress;
    @SerializedName("share_badge_content")
    private String shareBadgeContent;
    @SerializedName("badge_parameter")
    private double badgeParameter;
    @SerializedName("badge_parameter_check")
    private String badgeParameterCheck;

    public Integer getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Integer badgeId) {
        this.badgeId = badgeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNoOfStars() {
        return noOfStars;
    }

    public void setNoOfStars(Integer noOfStars) {
        this.noOfStars = noOfStars;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription1() {
        return description1;
    }

    public void setDescription1(String description1) {
        this.description1 = description1;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    public double getBadgeParameter() {
        return badgeParameter;
    }

    public void setBadgeParameter(double badgeParameter) {
        this.badgeParameter = badgeParameter;
    }

    public String getBadgeParameterCheck() {
        return badgeParameterCheck;
    }

    public void setBadgeParameterCheck(String badgeParameterCheck) {
        this.badgeParameterCheck = badgeParameterCheck;
    }

    public String getDescription3() {
        return description3;
    }

    public void setDescription3(String description3) {
        this.description3 = description3;
    }

    public String getDescriptionInProgress() {
        return descriptionInProgress;
    }

    public void setDescriptionInProgress(String descriptionInProgress) {
        this.descriptionInProgress = descriptionInProgress;
    }

    public String getShareBadgeContent() {
        return shareBadgeContent;
    }

    public void setShareBadgeContent(String shareBadgeContent) {
        this.shareBadgeContent = shareBadgeContent;
    }
}
