package com.sharesmile.share.home.howitworks.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

import java.util.List;

/**
 * Created by ankitmaheshwari on 1/31/18.
 */

public class HowItWorksResponse implements UnObfuscable{

    @SerializedName("how_it_works_steps")
    private List<HowItWorksRowItem> steps;

    public List<HowItWorksRowItem> getSteps() {
        return steps;
    }
}
