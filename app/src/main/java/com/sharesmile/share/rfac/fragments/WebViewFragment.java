package com.sharesmile.share.rfac.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.utils.Urls;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shine on 04/06/16.
 */
public class WebViewFragment extends BaseFragment {


    public static final int DISPLAY_ABOUT_US = 0;
    public static final int DISPLAY_FAQ = 1;

    private static final String bundle_display_mode = "display_mode";

    public static BaseFragment getInstance(int displayMode) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle arg = new Bundle();
        arg.putInt(bundle_display_mode, displayMode);
        fragment.setArguments(arg);
        return fragment;
    }

    @BindView(R.id.web_view)
    WebView mWebView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPage();
    }

    public void loadPage() {

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        Resources res = getResources();
        webSettings.setTextZoom(95);
        final Activity activity = getActivity();
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                //activity.setProgress(progress * 1000);
            }
        });

        String url;
        if (getArguments().getInt(bundle_display_mode) == DISPLAY_ABOUT_US) {
            url = Urls.getAboutUsUrl();
            getFragmentController().updateToolBar(getString(R.string.title_about_us), true);
        } else {
            url = Urls.getFaqUrl();
            getFragmentController().updateToolBar(getString(R.string.title_faq), true);
        }
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {

            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        });

    }
}
