package com.sharesmile.share.utils;

/**
 * Created by Shine on 01/05/16.
 */
public class Urls {

    private static final String BASE_URL = "http://139.59.243.247";

    private static final String CAUSE_LIST_URL = "/api/causes.json";
    private static final String RUN_URL = "/api/runs";

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getCauseListUrl() {
        String url = getBaseUrl() + CAUSE_LIST_URL;
        return url;
    }
    
    public static String getRunUrl(){
        String url = getBaseUrl() + RUN_URL;
        return url;
    }
}
