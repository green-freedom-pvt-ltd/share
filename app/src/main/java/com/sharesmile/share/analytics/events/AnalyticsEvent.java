package com.sharesmile.share.analytics.events;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.core.UnObfuscable;

import java.util.Collections;

public class AnalyticsEvent implements UnObfuscable {

    private static final String TAG = "AnalyticsEvent";

    //EVENT CATEGORIES
    public static final String CATEGORY_GENERAL = "GENERAL";
    public static final String CATEGORY_TRACKER = "TRACKER";
    public static final String CATEGORY_LOGIN = "LOGIN";
    public static final String CATEGORY_CAUSE = "CAUSE";
    public static final String CATEGORY_PROFILE = "PROFILE";
    public static final String CATEGORY_LEADERBOARD = "GLOBAL_LEADERBOARD";
    public static final String CATEGORY_VIGILANCE = "VIGILANCE";
    public static final String CATEGORY_WORKOUT = "WORKOUT";
    public static final String CATEGORY_SHARE = "SHARE";
    public static final String CATEGORY_SYNC = "SYNC";

    private Properties eventProperties;

    private AnalyticsEvent() {
        eventProperties = new Properties();
    }

    private AnalyticsEvent(Properties copyFrom){
        eventProperties = new Properties(copyFrom);
    }

    public Properties getProperties() {
        return eventProperties;
    }

    public String getEventName(){
        return String.valueOf(eventProperties.get(Properties.EVENT_NAME));
    }

    public static AnalyticsEvent copy(AnalyticsEvent event) {
        return new AnalyticsEvent(event.getProperties());
    }

    public static class Builder implements UnObfuscable {

        private Properties allProps;
        private CommonProperties.Builder commonPropsBuilder;

        public Builder(Context context) {
            allProps = new Properties();
            commonPropsBuilder = new CommonProperties.Builder(context);
        }

        public Builder event(Event event){
            commonPropsBuilder.eventName(event.toString());
            commonPropsBuilder.eventCategory(event.getCategory());
            return this;
        }

        public Builder put(String name, Object value){
            allProps.put(name, value);
            return this;
        }

        public Builder addBundle(Properties props){
            if (props != null){
                allProps.add(props);
            }
            return this;
        }

        public AnalyticsEvent build() {
            AnalyticsEvent event = new AnalyticsEvent();
            //Build Common Bundle and add it to event
            addBundle(commonPropsBuilder.build());
            event.eventProperties = allProps;
            return event;
        }

        public void buildAndDispatch(){
            AnalyticsEvent event = new AnalyticsEvent();
            //Build Common Bundle and add it to event
            addBundle(commonPropsBuilder.build());
            event.eventProperties = allProps;
            event.dispatch();
        }

    }

    public static Builder create(Event event){
        return new Builder(MainApplication.getContext()).event(event);
    }

    public String toJsonString() {
        return eventProperties.toJsonString();
    }

    public static AnalyticsEvent fromJsonString(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        Gson gson = new Gson();
        Properties props = gson.fromJson(jsonString, Properties.class);

        if (props == null || props.equals(Collections.emptyMap())) {
            return null;
        }

        return new AnalyticsEvent(props);
    }

    public void dispatch(){
        Analytics.getInstance().handleAnalyticsEvent(this);
    }

    @Override
    public String toString() {
        return "AnalyticsEvent{" +
                "eventName = " + getEventName() +
                "eventProperties = " + eventProperties.toJsonString() +
                '}';
    }
}


