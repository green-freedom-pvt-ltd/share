package com.sharesmile.share.profile.model;

import java.util.ArrayList;

public class CategoryStats {

    private String categoryName;
    private int categoryRaised;
    private int categoryWorkouts;
    private int categoryNoOfStars;
    ArrayList<CauseStats> causeStats;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryRaised() {
        return categoryRaised;
    }

    public void setCategoryRaised(int categoryRaised) {
        this.categoryRaised = categoryRaised;
    }

    public int getCategoryWorkouts() {
        return categoryWorkouts;
    }

    public void setCategoryWorkouts(int categoryWorkouts) {
        this.categoryWorkouts = categoryWorkouts;
    }

    public ArrayList<CauseStats> getCauseStats() {
        return causeStats;
    }

    public void setCauseStats(ArrayList<CauseStats> causeStats) {
        this.causeStats = causeStats;
    }

    public int getCategoryNoOfStars() {
        return categoryNoOfStars;
    }

    public void setCategoryNoOfStars(int categoryNoOfStars) {
        this.categoryNoOfStars += categoryNoOfStars;
    }
}
