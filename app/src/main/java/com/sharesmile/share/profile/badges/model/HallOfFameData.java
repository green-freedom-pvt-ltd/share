package com.sharesmile.share.profile.badges.model;

public class HallOfFameData {
    private int badgeId;
    private int count;
    private String badgeType;

    public HallOfFameData(int badgeId, int count, String badgeType) {
        this.badgeId = badgeId;
        this.count = count;
        this.badgeType = badgeType;
    }

    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getBadgeType() {
        return badgeType;
    }

    public void setBadgeType(String badgeType) {
        this.badgeType = badgeType;
    }
}
