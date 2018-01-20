package com.sharesmile.share.core;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.rfac.models.FeedbackCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Level;

/**
 * Created by ankitmaheshwari1 on 20/02/16.
 */
public class Constants {

    private static final String TAG = "Constants";

    public static final String STATIC_GOOGLE_MAP_BASE_URL
            = "http://maps.googleapis.com/maps/api/staticmap?";

    public static final String STATIC_GOOGLE_MAP_COMMON_PARAMS
            = "&maptype=roadmap&format=jpg&zoom=16";

    public static final String STATIC_GOOGLE_MAP_API_KEY
            = "AIzaSyBmu7TqNk5IWt27NWR59RkZMsICBHdIX3U";

    public static final int CODE_LOCATION_SETTINGS_RESOLUTION = 101;

    public static final int CODE_REQUEST_LOCATION_PERMISSION = 102;

    public static final int CODE_REQUEST_WRITE_PERMISSION = 103;

    public static final int CODE_GOOGLE_PLAY_SERVICES_RESOLUTION = 104;

    public static final String WORKOUT_SERVICE_BROADCAST_ACTION = "com.sharesmile.share.gps.location";

    public static final String LOCATION_TRACKER_BROADCAST_ACTION = "com.sharesmile.share.gps.location.tracker";

    public static final String WORKOUT_SERVICE_BROADCAST_CATEGORY = "location_service_braodcast_category";

    public static final String LOCATION_TRACKER_BROADCAST_CATEGORY = "location_tracker_braodcast_category";

    public static final int BROADCAST_FIX_LOCATION_SETTINGS_CODE = 201;

    public static final int BROADCAST_WORKOUT_RESULT_CODE = 202;

    public static final int BROADCAST_WORKOUT_UPDATE_CODE = 203;

    public static final int BROADCAST_UNBIND_SERVICE_CODE = 204;

    public static final int BROADCAST_STOP_WORKOUT_CODE = 205;

    public static final int BROADCAST_STEPS_UPDATE_CODE = 206;

    public static final int BROADCAST_PAUSE_WORKOUT_CODE = 207;

    public static final int BROADCAST_GOOGLE_FIT_READ_PERMISSION = 208;

    public static final int BROADCAST_REQUEST_PERMISSION_CODE = 209;

    public static final int BROADCAST_RESUME_WORKOUT_CODE = 210;

    public static final int BROADCAST_FIX_GOOGLE_PLAY_SERVICES_CODE = 211;

    public static final int PROBELM_NOT_MOVING = 0;

    public static final int PROBELM_TOO_SLOW = 1;

    public static final int PROBELM_TOO_FAST = 2;

    public static final int PROBLEM_GPS_DISABLED = 3;

    public static final String KEY_LOCATION_SETTINGS_PARCELABLE = "location_settings_parcelable";

    public static final String KEY_GOOGLE_FIT_RESOLUTION_PARCELABLE = "google_fit_resolution_parcelable";

    public static final String KEY_WORKOUT_RESULT = "key_wourkout_result";

    public static final String KEY_WORKOUT_TEST_MODE_ON = "key_workout_test_mode_on";

    public static final String KEY_WORKOUT_UPDATE_SPEED = "key_wourkout_update_speed";

    public static final String KEY_WORKOUT_UPDATE_ELAPSED_TIME_IN_SECS = "key_wourkout_update_elapsed_time_in_secs";

    public static final String KEY_WORKOUT_UPDATE_STEPS = "key_wourkout_steps";

    public static final String KEY_WORKOUT_UPDATE_TOTAL_DISTANCE = "key_wourkout_update_total_distance";

    public static final String KEY_PAUSE_WORKOUT_PROBLEM = "key_workout_pause_reason";

    public static final String KEY_USAIN_BOLT_DISTANCE_REDUCED = "key_usain_bolt_distance_reduced";

    public static final String KEY_STOP_WORKOUT_PROBLEM = "key_workout_stop_reason";

    public static final String KEY_CAUSE_LIST = "key_cause_list_v2";

    public static final String KEY_LAST_VISIBLE_CAUSE_LIST = "key_last_visible_cause_list_v2";

    public static final String KEY_LAST_CAUSE_SELECTED = "key_last_cause_selected";

    public static final String KEY_FAQ_LIST = "key_faq_list";

    public static final String PREF_WORKOUT_STATE = "pref_workout_state";

    public static final String PREF_PREV_DIST_RECORD_AS_STRING = "pref_prev_dist_record_as_string";

    public static final String PREF_WORKOUT_DATA_DIRTY = "pref_workout_data_dirty";

