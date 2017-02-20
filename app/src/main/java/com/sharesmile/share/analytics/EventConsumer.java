package com.sharesmile.share.analytics;

import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.core.UnObfuscable;

import org.json.JSONException;

/**
 * Created by ankitm on 09/04/16.
 */
public interface EventConsumer extends UnObfuscable {

    void sendEvent(AnalyticsEvent event) throws JSONException;

}

