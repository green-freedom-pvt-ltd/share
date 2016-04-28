package com.sharesmile.share.gps;

import android.os.Binder;

/**
 * Created by ankitmaheshwari1 on 20/02/16.
 */
public class LocationBinder extends Binder {

    private static final String TAG = "LocationBinder";

    private WorkoutService service;

    public LocationBinder(WorkoutService service){
        this.service = service;
    }

    public WorkoutService getService(){
        return service;
    }

}
