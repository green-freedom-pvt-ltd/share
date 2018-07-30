package com.sharesmile.share.core.cause.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.base.UnObfuscable;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Created by Shine on 01/05/16..
 */
public class CauseData implements UnObfuscable, Serializable {

    private static final String TAG = "Cause";

    @SerializedName("pk")
    private long id;
    @SerializedName("cause_title")
    private String title;

    @SerializedName("cause_category")
    private String category;

    @SerializedName("cause_category_id")
    private long categoryId;

    @SerializedName("partners")
    private List<Partner> executors;

    @SerializedName("sponsors")
    private List<Sponsor> sponsors;

    @SerializedName("cause_description")
    private String causeDescription;

    @SerializedName("cause_brief")
    private String detailText;

    @SerializedName("cause_image")
    private String imageUrl;

    @SerializedName("conversion_rate")
    private float conversionRate;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("cause_share_message_template")
    private String causeShareMessageTemplate;

    @SerializedName("min_distance")
    private int minDistance;

    @SerializedName("app_update")
    private ApplicationUpdate applicationUpdate;

    @SerializedName("order_priority")
    private int orderPriority;

    @SerializedName("amount")
    private float targetAmount;

    @SerializedName("amount_raised")
    private float amountRaised;

    @SerializedName("total_runs")
    private int totalRuns;

    @SerializedName("is_completed")
    private boolean isCompleted;

    @SerializedName("cause_completed_image")
    private String causeCompletedImage;

    @SerializedName("cause_completed_description_image")
    private String causeCompletedDescriptionImage;

    @SerializedName("cause_completed_share_message_template")
    private String causeCompletedShareMessageTemplate;

    @SerializedName("cause_completed_report")
    private String causeCompletedReport;

    @SerializedName("cause_thank_you_image_v2")
    private List<CauseImageData> causeImageDataList;

    public long getId() {
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

    public Partner getExecutor() {
        if (executors != null && !executors.isEmpty()) {
            return executors.get(0);
        }
        return null;
    }

    public List<Sponsor> getSponsors() {
        return sponsors;
    }

    public Sponsor getSponsor() {
        if (sponsors != null && !sponsors.isEmpty()) {
            return sponsors.get(0);
        }
        return null;
    }

    public float getConversionRate() {
        return conversionRate;
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

    public String getCauseShareMessageTemplate() {
        return causeShareMessageTemplate;
    }

    public String getCauseDescription() {
        return causeDescription;
    }

    public void setCauseShareMessageTemplate(String causeShareMessageTemplate) {
        this.causeShareMessageTemplate = causeShareMessageTemplate;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setExecutors(List<Partner> executors) {
        this.executors = executors;
    }

    public void setSponsors(List<Sponsor> sponsors) {
        this.sponsors = sponsors;
    }

    public void setCauseDescription(String causeDescription) {
        this.causeDescription = causeDescription;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setConversionRate(float conversionRate) {
        this.conversionRate = conversionRate;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Gets min distance in meters
     * @return
     */
    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public float getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }

    public float getAmountRaised() {
        return amountRaised;
    }

    public void setAmountRaised(int amountRaised) {
        this.amountRaised = amountRaised;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getCauseCompletedImage() {
        return causeCompletedImage;
    }

    public void setCauseCompletedImage(String causeCompletedImage) {
        this.causeCompletedImage = causeCompletedImage;
    }

    public String getCauseCompletedDescriptionImage() {
        return causeCompletedDescriptionImage;
    }

    public void setCauseCompletedDescriptionImage(String causeCompletedDescriptionImage) {
        this.causeCompletedDescriptionImage = causeCompletedDescriptionImage;
    }

    public String getCauseCompletedShareMessageTemplate() {
        return causeCompletedShareMessageTemplate;
    }

    public void setCauseCompletedShareMessageTemplate(String causeCompletedShareMessageTemplate) {
        this.causeCompletedShareMessageTemplate = causeCompletedShareMessageTemplate;
    }

    public String getCauseCompletedReport() {
        return causeCompletedReport;
    }

    public void setCauseCompletedReport(String causeCompletedReport) {
        this.causeCompletedReport = causeCompletedReport;
    }

    public List<CauseImageData> getCauseImageDataList() {
        return causeImageDataList;
    }

    public CauseImageData getRandomCauseImageData(){
        if (causeImageDataList != null){
            int size = causeImageDataList.size();
            if (size > 0){
                Random random = new Random();
                int randIndex = random.nextInt(size);
                return causeImageDataList.get(randIndex);
            }
        }
        return null;
    }

    public void setCauseImageDataList(List<CauseImageData> causeImageDataList) {
        this.causeImageDataList = causeImageDataList;
    }

    public ApplicationUpdate getApplicationUpdate() {
        return applicationUpdate;
    }

    public void setApplicationUpdate(ApplicationUpdate applicationUpdate) {
        this.applicationUpdate = applicationUpdate;
    }

    public int getOrderPriority() {
        return orderPriority;
    }

    public void setOrderPriority(int orderPriority) {
        this.orderPriority = orderPriority;
    }

    public Properties getCauseBundle(){
        int sponsorId = 0;
        if (getSponsor() != null){
            sponsorId = getSponsor().getId();
        }
        Properties p = new Properties();
        p.put("cause_id", getId());
        p.put("cause_title", getTitle());
        p.put("sponsor_id", sponsorId);
        return p;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
}
