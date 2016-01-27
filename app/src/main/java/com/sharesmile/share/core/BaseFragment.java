package com.sharesmile.share.core;

import android.support.v4.app.Fragment;

/**
 * Created by ankitmaheshwari1 on 11/01/16.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    public String getName(){
        return this.getClass().getCanonicalName();
    }
}
