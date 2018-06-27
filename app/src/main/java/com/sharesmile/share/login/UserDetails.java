package com.sharesmile.share.login;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.utils.Utils;

/**
 * Created by ankitmaheshwari on 4/24/17.
 */

public class UserDetails implements UnObfuscable {

    @SerializedName("user_id")
    private int userId;
    @SerializedName("auth_token")
    private String authToken;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("gender_user")
    private String genderUser;
    @SerializedName("email")
    private String email;
    @SerializedName("phone_number")
    private String phoneNumber;
    @SerializedName("address")
    private String address;
    @SerializedName("locality_user")
    private String locality_user;
    @SerializedName("city")
    private String city;
    @SerializedName("state")
    private String state;
    @SerializedName("country")
    private String country;
    @SerializedName("social_thumb")
    private String socialThumb;

    @SerializedName("profile_picture")
    private String profilePicture;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("total_amount")
    private TotalAmount totalAmount;
    @SerializedName("total_distance")
    private TotalDistance totalDistance;
    @SerializedName("sign_up")
    private boolean signUp;
    @SerializedName("team_code")
    private int teamId;
    @SerializedName("body_weight")
    private float bodyWeight;
    @SerializedName("body_height")
    private int bodyHeight;
    @SerializedName("body_height_unit")
    private int bodyHeightUnit; // 0=cms,1=inches

    //streak details
    @SerializedName("streak_count")
    private int streakCount;
    @SerializedName("streak_max_count")
    private int streakMaxCount;
    @SerializedName("streak_run_progress")
    private double streakRunProgress;
    @SerializedName("streak_current_date")
    private String streakCurrentDate;
    @SerializedName("streak_goal_distance")
    private double streakGoalDistance;
    @SerializedName("streak_goal_id")
    private int streakGoalID;
    @SerializedName("streak_added")
    private boolean streakAdded;

    @SerializedName("title1")
    private int title1;
    @SerializedName("title2")
    private int title2;


    public boolean isStreakAdded() {
        return streakAdded;
    }

    public void setStreakAdded(boolean streakAdded) {
        this.streakAdded = streakAdded;
    }

    public double getStreakGoalDistance() {
        return streakGoalDistance;
    }

    public void setStreakGoalDistance(double streakGoalDistance) {
        this.streakGoalDistance = streakGoalDistance;
    }

    public int getStreakGoalID() {
        return streakGoalID;
    }

    public void setStreakGoalID(int streakGoalID) {
        this.streakGoalID = streakGoalID;
    }

    public int getStreakMaxCount() {
        return streakMaxCount;
    }

    public void setStreakMaxCount(int streakMaxCount) {
        this.streakMaxCount = streakMaxCount;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void addStreakCount() {
        if (!isStreakAdded() && getStreakRunProgress() >= getStreakGoalDistance()) {
            this.streakCount += 1;
            setStreakAdded(true);
        }

        if(streakMaxCount<streakCount)
            setStreakMaxCount(streakCount);
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public double getStreakRunProgress() {
        return streakRunProgress;
    }

    public void addStreakRunProgress(double runDistance) {
        streakRunProgress += runDistance;
    }

    public void setStreakRunProgress(double streakRunProgress) {
        this.streakRunProgress = streakRunProgress;
    }

    public String getStreakCurrentDate() {
        return streakCurrentDate;
    }

    public void setStreakCurrentDate(String streakCurrentDate) {
        this.streakCurrentDate = streakCurrentDate;
    }

    public int getUserId() {
        return userId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return Utils.dedupName(getFirstName(), getLastName());
    }

    public String getGenderUser() {
        return genderUser;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getLocality_user() {
        return locality_user;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getSocialThumb() {
        return socialThumb;
    }

    public String getBirthday() {
        return birthday;
    }

    public int getTotalAmount() {
        return totalAmount.totalAmount;
    }

    public float getTotalDistance() {
        return totalDistance.totalDistance;
    }

    public boolean isSignUp() {
        return signUp;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGenderUser(String genderUser) {
        this.genderUser = genderUser;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocality_user(String locality_user) {
        this.locality_user = locality_user;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setSocialThumb(String socialThumb) {
        this.socialThumb = socialThumb;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getBodyHeightUnit() {
        return bodyHeightUnit;
    }

    public void setBodyHeightUnit(int bodyHeightUnit) {
        this.bodyHeightUnit = bodyHeightUnit;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = new TotalAmount();
        this.totalAmount.totalAmount = totalAmount;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = new TotalDistance();
        this.totalDistance.totalDistance = totalDistance;
    }

    public void setSignUp(boolean signUp) {
        this.signUp = signUp;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public float getBodyWeight() {
        return bodyWeight;
    }

    public void setBodyWeight(float bodyWeight) {
        this.bodyWeight = bodyWeight;
    }

    public int getBodyHeight() {
        return bodyHeight;
    }

    public void setBodyHeight(int bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    public static class TotalAmount {
        @SerializedName("total_amount")
        public int totalAmount;
    }

    public static class TotalDistance {
        @SerializedName("total_distance")
        public float totalDistance;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setTotalAmount(TotalAmount totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTotalDistance(TotalDistance totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTitle1() {
        return title1;
    }

    public void setTitle1(int title1) {
        this.title1 = title1;
    }

    public int getTitle2() {
        return title2;
    }

    public void setTitle2(int title2) {
        this.title2 = title2;
    }
}
