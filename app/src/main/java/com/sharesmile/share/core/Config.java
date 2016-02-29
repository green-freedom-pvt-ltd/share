package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class Config {

    private static final String TAG = "Config";

    public static final long LOCATION_UPDATE_INTERVAL = 1000; // in millisecs

    public static final int STEP_THRESHOLD_INTERVAL = 20000000; // in micro secs, i.e. 20 secs

    public static final long VIGILANCE_START_THRESHOLD = 40000; // in millisecs

    public static final float UPPER_SPEED_LIMIT = 6f; // in m/s, i.e. 20 km/hr

    public static final float LOWER_SPEED_LIMIT = 1f; // in m/s, i.e. 20 km/hr

    public static final float STEPS_PER_SECOND_FACTOR = 1f;

    public static float THRESHOLD_INTEVAL = 1; // in secs

    public static float SOURCE_ACCEPTABLE_ACCURACY = 50; // in m

    public static float THRESHOLD_ACCURACY = 6; // in m

    public static float THRESHOLD_ACCURACY_OFFSET = 1f;

    public static float THRESHOLD_FACTOR = 10;

    public static final float SMALLEST_DISPLACEMENT = 10;// in m

    public static boolean SPEED_TRACKING = false;

}
