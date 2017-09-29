package com.sharesmile.share.utils;

/**
 * Created by ankitmaheshwari1 on 08/01/16.
 */
public class BasicNameValuePair implements NameValuePair {

    private static final String TAG = "BasicNameValuePair";

    private String name;
    private String value;

    public BasicNameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "BasicNameValuePair{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
