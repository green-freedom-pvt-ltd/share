package com.sharesmile.share.events.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari1 on 27/12/15.
 */
public class Event implements UnObfuscable {

    private static final String TAG = "Event";

    @SerializedName("event_id")
    private int id;
    @SerializedName("event_name")
    private String name;

    @SerializedName("event_locality")
    private String locality;

    private String category;
    private String email;

    private String address;
    private String city;
    private String state;
    private String country;

    @SerializedName("event_organiser")
    private String organiser;

    @SerializedName("event_date")
    private String date;

    @SerializedName("event_image_url")
    private String imageUrl;

    @SerializedName("no_volunteers_expected")
    private int noVolunteersExpected;

    @SerializedName("no_volunteers_turned_up")
    private int noVolunteersTurnedUp;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("date_registered")
    private String dateRegistered;

    @SerializedName("is_active")
    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public String getDateRegistered() {
        return dateRegistered;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getNoVolunteersTurnedUp() {
        return noVolunteersTurnedUp;
    }

    public int getNoVolunteersExpected() {
        return noVolunteersExpected;
    }

    public String getDate() {
        return date;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getOrganiser(){
        return organiser;
    }

    public String getLocality(){
        return locality;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
