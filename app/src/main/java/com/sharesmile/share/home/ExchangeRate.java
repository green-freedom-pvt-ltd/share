package com.sharesmile.share.home;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ankitmaheshwari on 10/12/17.
 */

public class ExchangeRate {

    @SerializedName("currency")
    private String currencyCode;
    @SerializedName("rate")
    private float rate;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
