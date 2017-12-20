package com.sharesmile.share.gps;

import android.location.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by ankitmaheshwari on 12/20/17.
 */

public class LocationSerializer implements JsonSerializer<Location> {

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc,
                                 JsonSerializationContext context) {
        JsonObject jo = new JsonObject();
        jo.addProperty("mProvider", src.getProvider());
        jo.addProperty("mAccuracy", src.getAccuracy());
        jo.addProperty("mAltitude", src.getAltitude());
        jo.addProperty("mBearing", src.getBearing());
        jo.addProperty("mSpeed", src.getSpeed());
        jo.addProperty("mLatitude", src.getLatitude());
        jo.addProperty("mLongitude", src.getLongitude());
        jo.addProperty("mTime", src.getTime());
        return jo;
    }
}
