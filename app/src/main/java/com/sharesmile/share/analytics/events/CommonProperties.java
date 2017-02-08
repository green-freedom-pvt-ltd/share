package com.sharesmile.share.analytics.events;

import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by ankitm on 10/04/16.
 */
public class CommonProperties extends Properties {

    private static final String TAG = "CommonProperties";

    private static final ArrayList<String> MANDATORY_PROPERTIES
            = new ArrayList<String>() {
        {
            add(EVENT_CATEGORY);
            add(EVENT_NAME);
        }
    };

    private CommonProperties(){
        super();
    }

    static class Builder {

        private Context context;
        private CommonProperties commonProperties;

        Builder(Context context){
            this.context = context;
            commonProperties = new CommonProperties();
        }

        Builder eventName(String name){
            commonProperties.put(EVENT_NAME, name);
            return this;
        }

        Builder eventCategory(String category){
            commonProperties.put(EVENT_CATEGORY, category);
            return this;
        }


        CommonProperties build(){
            for (String mandatory : MANDATORY_PROPERTIES){
                if (commonProperties.isPropertyEmpty(mandatory)){
                    throw new IllegalStateException("Mandatory property " + mandatory
                            + " is absent for " + TAG + " bundle");
                }
            }
            //All Mandatory properties present, add other common properties using context
            commonProperties.put(EVENT_ID, UUID.randomUUID().toString());
            return commonProperties;
        }
    }

}
