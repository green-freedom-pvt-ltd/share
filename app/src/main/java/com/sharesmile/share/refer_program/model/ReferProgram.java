package com.sharesmile.share.refer_program.model;

import com.google.gson.annotations.SerializedName;

public class ReferProgram {
    @SerializedName("id")
    private int id;
    @SerializedName("program_name")
    private String programName;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("is_active")
    private String isActive;
    @SerializedName("banner_image")
    private String bannerImage;
    @SerializedName("referal_category")
    private String referalCategory;
    @SerializedName("incentive")
    private String incentive;
    @SerializedName("share_message")
    private String shareMessage;
    @SerializedName("share_message_2")
    private String shareMessage2;
    @SerializedName("sponsored_by")
    private String sponsoredBy;
    @SerializedName("referal_cause_category")
    private String referalCauseCategory;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public String getReferalCategory() {
        return referalCategory;
    }

    public void setReferalCategory(String referalCategory) {
        this.referalCategory = referalCategory;
    }

    public String getIncentive() {
        return incentive;
    }

    public void setIncentive(String incentive) {
        this.incentive = incentive;
    }

    public String getShareMessage() {
        return shareMessage;
    }

    public void setShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
    }

    public String getShareMessage2() {
        return shareMessage2;
    }

    public void setShareMessage2(String shareMessage2) {
        this.shareMessage2 = shareMessage2;
    }

    public String getSponsoredBy() {
        return sponsoredBy;
    }

    public void setSponsoredBy(String sponsoredBy) {
        this.sponsoredBy = sponsoredBy;
    }

    public String getReferalCauseCategory() {
        return referalCauseCategory;
    }

    public void setReferalCauseCategory(String referalCauseCategory) {
        this.referalCauseCategory = referalCauseCategory;
    }
}
