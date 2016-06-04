package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

import java.util.List;

/**
 * Created by Shine on 01/05/16.
 */
public class CauseList implements UnObfuscable {

    private static final String TAG = CauseList.class.getSimpleName();

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private int next;

    @SerializedName("previous")
    private int previous;

    @SerializedName("results")
    List<CauseData> causes;

    public int getPageNum() {
        return count;
    }

    public List<CauseData> getCauses() {
        return causes;
    }

    public void setCauses(List<CauseData> causes) {
        this.causes = causes;
    }

}