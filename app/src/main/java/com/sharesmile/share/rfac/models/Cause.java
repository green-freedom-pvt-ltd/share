package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

import java.util.List;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */
public class Cause implements UnObfuscable {

    private static final String TAG = "Cause";

    @SerializedName("cause_id")
    private int id;
    @SerializedName("cause_title")
    private String title;

    private String category;

    private List<Partner> executors;

    private List<Sponsor> sponsors;

    @SerializedName("cause_brief")
    private String briefText;

    @SerializedName("cause_detail")
    private String detailText;

    @SerializedName("cause_image_url")
    private String imageUrl;

    @SerializedName("conversion_rate")
    private float conversionRate;

    @SerializedName("cause_is_active")
    private boolean isActive;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public List<Partner> getExecutors() {
        return executors;
    }

    public Partner getExecutor(){
        if (executors != null && !executors.isEmpty()){
            return executors.get(0);
        }
        return null;
    }

    public List<Sponsor> getSponsors() {
        return sponsors;
    }

    public Sponsor getSponsor(){
        if (sponsors != null && !sponsors.isEmpty()){
            return sponsors.get(0);
        }
        return null;
    }

    public float getConversionRate() {
        return conversionRate;
    }

    public String getBriefText() {
        return briefText;
    }

    public String getDetailText() {
        return detailText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActive() {
        return isActive;
    }
}
