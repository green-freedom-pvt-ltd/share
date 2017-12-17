package com.sharesmile.share.gps.location;

import android.location.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by ankitmaheshwari on 12/18/17.
 */

public class LocationDeserializer implements JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jo = json.getAsJsonObject();
        Location l = new Location(jo.getAsJsonPrimitive("mProvider").getAsString());
        l.setAccuracy(jo.getAsJsonPrimitive("mAccuracy").getAsFloat());
        l.setAltitude(jo.getAsJsonPrimitive("mAltitude").getAsDouble());
        l.setBearing(jo.getAsJsonPrimitive("mBearing").getAsFloat());
        l.setSpeed(jo.getAsJsonPrimitive("mSpeed").getAsFloat());
        l.setLatitude(jo.getAsJsonPrimitive("mLatitude").getAsDouble());
        l.setLongitude(jo.getAsJsonPrimitive("mLongitude").getAsDouble());
        l.setTime(jo.getAsJsonPrimitive("mTime").getAsLong());
        return l;
    }
}
