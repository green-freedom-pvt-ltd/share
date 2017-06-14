package com.sharesmile.share.v8;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "WORKOUT".
 */
public class Workout {

    private Long id;
    private float distance;
    /** Not-null value. */
    private String elapsedTime;
    private Integer steps;
    private float recordedTime;
    private float avgSpeed;
    private String causeBrief;
    private java.util.Date date;
    private Float runAmount;
    private Boolean is_sync;
    private String workoutId;
    private Double startPointLatitude;
    private Double startPointLongitude;
    private Double endPointLatitude;
    private Double endPointLongitude;
    private Long beginTimeStamp;
    private Long endTimeStamp;
    private Boolean isValidRun;
    private Long version;
    private Double calories;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Workout() {
    }

    public Workout(Long id) {
        this.id = id;
    }

    public Workout(Long id, float distance, String elapsedTime, Integer steps, float recordedTime, float avgSpeed, String causeBrief, java.util.Date date, Float runAmount, Boolean is_sync, String workoutId, Double startPointLatitude, Double startPointLongitude, Double endPointLatitude, Double endPointLongitude, Long beginTimeStamp, Long endTimeStamp, Boolean isValidRun, Long version, Double calories) {
        this.id = id;
        this.distance = distance;
        this.elapsedTime = elapsedTime;
        this.steps = steps;
        this.recordedTime = recordedTime;
        this.avgSpeed = avgSpeed;
        this.causeBrief = causeBrief;
        this.date = date;
        this.runAmount = runAmount;
        this.is_sync = is_sync;
        this.workoutId = workoutId;
        this.startPointLatitude = startPointLatitude;
        this.startPointLongitude = startPointLongitude;
        this.endPointLatitude = endPointLatitude;
        this.endPointLongitude = endPointLongitude;
        this.beginTimeStamp = beginTimeStamp;
        this.endTimeStamp = endTimeStamp;
        this.isValidRun = isValidRun;
        this.version = version;
        this.calories = calories;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    /** Not-null value. */
    public String getElapsedTime() {
        return elapsedTime;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public float getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(float recordedTime) {
        this.recordedTime = recordedTime;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(float avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public String getCauseBrief() {
        return causeBrief;
    }

    public void setCauseBrief(String causeBrief) {
        this.causeBrief = causeBrief;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public Float getRunAmount() {
        return runAmount;
    }

    public void setRunAmount(Float runAmount) {
        this.runAmount = runAmount;
    }

    public Boolean getIs_sync() {
        return is_sync;
    }

    public void setIs_sync(Boolean is_sync) {
        this.is_sync = is_sync;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public Double getStartPointLatitude() {
        return startPointLatitude;
    }

    public void setStartPointLatitude(Double startPointLatitude) {
        this.startPointLatitude = startPointLatitude;
    }

    public Double getStartPointLongitude() {
        return startPointLongitude;
    }

    public void setStartPointLongitude(Double startPointLongitude) {
        this.startPointLongitude = startPointLongitude;
    }

    public Double getEndPointLatitude() {
        return endPointLatitude;
    }

    public void setEndPointLatitude(Double endPointLatitude) {
        this.endPointLatitude = endPointLatitude;
    }

    public Double getEndPointLongitude() {
        return endPointLongitude;
    }

    public void setEndPointLongitude(Double endPointLongitude) {
        this.endPointLongitude = endPointLongitude;
    }

    public Long getBeginTimeStamp() {
        return beginTimeStamp;
    }

    public void setBeginTimeStamp(Long beginTimeStamp) {
        this.beginTimeStamp = beginTimeStamp;
    }

    public Long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(Long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public Boolean getIsValidRun() {
        return isValidRun;
    }

    public void setIsValidRun(Boolean isValidRun) {
        this.isValidRun = isValidRun;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
