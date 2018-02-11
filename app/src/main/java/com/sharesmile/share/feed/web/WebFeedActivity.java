package com.sharesmile.share.feed.web;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.base.PermissionCallback;
import com.sharesmile.share.core.base.ToolbarActivity;

import static com.sharesmile.share.core.Constants.FEED_WEBVIEW_DEFAULT_URL;

/**
 * Not being used right now
 * Created by ankitmaheshwari on 1/10/18.
 */

public class WebFeedActivity extends ToolbarActivity {

    private static final String TAG = "WebFeedActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (isDeepLink()){
            handleDeeplink();
        }else{
            loadInitialFragment(FEED_WEBVIEW_DEFAULT_URL);
        }
    }

    public void loadInitialFragment(String webViewUrl) {
        addFragment(WebFeedFragment.newInstance(webViewUrl), false);
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

    private void handleDeeplink(){
        Logger.d(TAG, "Coming from Deeplink, url: " + getIntent().getData().toString());
        loadInitialFragment(getIntent().getData().toString());
    }

    public boolean isDeepLink(){
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null){
            return true;
        }
        return false;
    }
}
