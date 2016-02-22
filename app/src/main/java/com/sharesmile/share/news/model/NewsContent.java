package com.sharesmile.share.news.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public class NewsContent implements UnObfuscable{

    private static final String TAG = "NewsContent";

    private String brief;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("content_url")
    private String contentUrl;

    public String getBrief(){
        return brief;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getContentUrl() {
        return contentUrl;
    }

}
