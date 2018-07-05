package com.sharesmile.share.profile.model;

import java.util.ArrayList;

public class CauseStats {

    private String causeName;
    private int cause_raised;
    private int cause_workouts;
    private int cause_no_of_stars;
    private String cause_image_url;
    private ArrayList<String> sponsors;
    private ArrayList<String> partners;

    public String getCauseName() {
        return causeName;
    }

    public void setCauseName(String causeName) {
        this.causeName = causeName;
    }

    public int getCause_raised() {
        return cause_raised;
    }

    public void setCause_raised(int cause_raised) {
        this.cause_raised = cause_raised;
    }

    public int getCause_workouts() {
        return cause_workouts;
    }

    public void setCause_workouts(int cause_workouts) {
        this.cause_workouts = cause_workouts;
    }

    public String getCause_image_url() {
        return cause_image_url;
    }

    public void setCause_image_url(String cause_image_url) {
        this.cause_image_url = cause_image_url;
    }

    public int getCause_no_of_stars() {
        return cause_no_of_stars;
    }

    public void setCause_no_of_stars(int cause_no_of_stars) {
        this.cause_no_of_stars = cause_no_of_stars;
    }

    public ArrayList<String> getSponsors() {
        return sponsors;
    }

    public void setSponsors(ArrayList<String> sponsors) {
        this.sponsors = sponsors;
    }

    public ArrayList<String> getPartners() {
        return partners;
    }

    public void setPartners(ArrayList<String> partners) {
        this.partners = partners;
    }
}
