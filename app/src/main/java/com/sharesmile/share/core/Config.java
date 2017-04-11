package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class Config {

    private static final String TAG = "Config";

    public static final long LOCATION_UPDATE_INTERVAL = 1000; // in millisecs

    public static final int STEP_THRESHOLD_INTERVAL = 5000000; // in micro secs

    public static final long VIGILANCE_START_THRESHOLD = 80000; // in millisecs

    public static final float MIN_DISTANCE_FOR_VIGILANCE = 50; // in meters

    public static final float SPIKE_FILTER_SPEED_THRESHOLD_IN_VEHICLE = 52f; // in m/s, i.e. 187 km/hr

    public static final float SPIKE_FILTER_SPEED_THRESHOLD_DEFAULT = 32f; // in m/s, i.e. 115.2 km/hr

    public static final float SPIKE_FILTER_SPEED_THRESHOLD_ON_FOOT = 13f; // in m/s, i.e. 46.8 km/hr

    public static final float UPPER_SPEED_LIMIT = 7f; // in m/s, i.e. 25.2 km/hr

    public static final float UPPER_SPEED_LIMIT_WITH_ACTIVITY_DETECTION = 5.5f; // in m/s, i.e. 20 km/hr

    public static final float LOWER_SPEED_LIMIT = 0.7f; // in m/s,

    public static final float THRESHOLD_INTEVAL = 1; // in secs

    public static final float THRESHOLD_ACCURACY_OFFSET = 1f;

    public static float USAIN_BOLT_WAIVER_STEPS_RATIO = 0.4f;

    public static float GLOBAL_AVERAGE_STRIDE_LENGTH = 0.6f; // in meters

    public static long VIGILANCE_TIMER_INTERVAL = 10000; // in millisecs

    public static float MIN_STEPS_PER_SECOND_FACTOR = 1f;

    public static float MAX_STEPS_PER_SECOND_FACTOR = 3.5f;

    public static float SOURCE_ACCEPTABLE_ACCURACY = 40; // in m

    public static float THRESHOLD_ACCURACY = 12; // in m

    public static float THRESHOLD_FACTOR = 4.75f;

    public static float SMALLEST_DISPLACEMENT = 0.1f;// in m

    public static boolean TOO_SLOW_CHECK = false;

    public static boolean LAZY_ASS_CHECK = false;

    public static boolean USAIN_BOLT_CHECK = true;

    public static long GPS_INACTIVITY_NOTIFICATION_DELAY = 55000; // in Millisecs

    // Activity Detector Config

    public static final int CONFIDENCE_THRESHOLD = 80;
    public static final int CONFIDENCE_THRESHOLD_EVENT = 60;
    public static final long ACTIVITY_VALID_INTERVAL = 24000; // in millisecs
    public static final long DETECTED_INTERVAL_IDLE = 7000; // in millisecs
    public static final long DETECTED_INTERVAL_ACTIVE = 2000; // in millisecs

    public static final long CURRENT_SPEED_VALIDITY_THRESHOLD_INTERVAL = 12000; // in millisecs




}
