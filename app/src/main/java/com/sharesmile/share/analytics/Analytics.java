package com.sharesmile.share.analytics;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Shine on 05/02/17.
 */

public class Analytics {


    private static MixpanelAPI mixpanel;

    private static MixpanelAPI getInstance() {
        if (mixpanel == null) {
            mixpanel = MixpanelAPI.getInstance(MainApplication.getContext(), MainApplication.getContext().getString(R.string.mixpanel_project_token));
        }
        return mixpanel;
    }


    public static void track(String event, JSONObject jsonObject) {
        getInstance().track(event, jsonObject);
    }

    public static JSONObject createProp(JSONObject props, String key, String value) {

        if (props == null) {
            props = new JSONObject();
        }

        try {
            props.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return props;

    }

}
