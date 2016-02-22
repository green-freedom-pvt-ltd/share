package com.sharesmile.share.gps;

import android.os.Binder;

/**
 * Created by ankitmaheshwari1 on 20/02/16.
 */
public class LocationBinder extends Binder {

    private static final String TAG = "LocationBinder";

    private LocationService service;

    public LocationBinder(LocationService service){
        this.service = service;
    }

    public LocationService getService(){
        return service;
    }

}
