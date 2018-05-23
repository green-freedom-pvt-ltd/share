package com.sharesmile.share.profile.badges.model;

public class HallOfFameData {
    private int badgeId;
    private int count;

    public HallOfFameData(int badgeId, int count) {
        this.badgeId = badgeId;
        this.count = count;
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
}