    public static final String PREF_WORKOUT_DATA_APPROVED = "pref_workout_data_approved";

    public static final String PREF_USAIN_BOLT_OCURRED_TIME_STAMPS = "pref_usain_bolt_ocurred_time_stamps";

    public static final String PREF_NEXT_VOICE_UPDATE_SCHEDULD_AT_INDEX = "pref_next_voice_update_scheduled_at_index";

    public static final String PREF_WORKOUT_DATA_NUM_STEPS_WHEN_BATCH_BEGIN = "pref_workout_data_num_steps_when_batch_begin";

    public static final String PREF_WORKOUT_DATA_NUM_STEPS_AT_PREVIOUS_RECORD = "pref_workout_data_num_steps_at_previous_record";

    public static final String PREF_WORKOUT_LIFETIME_DISTANCE = "pref_workout_lifetime_distance_new";

    public static final String PREF_WORKOUT_LIFETIME_STEPS = "pref_workout_lifetime_steps_new";

    public static final String PREF_TOTAL_CALORIES = "pref_total_calories";

    public static final String PREF_WORKOUT_LIFETIME_WORKING_OUT = "pref_workout_lifetime_working_out_new";

    public static final String PREF_WORKOUT_DATA_SYNC_VERSION = "pref_workout_data_sync_version";

    public static final String PREF_IS_LOGIN = "pref_user_login";
    public static final String PREF_USER_ID = "pref_user_id";
    public static final String PREF_FIRST_TIME_USER = "pref_first_time_user";
    public static final String PREF_IS_REMINDER_DISABLE = "pref_is_reminder_disable";
    public static final String PREF_TOTAL_RUN = "pref_total_run_count";
    public static final String PREF_TOTAL_IMPACT = "pref_total_impact";

    //User Data
    public static final String PREF_USER_EMAIL = "pref_user_email";
    public static final String PREF_LOGIN_SKIP = "pref_login_skip";
    public static final String PREF_AUTH_TOKEN = "pref_auth_token";
    public static final String PREF_CAUSE_DATA = "perf_cause_data";
    public static final String PREF_UNREAD_MESSAGE = "perf_has_unread_message";
    public static final String PREF_NEW_FEED_ARTICLE_AVAILABLE = "pref_new_feed_article_available";
    public static final String PREF_IS_SIGN_UP_USER ="pref_sign_up_user" ;
    public static final String PREF_FIRST_RUN_FEEDBACK = "pref_first_run_feedback";
    public static final String PREF_USER_DETAILS="pref_user_details";

    public static final String PREF_GLOBAL_LEADERBOARD_CACHED_DATA ="pref_global_leader_board_cached_data_new";
//    public static final String PREF_GLOBAL_LAST_WEEK_LEADERBOARD_CACHED_DATA ="pref_global_leaderboard_cached_data";
//    public static final String PREF_GLOBAL_ALL_TIME_LEADERBOARD_CACHED_DATA="pref_global_all_time_leaderboard_cached_data";
//    public static final String PREF_GLOBAL_LAST_MONTH_LEADERBOARD_CACHED_DATA="pref_global_last_month_leaderboard_cached_data";
    public static final String PREF_LEAGUEBOARD_CACHED_DATA="pref_leagueboard_cached_data_new";
    public static final String PREF_MY_TEAM_LEADERBOARD_CACHED_DATA="pref_my_team_leaderboard_cached_data_new";
    public static final String PREF_IS_WORKOUT_DATA_UP_TO_DATE_IN_DB="pref_is_workout_data_up_to_date_in_db";


    //app Update
    public static final String PREF_LATEST_APP_VERSION = "pref_latest_app_version";
    public static final String PREF_FORCE_UPDATE = "pref_force_update";
    public static final String PREF_APP_UPDATE_MESSAGE = "pref_promo_model_message";
    public static final String PREF_SHOW_APP_UPDATE_DIALOG = "pref_show_app_update_dialog";
    public static final String PREF_CAMPAIGN_DATA = "pref_campaign_data";
    public static final String PREF_CAMPAIGN_SHOWN_ONCE = "pref_campaign_shown_once";

    //Bundle
    public static final String BUNDLE_SHOW_RUN_STATS = "bundle_show_profile";

    public static final String GOOGLE_PROJECT_ID = "159550091621";

    public static final String PREF_FIRST_LAUNCH_EVENT_SENT = "first_launch_event_sent";

    public static final String PREF_DISABLE_ALERTS = "pref_disable_alerts";
    public static final String PREF_DISABLE_VOICE_UPDATES = "pref_disable_voice_updates";
    public static final String PREF_DISABLE_GPS_UPDATES = "pref_disable_gps_updates";
    public static final String PREF_LAST_ACTIVITY_DETECTION_STOPPED_TIMESTAMP = "pref_last_activity_detection_stopped_time_stamp";

