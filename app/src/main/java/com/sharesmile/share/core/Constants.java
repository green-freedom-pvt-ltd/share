package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari1 on 20/02/16.
 */
public class Constants {

    private static final String TAG = "Constants";

    public static final int CODE_LOCATION_SETTINGS_RESOLUTION = 101;

    public static final int CODE_REQUEST_LOCATION_PERMISSION = 102;

    public static final int CODE_REQUEST_WRITE_PERMISSION = 103;

    public static final String  LOCATION_SERVICE_BROADCAST_ACTION = "com.sharesmile.share.gps.location";

    public static final String  LOCATION_SERVICE_BROADCAST_CATEGORY = "location_service_braodcast_category";

    public static final int BROADCAST_FIX_LOCATION_SETTINGS_CODE = 201;

    public static final int BROADCAST_WORKOUT_RESULT_CODE = 202;

    public static final int BROADCAST_WORKOUT_UPDATE_CODE = 203;

    public static final int BROADCAST_UNBIND_SERVICE_CODE = 204;

    public static final String KEY_LOCATION_SETTINGS_PARCELABLE = "location_settings_parcelable";

    public static final String KEY_WORKOUT_RESULT = "key_wourkout_result";

    public static final String KEY_WORKOUT_UPDATE_SPEED = "key_wourkout_update";

    public static final String KEY_WORKOUT_UPDATE_TOTAL_DISTANCE = "key_wourkout_update_total_distance";

    public static final String PREF_IS_WORKOUT_ACTIVE = "pref_is_workout_active";

    public static final String PREF_RUN_SOURCE  = "pref_run_source";

    public static final String PREF_PREV_DIST_RECORD  = "pref_prev_dist_record";

    public static final String PREF_RUN_BEGIN_TIMESTAMP  = "pref_run_begin_time_stamp";

    public static final String PREF_NUM_RECORDS  = "pref_num_records";

    public static final String PREF_WORKOUT_DATA  = "pref_workout_data";
}
