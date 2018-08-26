package com.sharesmile.share.profile.streak.model;

import com.google.gson.annotations.SerializedName;

public class Streak {
    @SerializedName("user_id")
    private int userId;
    @SerializedName("max_streak")
    private int maxStreak;
    @SerializedName("current_streak")
    private int currentStreak;
    @SerializedName("current_streak_date")
    private String currentStreakDate;
    @SerializedName("streak_added")
    private boolean streakAdded;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public String getCurrentStreakDate() {
        return currentStreakDate;
    }

    public void setCurrentStreakDate(String currentStreakDate) {
        this.currentStreakDate = currentStreakDate;
    }

    public boolean isStreakAdded() {
        return streakAdded;
    }

    public void setStreakAdded(boolean streakAdded) {
        this.streakAdded = streakAdded;
    }
}
