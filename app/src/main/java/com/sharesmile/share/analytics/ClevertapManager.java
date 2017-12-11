package com.sharesmile.share.analytics;

import android.content.Context;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.exceptions.CleverTapMetaDataNotFoundException;
import com.clevertap.android.sdk.exceptions.CleverTapPermissionsNotSatisfied;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ankitm on 09/04/16.
 */

public class ClevertapManager implements EventConsumer {

    private static final String TAG = "ClevertapManager";
    CleverTapAPI cleverTap;
    private Context context;

    ClevertapManager(Context context) {
        this.context = context;
        init();
    }

    @Override
    public void sendEvent (AnalyticsEvent event) throws JSONException {
        if (event == null) {
            Logger.e(TAG, "[sendEvent] Event passed is null");
            return;
        }
        cleverTap.event.push(event.getEventName(), event.getProperties().getBackingMap());
    }

    private void init() {
        try {
            cleverTap = CleverTapAPI.getInstance(context);
            CleverTapAPI.setDebugLevel(1);
        } catch (CleverTapMetaDataNotFoundException e) {
            // thrown if you haven't specified your CleverTap Account ID or Token in your AndroidManifest.xml
            Logger.e(TAG, "you haven't specified your CleverTap Account ID or Token in your AndroidManifest.xml");
        } catch (CleverTapPermissionsNotSatisfied e) {
            // thrown if you haven’t requested the required permissions in your AndroidManifest.xml
            Logger.e(TAG, "you haven’t requested the required permissions in your AndroidManifest.xml");
        }
    }

    public void setUserProperty(String propertyName, Object value){
        Map<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put(propertyName, value);
        cleverTap.profile.push(profileUpdate);
    }

    private void flattenDictionary(String dictKey, JSONObject dictValue, JSONObject properties) {
        Iterator<String> iter = dictValue.keys();
        try {
            while (iter.hasNext()) {
                String key = iter.next();
                JSONObject nestedDictValue = dictValue.optJSONObject(key);
                if (nestedDictValue != null) {
                    flattenDictionary(dictKey + "." + key, nestedDictValue, properties);
                } else {
                    Object flatValue = dictValue.get(key);
                    properties.put(dictKey + "." + key, flatValue);
                }
            }
            properties.remove(dictKey);
        } catch (JSONException jse) {
            Logger.e(TAG, "Problem flattening JSONObject with key: " + dictKey + ", and value: " +
                    String.valueOf(dictValue), jse);
        }
    }

}


