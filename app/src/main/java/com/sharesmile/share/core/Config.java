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

    public static final int SPIKE_FILTER_ELIGIBLE_TIME_INTERVAL = 20; // in secs

    public static final float SPIKE_FILTER_SPEED_THRESHOLD = 45f; // in m/s, i.e. 166 km/hr

    public static final float UPPER_SPEED_LIMIT = 7f; // in m/s, i.e. 25.2 km/hr

    public static final float LOWER_SPEED_LIMIT = 0.7f; // in m/s,

    public static final float THRESHOLD_INTEVAL = 1; // in secs

    public static final float THRESHOLD_ACCURACY_OFFSET = 1f;

    public static float USAIN_BOLT_WAIVER_STEPS_RATIO = 0.2f;

    public static float GLOBAL_AVERAGE_STRIDE_LENGTH = 1.2f; // in meters

    public static long VIGILANCE_TIMER_INTERVAL = 10000; // in millisecs

    public static float MIN_STEPS_PER_SECOND_FACTOR = 1f;

    public static float SOURCE_ACCEPTABLE_ACCURACY = 40; // in m

    public static float THRESHOLD_ACCURACY = 12; // in m

    public static float THRESHOLD_FACTOR = 5.7f;

    public static float SMALLEST_DISPLACEMENT = 0.1f;// in m

    public static boolean TOO_SLOW_CHECK = false;

    public static boolean LAZY_ASS_CHECK = false;

    public static boolean USAIN_BOLT_CHECK = true;

    public static final int ACTIVITY_RECOGNITION_CONFIDENCE_THRESHOLD = 85;


}
