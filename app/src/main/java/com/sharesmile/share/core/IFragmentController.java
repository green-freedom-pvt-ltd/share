package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari1 on 11/01/16.
 */
public interface IFragmentController {

    static final String TAG = "IFragmentController";

    void addFragment(BaseFragment fragment);

    void replaceFragment(BaseFragment fragment);

    void loadInitialFragment();

    int getFrameLayoutId();

    String getName();

}
