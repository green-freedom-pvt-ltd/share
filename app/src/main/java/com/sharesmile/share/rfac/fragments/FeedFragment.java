package com.sharesmile.share.rfac.fragments;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.ServerTimeKeeper;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.Constants.PREF_LAST_TIME_FEED_WAS_SEEN;

/**
 * Created by ankitmaheshwari on 1/10/18.
 */

public class FeedFragment extends BaseFragment {

    private static final String TAG = "FeedFragment";

    @BindView(R.id.wv_main)
    WebView webview;

    @BindView(R.id.feed_progress_bar)
    ProgressBar progressBar;

    public static final String KEY_URL = "key_url";

    public static FeedFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, null);
        ButterKnife.bind(this, view);
        init(getArguments().getString(KEY_URL));
        return view;
    }

    private void init(String urlToLoad){
        setupWebView(urlToLoad);
        getFragmentController().updateToolBar(getString(R.string.title_feed), true);
    }

    private void setupWebView(String urlToLoad){
        webview.getSettings().setJavaScriptEnabled(true); // enable javascript
        webview.setWebViewClient(webViewCleint);
        webview.loadUrl(urlToLoad);
    }

    WebViewClient webViewCleint = new WebViewClient(){

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Logger.d(TAG, "onPageStarted, loadedUrl: " + url);
            super.onPageStarted(view, url, favicon);
            webview.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrl(url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUrl(request.getUrl().toString());
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            Logger.i(TAG, "onPageFinished: " + url);
            super.onPageFinished(view, url);
            if (Urls.isFeedArticlesListUrl(url)){
                Logger.d(TAG, "It was feed's list url");
                // Will rest Feed last seen timestamp
                SharedPrefsManager.getInstance().setLong(PREF_LAST_TIME_FEED_WAS_SEEN,
                        ServerTimeKeeper.getServerTimeStampInMillis());
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_NEW_FEED_ARTICLE_AVAILABLE, false);
            }
            webview.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

    };

    private boolean handleUrl(String url) {
        Logger.i(TAG, "Loading: " + url);
        // Based on some condition you need to determine if you are going to load the loadedUrl
        // in your web view itself or in a browser.
        // You can use `host` or `scheme` or any part of the `uri` to decide.
        if (true) {
            // Returning false means that you are going to load this loadedUrl in the webView itself
            return false;
        } else {
            // Returning true means that you need to handle what to do with the loadedUrl
            // e.g. open web page in a Browser
            return true;
        }
    }

    @Override
    protected boolean handleBackPress() {
        Logger.d(TAG, "handleBackPress");
        if (isShowingArticleDetailPage()){
            Logger.d(TAG, "Returning true as detail page is on display");
            setupWebView(Urls.getFeedUrl());
            return true;
        }
        return super.handleBackPress();
    }

    private boolean isShowingArticleDetailPage(){
        String loadedUrl = webview.getUrl();
        return Urls.isFeedArticleDetailUrl(loadedUrl);
    }



}
