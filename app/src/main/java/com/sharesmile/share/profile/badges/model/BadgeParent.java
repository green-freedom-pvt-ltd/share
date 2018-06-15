package com.sharesmile.share.profile.badges.model;

import com.google.gson.annotations.SerializedName;


import java.util.ArrayList;

public class BadgeParent {
    @SerializedName("cause_category_id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("cause_category_name")
    private String categoryName;

    @SerializedName("badges")
    private ArrayList<Badge> badges;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Badge> getBadges() {
        return badges;
    }

    public void setBadges(ArrayList<Badge> badges) {
        this.badges = badges;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