    public static final String FEEDBACK_TAG_FLAGGED_RUN = "flag";
    public static final String FEEDBACK_TAG_DRAWER = "drawer";
    public static final String FEEDBACK_TAG_POST_RUN_SAD = "sad";


    public static final String PREF_APP_VERSION = "pref_app_version";

    public static final String PREF_CLIENT_CONFIG = "pref_client_config";
    public static final String PREF_PENDING_WORKOUT_LOCATION_DATA_QUEUE_PREFIX = "pending_workout_location_data_queue_";


    public static final String PREFS_CHAT_TOOLTIP_DISPLAY_COUNT = "chat_tooltip_diplay_count";

    public static final String PREFS_MY_COUNTRY_CODE = "my_country_code";
    public static final String PREFS_MY_CURRENCY = "my_currency";
    public static final String PREFS_MY_DISTANCE_UNIT = "my_distance_unit";
    public static final String PREFS_DEFAULT_UNITS_SET = "default_units_set";
    public static final String PREFS_MY_EXCHANGE_RATE = "my_exchange_rate";

    public static final int REQUEST_CODE_LOGIN = 1001;

    public static final String PAUSE_REASON_GPS_DISABLED = "gps_disabled";
    public static final String PAUSE_REASON_USAIN_BOLT = "usain_bolt";
    public static final String PAUSE_REASON_USER_CLICKED_NOTIFICATION = "user_clicked_notification";
    public static final String PAUSE_REASON_USER_CLICKED = "user_clicked";

    public static final String PREF_FITNESS_BATCHES = "fitness_batches";
    public static final String PREF_TYPE_STEP_COUNTER = "type_step_counter";

    public final static String USER_PROP_NAME="Name";
    public final static String USER_PROP_EMAIL="Email";
    public final static String USER_PROP_USER_ID="UserId";
    public final static String USER_PROP_PHONE="Phone";
    public final static String USER_PROP_GENDER="Gender";
    public final static String USER_PROP_TEAM_CODE="TeamCode";
    public final static String USER_PROP_AGE="Age";

    public static final String USER_PROP_LIFETIME_DISTANCE = "LifeTimeDistance";
    public static final String USER_PROP_LIFETIME_STEPS = "LifeTimeSteps";
    public static final String USER_PROP_AVG_STRIDE_LENGTH = "AvgStrideLength";
    public static final String USER_PROP_AVG_SPEED = "AvgSpeed";
    public static final String USER_PROP_AVG_CADENCE = "AvgCadence";
    public static final String USER_PROP_TOTAL_CALORIES = "TotalCalories";
    public static final String USER_PROP_TOTAL_RUNS = "TotalRuns";
    public static final String USER_PROP_TOTAL_AMT_RAISED = "TotalAmountRaised";
    public final static String USER_PROP_LEAGUE_NAME="LeagueName";
    public final static String USER_PROP_LEAGUE_TEAM_NAME="LeagueTeamName";

    public static final String FEED_WEBVIEW_DEFAULT_URL = "http://blog.impactapp.in/";

    public static final String PREF_DID_USER_RATE_APP = "pref_did_user_rate_app";
    public static final String PREF_LAST_TIME_FEED_WAS_SEEN = "pref_last_time_feed_was_seen";


    public static String SHARE_PLACEHOLDER_FIRST_NAME = "<first_name>";
    public static String SHARE_PLACEHOLDER_DISTANCE = "<distance>";
    public static String SHARE_PLACEHOLDER_AMOUNT = "<amount>";
    public static String SHARE_PLACEHOLDER_SPONSOR = "<sponsor_company>";
    public static String SHARE_PLACEHOLDER_PARTNER = "<partner_ngo>";


    public static final List<FeedbackCategory>  HELP_CENTER_CATEGORIES = new ArrayList<FeedbackCategory>(){{
        add(FeedbackCategory.PAST_WORKOUT);
        add(FeedbackCategory.QUESTIONS);
        add(FeedbackCategory.FEEDBACK);
        add(FeedbackCategory.SOMETHING_ELSE);
    }};

    public static final List<FeedbackCategory> getPastWorkoutCategories(){
        return new ArrayList<FeedbackCategory>(){{
            add(FeedbackCategory.LESS_DISTANCE.copy());
            add(FeedbackCategory.MORE_DISTANCE.copy());
            add(FeedbackCategory.FLAGGED_RUN.copy());
            add(FeedbackCategory.NOT_IN_VEHICLE.copy());
            add(FeedbackCategory.IMPACT_MISSING_LEADERBOARD.copy());
            add(FeedbackCategory.STILL_SOMETHING_ELSE.copy());
        }};
    }

