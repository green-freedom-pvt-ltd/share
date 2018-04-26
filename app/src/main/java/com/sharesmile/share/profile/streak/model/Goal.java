package com.sharesmile.share.profile.streak.model;

import com.google.gson.annotations.SerializedName;

public class Goal {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("value")
    private int value;
    @SerializedName("iconCount")
    private int iconCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getIconCount() {
        return iconCount;
    }

    public void setIconCount(int iconCount) {
        this.iconCount = iconCount;
    }
}
