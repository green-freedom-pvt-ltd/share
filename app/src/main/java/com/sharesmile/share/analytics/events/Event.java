package com.sharesmile.share.analytics.events;

/**
 * Created by ankitm on 11/04/16.
 */
public enum Event {

    SESSION_START(AnalyticsEvent.CATEGORY_GENERAL),
    FIRST_LAUNCH_AFTER_INSTALL(AnalyticsEvent.CATEGORY_GENERAL),
    LAUNCH_APP(AnalyticsEvent.CATEGORY_GENERAL),
    ON_LOAD_LOGIN_SCREEN(AnalyticsEvent.CATEGORY_LOGIN),
    ON_CLICK_LOGIN_SKIP(AnalyticsEvent.CATEGORY_LOGIN),

    ON_LOAD_CAUSE_SELECTION(AnalyticsEvent.CATEGORY_CAUSE),
    ON_CLICK_LETS_GO(AnalyticsEvent.CATEGORY_CAUSE),
    ON_CLICK_CAUSE_CARD(AnalyticsEvent.CATEGORY_CAUSE),


    ON_CLICK_LOGIN_BUTTON(AnalyticsEvent.CATEGORY_LOGIN);

    private String category;

    Event(String category){
        this.category = category;
    }

    public String getCategory(){
        return category;
    }

}

