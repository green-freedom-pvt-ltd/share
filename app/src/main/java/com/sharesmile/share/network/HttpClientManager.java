package com.sharesmile.share.network;



import okhttp3.OkHttpClient;

/**
 * Created by ankitmaheshwari on 6/16/17.
 */

public class HttpClientManager {

    private static volatile OkHttpClient okHttpClient = null;

    /**
     * Always use this singleton client of OkHttp. You can call client.newBuilder()
     * to modify it according to your use, but DO NOT create new OkHttp client.
     * @return OkHttpClient
     */
    public static OkHttpClient getDefaultHttpClient() {
        if(okHttpClient == null) {
            synchronized (HttpClientManager.class) {
                if(okHttpClient == null) {
                    okHttpClient = new OkHttpClient();
                }
            }
        }
        return okHttpClient;
    }

}
