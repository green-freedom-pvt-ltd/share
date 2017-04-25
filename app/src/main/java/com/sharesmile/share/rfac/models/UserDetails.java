package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari on 4/24/17.
 */

public class UserDetails implements UnObfuscable{

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
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("total_amount")
    private TotalAmount totalAmount;
    @SerializedName("total_distance")
    private TotalDistance totalDistance;
    @SerializedName("sign_up")
    private boolean signUp;
    @SerializedName("team_code")
    private int teamCode;


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

    public int getTeamCode() {
        return teamCode;
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

    public void setTeamCode(int teamCode) {
        this.teamCode = teamCode;
    }

    public static class TotalAmount{
        @SerializedName("total_amount")
        public int totalAmount;
    }

    public static class TotalDistance{
        @SerializedName("total_distance")
        public float totalDistance;
    }
}
