package com.sharesmile.share.analytics;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.onesignal.OneSignal;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;

import java.util.HashMap;
import java.util.Map;

import io.smooch.core.User;

import static com.sharesmile.share.core.Constants.USER_PROP_AGE;
import static com.sharesmile.share.core.Constants.USER_PROP_EMAIL;
import static com.sharesmile.share.core.Constants.USER_PROP_GENDER;
import static com.sharesmile.share.core.Constants.USER_PROP_NAME;
import static com.sharesmile.share.core.Constants.USER_PROP_PHONE;
import static com.sharesmile.share.core.Constants.USER_PROP_TEAM_CODE;
import static com.sharesmile.share.core.Constants.USER_PROP_TITLE1;
import static com.sharesmile.share.core.Constants.USER_PROP_USER_ID;

/**
 * Created by ankitm on 11/04/16.
 */
public class Analytics {

    private static final String TAG = "Analytics";

    private static Analytics instance;
    private ClevertapManager clevertapManager;
    private Context context;

    private Analytics(Context ctx){
        clevertapManager = new ClevertapManager(ctx);
        context = ctx;
    }

    public static synchronized void initialize(Context appContext) {
        if (null == instance) {
            synchronized (Analytics.class) {
                if (null == instance) {
                    instance = new Analytics(appContext);
                }
            }
        }
    }

    public static Analytics getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "Analytics is not initialized, call initialize(applicationContext) " +
                            "static method first");
        }
        return instance;
    }

    /**
     Triggered when a client post an analytics event on EventBus
     */
    public void handleAnalyticsEvent(AnalyticsEvent event) {
        if (event == null) {
            //Someone is trolling, return silently
            return;
        }
        try{
            Logger.d(TAG, "handleAnalyticsEvent " + event.getEventName() + ":\n"
                    + event.toJsonString());
            clevertapManager.sendEvent(event);
        }catch (Exception e){
            String message = "Exception while handling " + event.getEventName();
            Logger.e(TAG, message, e);
            Crashlytics.log(message);
            Crashlytics.logException(e);
        }
    }

    public void setUserProperties(){
        UserDetails details = MainApplication.getInstance().getUserDetails();
        if (details != null){
            Logger.d(TAG, "setUserProperties");
            setUserName(details.getFullName());
            setUserId(details.getUserId());
            setUserEmail(details.getEmail());
            setUserPhone(details.getPhoneNumber());
            setUserGender(details.getGenderUser());
            setUserPhoto(details.getSocialThumb());
            setUserImpactLeagueTeamCode(details.getTeamId());
            setUserProperty("StepCounter",
                    SharedPrefsManager.getInstance().getString(Constants.PREF_TYPE_STEP_COUNTER));

            // Setting properties for smooch

            User.getCurrentUser().setFirstName(details.getFirstName());
            User.getCurrentUser().setLastName(details.getLastName());
            User.getCurrentUser().setEmail(details.getEmail());

            final Map<String, Object> customProperties = new HashMap<>();
            customProperties.put("userId", details.getUserId());
            customProperties.put("phoneNumber", details.getPhoneNumber());
            customProperties.put("gender", details.getGenderUser());
            customProperties.put("teamId", details.getTeamId());

            User.getCurrentUser().addProperties(customProperties);
        }
    }

    public void setUserProperty(String propertyName, Object value){
        clevertapManager.setUserProperty(propertyName, value);
        OneSignal.sendTag(propertyName, String.valueOf(value));

    }

    public void setUserName(String name){
        Crashlytics.setString(USER_PROP_NAME, name);
        clevertapManager.setUserProperty(USER_PROP_NAME, name);
        OneSignal.sendTag(USER_PROP_NAME, name);
    }

    public void setUserEmail(String email){
        Crashlytics.setString(USER_PROP_EMAIL, email);
        clevertapManager.setUserProperty(USER_PROP_EMAIL, email);
        OneSignal.sendTag(USER_PROP_EMAIL, email);
    }

    public void setUserId(int userId){
        Crashlytics.setUserIdentifier(String.valueOf(userId));
        Crashlytics.setInt(USER_PROP_USER_ID, userId);
        clevertapManager.setUserProperty("Identity", userId);
        OneSignal.sendTag(USER_PROP_USER_ID, String.valueOf(userId));
    }

    /**
     * Sets 10 digit phone number without country code
     * @param phone
     */
    public void setUserPhone(String phone){
        clevertapManager.setUserProperty(USER_PROP_PHONE, phone);
        OneSignal.sendTag(USER_PROP_PHONE, phone);
    }

    /**
     * Sets gender, "M" or "F"
     * @param gender
     */
    public void setUserGender(String gender){
        clevertapManager.setUserProperty(USER_PROP_GENDER, gender);
        OneSignal.sendTag(USER_PROP_GENDER, gender);
    }
    public void setUserTitle(String tag,String titleId){
        clevertapManager.setUserProperty(tag, titleId);
        OneSignal.sendTag(tag, titleId);
    }

    public void setUserImpactLeagueTeamCode(int teamCode){
        Crashlytics.setInt(USER_PROP_TEAM_CODE, teamCode);
        clevertapManager.setUserProperty(USER_PROP_TEAM_CODE, teamCode);
        OneSignal.sendTag(USER_PROP_TEAM_CODE, String.valueOf(teamCode));
    }

    public void setUserAge(int age){
        clevertapManager.setUserProperty(USER_PROP_AGE, age);
        OneSignal.sendTag(USER_PROP_AGE, String.valueOf(age));
    }

    public void setUserPhoto(String pictureUrl){
        clevertapManager.setUserProperty("Photo", pictureUrl);
    }

}
