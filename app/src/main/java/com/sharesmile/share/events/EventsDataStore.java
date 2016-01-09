package com.sharesmile.share.events;

import com.sharesmile.share.events.models.Event;
import com.sharesmile.share.events.models.EventsPageData;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ankitmaheshwari1 on 09/01/16.
 */
public class EventsDataStore {

    private static final String TAG = "EventsDataStore";

    private Map<Integer, List<Event>> eventsPageWiseMap;
    private List<Event> eventsList;
    private int totalCount;

    public EventsDataStore(){
        this.eventsPageWiseMap = new HashMap<>();
        this.eventsList = new ArrayList<>();
        totalCount = 0;
    }

    public void addPageData(EventsPageData data){
        int pgNum = data.getPageNum();
        if (eventsPageWiseMap.containsKey(pgNum)){
            //Already there, do nothing
            return;
        }
        List<Event> eventsInPage = data.getEvents();
        if (!Utils.isCollectionFilled(eventsInPage)){
            // Nothing to do, return
            return;
        }

        eventsPageWiseMap.put(pgNum, eventsInPage);
        for (Event event : eventsInPage){
            eventsList.add(event);
        }
        totalCount = eventsList.size();
    }

    public int getTotalEvents(){
        return  totalCount;
    }

    public List<Event> getEventsList(){
        return eventsList;
    }

    public Event get(int index){
        return eventsList.get(index);
    }

}
