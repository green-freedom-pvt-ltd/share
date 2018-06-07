package com.sharesmile.share.profile.badges.model;

import com.sharesmile.share.AchievedBadge;

public class AchievedBadgeCount {
    long count;
    long achievedBadgeId;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getAchievedBadgeId() {
        return achievedBadgeId;
    }

    public void setAchievedBadgeId(long achievedBadgeId) {
        this.achievedBadgeId = achievedBadgeId;
    }
}
