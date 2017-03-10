package com.sharesmile.share.gps.activityrecognition;

import android.content.Context;

import com.sharesmile.share.gps.GoogleLocationTracker;

/**
 * Created by ankitmaheshwari on 3/10/17.
 */

public class ActivityDetector {

    private static final String TAG = "ActivityDetector";

    private static ActivityDetector uniqueInstance;
    private Context appContext;


    private ActivityDetector(Context appContext){
        this.appContext = appContext;
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique ActivityDetector instance
     */
    public static ActivityDetector getInstance() {
        if (uniqueInstance == null) {
            throw new IllegalStateException(
                    "ActivityDetector is not initialized, call initialize(applicationContext) " +
                            "static method first");
        }
        return uniqueInstance;
    }

    /**
     Initialize this class using application Context,
     should be called once in the beginning by any application Component

     @param appContext application context
     */
    public static void initialize(Context appContext) {
        if (appContext == null) {
            throw new NullPointerException("Provided application context is null");
        }
        if (uniqueInstance == null) {
            synchronized (ActivityDetector.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new ActivityDetector(appContext);
                }
            }
        }
    }


}
