package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 1/10/18.
 */

public class FeedFragment extends BaseFragment {

    private static final String TAG = "FeedFragment";

    @BindView(R.id.wv_main)
    WebView webview;

    public static FeedFragment newInstance() {

        Bundle args = new Bundle();

        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, null);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init(){
        webview.getSettings().setJavaScriptEnabled(true); // enable javascript
        webview.loadUrl("http://blog.impactapp.in/");
        getFragmentController().updateToolBar(getString(R.string.title_feed), true);
    }


}
