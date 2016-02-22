package com.sharesmile.share.news.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public class News implements UnObfuscable{

    private static final String TAG = "News";

    @SerializedName("news_id")
    private int id;

    @SerializedName("title")
    private String name;

    @SerializedName("source_name")
    private String sourceName;

    @SerializedName("source_url")
    private String sourceUrl;

    @SerializedName("curator_name")
    private String curatorName;

    @SerializedName("author_name")
    private String authorName;

    @SerializedName("content_type")
    private String contentType;

    @SerializedName("pub_date")
    private long pubDate;

    private NewsContent content;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getCuratorName() {
        return curatorName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getPubDate() {
        return pubDate;
    }

    public NewsContent getContent() {
        return content;
    }

}