    public static final List<FeedbackCategory> getPostRunSadCategories(){
        return new ArrayList<FeedbackCategory>(){{
            add(FeedbackCategory.LESS_DISTANCE.copy());
            add(FeedbackCategory.MORE_DISTANCE.copy());
            add(FeedbackCategory.NOT_IN_VEHICLE.copy());
            add(FeedbackCategory.STILL_SOMETHING_ELSE.copy());
        }};
    }

    public static final List<FeedbackCategory>  getOtherLevelTwoCategories() {
        return new ArrayList<FeedbackCategory>() {{
            add(FeedbackCategory.DISTANCE_NOT_ACCURATE.copy());
            add(FeedbackCategory.WORKOUT_MISSING_HISTORY.copy());
            add(FeedbackCategory.IMPACT_MISSING_LEADERBOARD.copy());
            add(FeedbackCategory.NOT_IN_VEHICLE.copy());
            add(FeedbackCategory.GPS_ISSUE.copy());
            add(FeedbackCategory.ZERO_DISTANCE.copy());
            add(FeedbackCategory.STILL_SOMETHING_ELSE.copy());
        }};
    }

    public static final List<LatLng> SAMPLE_POINTS_LIST = new ArrayList<LatLng>() {{
        add(new LatLng(19.118394, 72.914196));
        add(new LatLng(19.118485, 72.913496));
        add(new LatLng(19.118592, 72.912808));
        add(new LatLng(19.118616, 72.912654));
        add(new LatLng(19.118616, 72.912473));
        add(new LatLng(19.118614, 72.912325));
        add(new LatLng(19.118609, 72.912188));
        add(new LatLng(19.118596, 72.912044));
        add(new LatLng(19.118472, 72.911925));
        add(new LatLng(19.118374, 72.911835));
        add(new LatLng(19.118280, 72.911754));
        add(new LatLng(19.118176, 72.911660));
        add(new LatLng(19.118086, 72.911555));
        add(new LatLng(19.117179, 72.910924));
        add(new LatLng(19.116917, 72.910417));
    }};

    public static final Map<Integer, Level> LEVELS_MAP = new HashMap<Integer, Level>(){{
        put(0, new Level(0, 0, 10));
        put(1, new Level(1, 10, 50));
        put(2, new Level(2, 50, 100));
        put(3, new Level(3, 100, 500));
        put(4, new Level(4, 500, 1000));
        put(5, new Level(5, 1000, 2000));
        put(6, new Level(6, 2000, 5000));
        put(7, new Level(7, 5000, 10000));
        put(8, new Level(8, 10000, 20000));
        put(9, new Level(9, 20000, 50000));
        put(10, new Level(10, 50000, 100000));
        put(11, new Level(11, 100000, Integer.MAX_VALUE));
    }};

    public static final String DEFAULT_EXCHANGE_RATE_JSON = "[\n" +
            "                {\n" +
            "                    \"currency\": \"USD\",\n" +
            "                    \"rate\": 65.08\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"CAD\",\n" +
            "                    \"rate\": 52.26\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"rate\": 1.0\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"GBP\",\n" +
            "                    \"rate\": 86.11\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"EUR\",\n" +
            "                    \"rate\": 77.21\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"JPY\",\n" +
            "                    \"rate\": 0.58\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"AUD\",\n" +
            "                    \"rate\": 50.94\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"CHF\",\n" +
            "                    \"rate\": 66.83\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"CNY\",\n" +
            "                    \"rate\": 9.88\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"SEK\",\n" +
            "                    \"rate\": 8.04\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"NZD\",\n" +
            "                    \"rate\": 74.21\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"MXN\",\n" +
            "                    \"rate\": 3.48\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"SGD\",\n" +
            "                    \"rate\": 48.08\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"HKD\",\n" +
            "                    \"rate\": 8.34\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"NOK\",\n" +
            "                    \"rate\": 8.23\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"KRW\",\n" +
            "                    \"rate\": 0.06\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"TRY\",\n" +
            "                    \"rate\": 17.9\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"RUB\",\n" +
            "                    \"rate\": 1.13\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"BRL\",\n" +
            "                    \"rate\": 20.52\n" +
            "                },\n" +
            "                {\n" +
            "                    \"currency\": \"ZAR\",\n" +
            "                    \"rate\": 4.83\n" +
            "                }\n" +
            "            ]";

}
