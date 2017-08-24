package com.sharesmile.share.analytics.events;

import com.sharesmile.share.analytics.Analytics;

/**
 * Created by ankitm on 11/04/16.
 */
public enum Event {

    SESSION_START(AnalyticsEvent.CATEGORY_GENERAL),
    FIRST_LAUNCH_AFTER_INSTALL(AnalyticsEvent.CATEGORY_GENERAL),
    LAUNCH_APP(AnalyticsEvent.CATEGORY_GENERAL),
    ON_DRAWER_OPEN(AnalyticsEvent.CATEGORY_GENERAL),

    ON_LOAD_LOGIN_SCREEN(AnalyticsEvent.CATEGORY_LOGIN),
    ON_CLICK_LOGIN_SKIP(AnalyticsEvent.CATEGORY_LOGIN),
    ON_CLICK_LOGIN_BUTTON(AnalyticsEvent.CATEGORY_LOGIN),
    ON_LOGIN_SUCCESS(AnalyticsEvent.CATEGORY_LOGIN),
    ON_LOGIN_FAILED(AnalyticsEvent.CATEGORY_LOGIN),

    ON_LOAD_CAUSE_SELECTION(AnalyticsEvent.CATEGORY_CAUSE),
    ON_CLICK_LETS_GO(AnalyticsEvent.CATEGORY_CAUSE),
    ON_CLICK_CAUSE_CARD(AnalyticsEvent.CATEGORY_CAUSE),
    ON_LOAD_CAUSE_DETAILS(AnalyticsEvent.CATEGORY_CAUSE),

    ACTIVITY_DETECTOR_RESET(AnalyticsEvent.CATEGORY_VIGILANCE),
    ACTIVITY_RCOGNIZED_IN_VEHICLE(AnalyticsEvent.CATEGORY_VIGILANCE),
    ON_USAIN_BOLT_ALERT(AnalyticsEvent.CATEGORY_VIGILANCE),
    ON_POTENTIAL_USAIN_BOLT_MISSED(AnalyticsEvent.CATEGORY_VIGILANCE),
    DISP_YOU_ARE_DRIVING_NOTIF(AnalyticsEvent.CATEGORY_VIGILANCE),
    DISP_YOU_ARE_STILL_NOTIF(AnalyticsEvent.CATEGORY_VIGILANCE),
    DISP_WALK_ENGAGEMENT_NOTIF(AnalyticsEvent.CATEGORY_TRACKER),
    DISMISS_WALK_ENGAGEMENT_NOTIF(AnalyticsEvent.CATEGORY_TRACKER),
    DISP_GPS_NOT_ACTIVE_NOTIF(AnalyticsEvent.CATEGORY_TRACKER),
    DISP_BAD_GPS_NOTIF(AnalyticsEvent.CATEGORY_TRACKER),
    DETECTED_GPS_SPIKE(AnalyticsEvent.CATEGORY_VIGILANCE),

    ON_LOAD_TRACKER_SCREEN(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_CLICK_BEGIN_RUN(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_LOAD_COUNTDOWN_SCREEN(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_SKIP_COUNTDOWN(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_CLICK_RESUME_RUN(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_CLICK_STOP_RUN(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_WORKOUT_START(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_APPLICATION_CREATE(AnalyticsEvent.CATEGORY_GENERAL),
    ON_APP_UPDATE(AnalyticsEvent.CATEGORY_GENERAL),
    ON_WORKOUT_END(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_WORKOUT_PAUSE(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_WORKOUT_UPDATE(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_WORKOUT_COMPLETE(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_WORKOUT_RESUME(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_LOAD_FINISH_RUN_POPUP(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_LOAD_DISBALE_MOCK_LOCATION(AnalyticsEvent.CATEGORY_TRACKER),
    ON_LOAD_USAIN_BOLT_FORCE_EXIT(AnalyticsEvent.CATEGORY_TRACKER),
    ON_LOAD_TOO_SHORT_POPUP(AnalyticsEvent.CATEGORY_WORKOUT),

    ON_LOAD_GPS_INACTIVE_POPUP(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_LOAD_GPS_WEAK_POPUP(AnalyticsEvent.CATEGORY_WORKOUT),


    ON_LOAD_HAPPY_SAD_POPUP(AnalyticsEvent.CATEGORY_GENERAL),
    ON_LOAD_TAKE_FEEDBACK_POPUP(AnalyticsEvent.CATEGORY_GENERAL),
    ON_LOAD_RATE_US_POPUP(AnalyticsEvent.CATEGORY_GENERAL),
    ON_CLICK_HAPPY_WORKOUT(AnalyticsEvent.CATEGORY_GENERAL),
    ON_CLICK_SAD_WORKOUT(AnalyticsEvent.CATEGORY_GENERAL),
    ON_SUBMIT_FEEDBACK(AnalyticsEvent.CATEGORY_GENERAL),
    ON_CLICK_RATE_US(AnalyticsEvent.CATEGORY_GENERAL),
    ON_CLICK_WORKOUT_SHARE(AnalyticsEvent.CATEGORY_GENERAL),
    ON_CLICK_GIVE_FEEDBACK_BTN(AnalyticsEvent.CATEGORY_GENERAL),
    ON_CLICK_PROFILE_SHARE(AnalyticsEvent.CATEGORY_PROFILE),

    ON_RUN_SYNC(AnalyticsEvent.CATEGORY_SYNC),
    ON_LOCATION_DATA_SYNC(AnalyticsEvent.CATEGORY_SYNC),
    ON_EXCEPTION_IN_SYNC_TASK(AnalyticsEvent.CATEGORY_SYNC),
    ON_FORCE_REFRESH_FAILURE(AnalyticsEvent.CATEGORY_SYNC),
    ON_START_LOCATION_AFTER_RESUME(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_CLICK_PAUSE_RUN(AnalyticsEvent.CATEGORY_WORKOUT),
    ON_LOAD_SHARE_SCREEN(AnalyticsEvent.CATEGORY_SHARE),
    ON_LOAD_WEIGHT_INPUT_DIALOG(AnalyticsEvent.CATEGORY_PROFILE),
    ON_SET_BODY_WEIGHT(AnalyticsEvent.CATEGORY_PROFILE),
    ON_SET_PHONE_NUM(AnalyticsEvent.CATEGORY_PROFILE),
    ON_SET_BIRTTHDAY(AnalyticsEvent.CATEGORY_PROFILE),
    ON_SET_NAME(AnalyticsEvent.CATEGORY_PROFILE),
    ON_CLICK_SELF_TEAM_LEAGUE_BOARD(AnalyticsEvent.CATEGORY_LEAGUE),
    ON_CLICK_OTHER_TEAM_LEAGUE_BOARD(AnalyticsEvent.CATEGORY_LEAGUE),
    ON_CLICK_IMPACT_LEAGUE_NAVIGATION_MENU(AnalyticsEvent.CATEGORY_LEAGUE),
    ON_CLICK_CUP_ICON(AnalyticsEvent.CATEGORY_LEAGUE);

    private String category;

    Event(String category){
        this.category = category;
    }

    public String getCategory(){
        return category;
    }


}

