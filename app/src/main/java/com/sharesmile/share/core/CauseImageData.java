package com.sharesmile.share.core;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari on 6/27/17.
 */

public class CauseImageData implements UnObfuscable, Serializable {

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
