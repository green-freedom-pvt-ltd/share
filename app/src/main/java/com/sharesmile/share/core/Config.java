package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class Config {

    private static final String TAG = "Config";

    public static final long LOCATION_UPDATE_INTERVAL = 1000; // in millisecs

    public static final int STEP_THRESHOLD_INTERVAL = 5000000; // in micro secs, i.e. 20 secs

    public static final long VIGILANCE_TIMER_INTERVAL = 20000; // in millisecs

    public static final long VIGILANCE_START_THRESHOLD = 80000; // in millisecs

    public static final float MIN_DISTANCE_FOR_VIGILANCE = 50; // in meters

    public static final float UPPER_SPEED_LIMIT = 8f; // in m/s, i.e. 20 km/hr

    public static final float LOWER_SPEED_LIMIT = 0.7f; // in m/s, i.e. 20 km/hr

    public static final float STEPS_PER_SECOND_FACTOR = 1f;

    public static float THRESHOLD_INTEVAL = 1; // in secs

    public static float SOURCE_ACCEPTABLE_ACCURACY = 30; // in m

    public static float THRESHOLD_ACCURACY = 7; // in m

    public static float THRESHOLD_ACCURACY_OFFSET = 1f;

    public static float THRESHOLD_FACTOR = 6;

    public static final float SMALLEST_DISPLACEMENT = 7;// in m

    public static boolean SPEED_TRACKING = false;

}
