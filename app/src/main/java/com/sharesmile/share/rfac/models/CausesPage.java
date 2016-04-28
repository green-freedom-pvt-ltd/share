package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

import java.util.List;

/**
 * Created by ankitmaheshwari1 on 09/03/16.
 */
public class CausesPage implements UnObfuscable {

    private static final String TAG = "CausesPage";

    @SerializedName("pg_num")
    private int pageNum;

    List<Cause> causes;

    public int getPageNum() {
        return pageNum;
    }

    public List<Cause> getCauses() {
        return causes;
    }
}
