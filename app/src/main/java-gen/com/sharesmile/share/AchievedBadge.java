package com.sharesmile.share;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "ACHIEVED_BADGE".
 */
public class AchievedBadge {

    private Long id;
    private long serverId;
    private long userId;
    private long causeId;
    /** Not-null value. */
    private String causeName;
    private long badgeIdInProgress;
    private long badgeIdAchieved;
    private java.util.Date badgeIdAchievedDate;
    private Integer noOfStarAchieved;
    /** Not-null value. */
    private String badgeType;
    private long category;
    /** Not-null value. */
    private String categoryStatus;
    private double paramDone;
    private boolean isSync;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public AchievedBadge() {
    }

    public AchievedBadge(Long id) {
        this.id = id;
    }

    public AchievedBadge(Long id, long serverId, long userId, long causeId, String causeName, long badgeIdInProgress, long badgeIdAchieved, java.util.Date badgeIdAchievedDate, Integer noOfStarAchieved, String badgeType, long category, String categoryStatus, double paramDone, boolean isSync) {
        this.id = id;
        this.serverId = serverId;
        this.userId = userId;
        this.causeId = causeId;
        this.causeName = causeName;
        this.badgeIdInProgress = badgeIdInProgress;
        this.badgeIdAchieved = badgeIdAchieved;
        this.badgeIdAchievedDate = badgeIdAchievedDate;
        this.noOfStarAchieved = noOfStarAchieved;
        this.badgeType = badgeType;
        this.category = category;
        this.categoryStatus = categoryStatus;
        this.paramDone = paramDone;
        this.isSync = isSync;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCauseId() {
        return causeId;
    }

    public void setCauseId(long causeId) {
        this.causeId = causeId;
    }

    /** Not-null value. */
    public String getCauseName() {
        return causeName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCauseName(String causeName) {
        this.causeName = causeName;
    }

    public long getBadgeIdInProgress() {
        return badgeIdInProgress;
    }

    public void setBadgeIdInProgress(long badgeIdInProgress) {
        this.badgeIdInProgress = badgeIdInProgress;
    }

    public long getBadgeIdAchieved() {
        return badgeIdAchieved;
    }

    public void setBadgeIdAchieved(long badgeIdAchieved) {
        this.badgeIdAchieved = badgeIdAchieved;
    }

    public java.util.Date getBadgeIdAchievedDate() {
        return badgeIdAchievedDate;
    }

    public void setBadgeIdAchievedDate(java.util.Date badgeIdAchievedDate) {
        this.badgeIdAchievedDate = badgeIdAchievedDate;
    }

    public Integer getNoOfStarAchieved() {
        return noOfStarAchieved;
    }

    public void setNoOfStarAchieved(Integer noOfStarAchieved) {
        this.noOfStarAchieved = noOfStarAchieved;
    }

    /** Not-null value. */
    public String getBadgeType() {
        return badgeType;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setBadgeType(String badgeType) {
        this.badgeType = badgeType;
    }

    public long getCategory() {
        return category;
    }

    public void setCategory(long category) {
        this.category = category;
    }

    /** Not-null value. */
    public String getCategoryStatus() {
        return categoryStatus;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCategoryStatus(String categoryStatus) {
        this.categoryStatus = categoryStatus;
    }

    public double getParamDone() {
        return paramDone;
    }

    public void setParamDone(double paramDone) {
        this.paramDone = paramDone;
    }

    public boolean getIsSync() {
        return isSync;
    }

    public void setIsSync(boolean isSync) {
        this.isSync = isSync;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
