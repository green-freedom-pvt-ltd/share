package com.sharesmile.share.events.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

import java.util.List;

/**
 * Created by ankitmaheshwari1 on 08/01/16.
 */
public class EventsPageData implements UnObfuscable{

    private static final String TAG = "EventsPageData";

    @SerializedName("pg_num")
    private int pageNum;

    List<Event> events;

    public int getPageNum() {
        return pageNum;
    }

    public List<Event> getEvents() {
        return events;
    }

}
