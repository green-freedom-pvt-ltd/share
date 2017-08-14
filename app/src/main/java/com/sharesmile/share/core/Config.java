package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class Config {

    private static final String TAG = "Config";

//    // Remotely configurable parameters
//
//    public static final float MIN_CADENCE_FOR_WALK = 0.68f; // in steps per sec
//
//    public static final float SPIKE_FILTER_SPEED_THRESHOLD_IN_VEHICLE = 52f; // in m/s, i.e. 187 km/hr
//
//    public static final float SPIKE_FILTER_SPEED_THRESHOLD_DEFAULT = 32f; // in m/s, i.e. 115.2 km/hr
//
//    public static final float SECONDARY_SPIKE_FILTER_SPEED_THRESHOLD_DEFAULT = 16.67f; // in m/s, i.e. 60 km/hr
//
//    public static final float SPIKE_FILTER_SPEED_THRESHOLD_ON_FOOT = 9.03f; // in m/s, i.e. 32.5 km/hr
//
//    public static final float USAIN_BOLT_UPPER_SPEED_LIMIT = 7f; // in m/s, i.e. 25.2 km/hr
//
//    public static final float USAIN_BOLT_UPPER_SPEED_LIMIT_ON_FOOT = 12.5f; // in m/s, i.e. 45 km/hr
//
//    public static final float USAIN_BOLT_RECENT_SPEED_LOWER_BOUND = 4.1f; // in m/s, i.e. 14.8 km/hr
//
//    public static final float USAIN_BOLT_GPS_SPEED_LIMIT = 5.83f; // in m/s, i.e. 21 km/hr
//
//    public static float USAIN_BOLT_WAIVER_STEPS_RATIO = 0.38f;
//
//    public static long VIGILANCE_TIMER_INTERVAL = 17000; // in millisecs
//
//    public static float SOURCE_ACCEPTABLE_ACCURACY = 40; // in m
//
//    public static float THRESHOLD_ACCURACY = 12; // in m
//
//    public static float THRESHOLD_FACTOR = 4.75f;
//
//    public static long GPS_INACTIVITY_NOTIFICATION_DELAY = 50000; // in Millisecs
//
//    // Activity Detector Config
//    public static final int CONFIDENCE_THRESHOLD_VEHICLE = 74;
//    public static final int CONFIDENCE_THRESHOLD_ON_FOOT = 73;
//    public static final int CONFIDENCE_UPPER_THRESHOLD_STILL = 85;
//    public static final int CONFIDENCE_LOWER_THRESHOLD_STILL = 20;
//    // Minimum ON_FOOT confidence required for walk engagement notif
//    public static final int CONFIDENCE_THRESHOLD_WALK_ENGAGEMENT = 60;
//
//    // Used maintain history of activityRecognitionResult recent history
//    public static final int ACTIVITY_RECOGNITION_RESULT_HISTORY_QUEUE_MAX_SIZE = 5;
//
//    // ActivityRecognition updates request interval
//    public static final long DETECTED_INTERVAL_IDLE = 10000; // in millisecs
//    public static final long DETECTED_INTERVAL_ACTIVE = 2000; // in millisecs
//
//    // If user remains continuously still for this much time then Still notification is shown
//    public static final long STILL_NOTIFICATION_DISPLAY_INTERVAL = 25000; // in millisecs
//
//    // Walk engagement counter is invoked periodically after this interval
//    public static final long WALK_ENGAGEMENT_COUNTER_INTERVAL = 15000; // in millisecs
//
//    // Show walk engaagement notif if user has been on foot continuously for this much amount of time
//    public static final long WALK_ENGAGEMENT_NOTIFICATION_INTERVAL = 105000;// in millisecs
//
//    public static final long WALK_ENGAGEMENT_NOTIFICATION_THROTTLE_PERIOD = 43200000;// in millisecs, i.e. 12 hours
//
//    // During tracking If GPS behaves bad continuously for this much amount of time, then BAD_GPS_NOTIF is shown
//    public static final long BAD_GPS_NOTIF_THRESHOLD_INTERVAL = 60000;
//
//    // In a single DistRecord update delta_distance cannot be more than this value
//    public static final float DIST_INC_IN_SINGLE_GPS_UPDATE_UPPER_LIMIT = 5000f;// in meters
//
//    // ON_WORKOUT_UPDATE analytics event occurrs after this much distance
//    public static final float MIN_DISPLACEMENT_FOR_WORKOUT_UPDATE_EVENT = 0.1f;// in Kms
//
//
//    // Sync Config
//    // All the client data is synced periodically after this interval
//    public static final long DATA_SYNC_INTERVAL = 10800L;// in secs, i.e. every 3 hours
//    public static final long DATA_SYNC_INTERVAL_FLEX = 5400L;// in secs, i.e. every 1.5 hours

    /*******************************/

    public static final long CONSECUTIVE_USAIN_BOLT_WAIVER_TIME_INTERVAL = 1200000; // in millis

    public static final int TRACKER_RECORD_HISTORY_QUEUE_MAX_SIZE = 8;

    public static final int WORKOUT_BEGINNING_LOCATION_CIRCULAR_QUEUE_MAX_SIZE = 3;

    // Used to calculate recent GPS speed
    public static final int GOOGLE_LOCATION_TRACKER_QUEUE_MAX_SIZE = 8;


    public static final long LOCATION_UPDATE_INTERVAL = 1000; // in millisecs

    public static final int STEP_THRESHOLD_INTERVAL = 5000000; // in micro secs

    public static final long VIGILANCE_START_THRESHOLD = 80000; // in millisecs

    public static final float MIN_DISTANCE_FOR_VIGILANCE = 50; // in meters

    public static final float LOWER_SPEED_LIMIT = 0.7f; // in m/s,

    public static final float THRESHOLD_INTEVAL = 1; // in secs

    public static final float THRESHOLD_ACCURACY_OFFSET = 1f;

    public static float GLOBAL_AVERAGE_STRIDE_LENGTH = 0.6f; // in meters

    public static float GLOBAL_AVERAGE_STRIDE_LENGTH_LOWER_LIMIT = 0.3f; // in meters

    public static float GLOBAL_AVERAGE_STRIDE_LENGTH_UPPER_LIMIT = 1.0f; // in meters

    public static float MIN_STEPS_PER_SECOND_FACTOR = 1f;

    public static float MAX_STEPS_PER_SECOND_FACTOR = 3.5f;

    public static boolean TOO_SLOW_CHECK = false;

    public static boolean LAZY_ASS_CHECK = false;

    public static boolean USAIN_BOLT_CHECK = true;


    // All recent avg confidence values are reset to 0 after this interval
    public static final long ACTIVITY_RESET_CONFIDENCE_VALUES_INTERVAL = 30000; // in millisecs
    public static final long ACTIVITY_RESET_CONFIDENCE_VALUES_INTERVAL_INACTIVE = 40000; // in millisecs

    // If ActivityRecognitionResult is older than this interval then the result value is not considered in calculating cumulative recent average confidence values
    public static final long ACTIVITY_VALID_INTERVAL_ACTIVE = 15000; // in millisecs
    public static final long ACTIVITY_VALID_INTERVAL_IDLE = 35000; // in millisecs


    public static final long CURRENT_SPEED_VALIDITY_THRESHOLD_INTERVAL = 24000; // in millisecs
    public static final long CURRENT_GPS_SPEED_VALIDITY_THRESHOLD_INTERVAL = 24000; // in millisecs


    public static final long REMOVE_WALK_ENGAGEMENT_NOTIF_INTERVAL = 60000; // in millisecs


    public static final float GOOD_GPS_RECENT_SPEED_LOWER_THRESHOLD = 0.305f; // in m/s, i.e. 1.1 km/hr
    public static final float MIN_NUM_SPIKES_RATE_FOR_BAD_GPS = 0.083f; // in spikes/sec, i.e. 5 spikes per min

    public static final float MIN_DISTANCE_FOR_FEEDBACK_POPUP = 5.0f;// in Kms


}
