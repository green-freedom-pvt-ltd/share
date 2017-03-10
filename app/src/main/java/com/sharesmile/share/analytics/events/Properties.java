package com.sharesmile.share.analytics.events;

import com.sharesmile.share.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ankitm on 10/04/16.
 */
public class Properties {

    //Common properties
    public static final String EVENT_ID = "eid";
    public static final String EVENT_NAME = "enanme";
    public static final String EVENT_CATEGORY = "ecat";
    public static final String EVENT_TIME_STAMP = "ets";

    private HashMap<String, Object> backingMap;

    public Properties(){
        backingMap = new HashMap<>();
    }

    public Properties(Properties copyFrom){
        backingMap = new HashMap<>(copyFrom.backingMap);
    }

    public Properties add(Properties bundleProperties){
        if (bundleProperties != null && bundleProperties.backingMap != null
                && !bundleProperties.backingMap.isEmpty()){
            backingMap.putAll(bundleProperties.backingMap);
        }
        return this;
    }

    public void put(String name, Object value) {
        backingMap.put(name, value);
    }

    public String toJsonString(){
        return Utils.createPrettyJSONStringFromObject(backingMap);
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject(toJsonString());
    }

    public Object get(String name){
        return backingMap.get(name);
    }

    public Map<String, Object> getBackingMap(){
        return backingMap;
    }

    /**
     * Returns true iff a non null (and non Empty in case of string property)
     * value for the given propertyName is present in the map
     * @param propertyName String
     */
    public boolean isPropertyEmpty(String propertyName){
        if (get(propertyName) == null){
            return true;
        }
        if (get(propertyName) instanceof String){
            return "".equals(get(propertyName));
        }
        return false;
    }

}

