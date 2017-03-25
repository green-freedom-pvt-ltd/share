package com.sharesmile.share.core;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

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

    public static final String PREF_WORKOUT_STATE = "pref_workout_state";

    public static final String PREF_PREV_DIST_RECORD = "pref_prev_dist_record";

    public static final String PREF_WORKOUT_DATA_DIRTY = "pref_workout_data_dirty";

    public static final String PREF_WORKOUT_DATA_APPROVED = "pref_workout_data_approved";

    public static final String PREF_WORKOUT_LIFETIME_DISTANCE = "pref_workout_lifetime_distance_new";

    public static final String PREF_WORKOUT_LIFETIME_STEPS = "pref_workout_lifetime_steps_new";

    public static final String PREF_WORKOUT_LIFETIME_WORKING_OUT = "pref_workout_lifetime_working_out_new";

    public static final String PREF_IS_LOGIN = "pref_user_login";
    public static final String PREF_USER_ID = "pref_user_id";
    public static final String PREF_FIRST_TIME_USER = "pref_first_time_user";
    public static final String PREF_TOTAL_RUN = "pref_total_run_count";
    public static final String PREF_TOTAL_IMPACT = "pref_total_impact";

    //User Data
    public static final String PREF_USER_NAME = "pref_user_name";
    public static final String PREF_USER_EMAIL = "pref_user_email";
    public static final String PREF_USER_IMAGE = "pref_user_image";
    public static final String PREF_LOGIN_SKIP = "pref_login_skip";
    public static final String PREF_AUTH_TOKEN = "pref_auth_token";
    public static final String PREF_CAUSE_DATA = "perf_cause_data";
    public static final String PREF_HAS_RUN = "perf_has_run";
    public static final String PREF_UNREAD_MESSAGE = "perf_has_unread_message";
    public static final String PREF_IS_SIGN_UP_USER ="pref_sign_up_user" ;
    public static final String PREF_FIRST_RUN_FEEDBACK = "pref_first_run_feedback";
    public static final String PREF_LEAGUE_TEAM_ID="pref_league_team_id";


    //app Update
    public static final String PREF_LATEST_APP_VERSION = "pref_latest_app_version";
    public static final String PREF_FORCE_UPDATE = "pref_force_update";
    public static final String PREF_APP_UPDATE_MESSAGE = "pref_promo_model_message";
    public static final String PREF_SHOW_APP_UPDATE_DIALOG = "pref_show_app_update_dialog";
    public static final String PREF_CAMPAIGN_DATA = "pref_campaign_data";
    public static final String PREF_CAMPAIGN_SHOWN_ONCE = "pref_campaign_shown_once";

    //Bundle
    public static final String BUNDLE_FIRST_RUN_FEEDBACK = "bundle_first_run_feedback";
    public static final String BUNDLE_SHOW_PROFILE = "bundle_show_profile";

    public static final String GOOGLE_PROJECT_ID = "159550091621";

    public static final String PREF_FIRST_LAUNCH_EVENT_SENT = "first_launch_event_sent";

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

}
