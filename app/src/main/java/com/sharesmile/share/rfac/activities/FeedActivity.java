package com.sharesmile.share.rfac.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.sharesmile.share.R;
import com.sharesmile.share.core.PermissionCallback;
import com.sharesmile.share.core.ToolbarActivity;
import com.sharesmile.share.rfac.fragments.FeedFragment;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 1/10/18.
 */

public class FeedActivity extends ToolbarActivity {

    private static final String TAG = "FeedActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            loadInitialFragment();
        }
    }

    public void loadInitialFragment() {
        addFragment(FeedFragment.newInstance(), false);
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.feed_main_frame_layout;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public void requestPermission(int requestCode, PermissionCallback permissionsCallback) {

    }

    @Override
    public void unregisterForPermissionRequest(int requestCode) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_feed;
    }
}
