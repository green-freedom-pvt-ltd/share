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
    @SerializedName("badgeParameter")
    private String badgeParameter;
    @SerializedName("badgeParameterCheck")
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

    public String getBadgeParameter() {
        return badgeParameter;
    }

    public void setBadgeParameter(String badgeParameter) {
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
}
