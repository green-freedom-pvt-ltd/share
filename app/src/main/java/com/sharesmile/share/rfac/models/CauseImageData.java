package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ankitmaheshwari on 6/27/17.
 */

public class CauseImageData {

    @SerializedName("cause_thank_you_image")
    private String image;

    @SerializedName("cause_thank_you_title")
    private String titleTemplate;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }
}
