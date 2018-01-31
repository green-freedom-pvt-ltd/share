package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ankitmaheshwari on 1/31/18.
 */

public class HowItWorksRowItem {

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("image_url")
    private String imageUrl;

    public HowItWorksRowItem(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
