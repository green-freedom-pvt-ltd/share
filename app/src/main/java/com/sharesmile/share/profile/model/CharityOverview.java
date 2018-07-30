package com.sharesmile.share.profile.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CharityOverview {


    @SerializedName("total_raised")
    private int totalRaised;
    @SerializedName("total_workouts")
    private int totalWorkouts;
    ArrayList<CategoryStats> categoryStats;

    public int getTotalRaised() {
        return totalRaised;
    }

    public void setTotalRaised(int totalRaised) {
        this.totalRaised = totalRaised;
    }

    public int getTotalWorkouts() {
        return totalWorkouts;
    }

    public void setTotalWorkouts(int totalWorkouts) {
        this.totalWorkouts = totalWorkouts;
    }

    public ArrayList<CategoryStats> getCategoryStats() {
        return categoryStats;
    }

    public void setCategoryStats(ArrayList<CategoryStats> categoryStats) {
        this.categoryStats = categoryStats;
    }

}

