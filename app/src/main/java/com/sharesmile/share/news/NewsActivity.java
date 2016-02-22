package com.sharesmile.share.news;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseActivity;

public class NewsActivity extends BaseActivity{

    private static String TAG = "NewsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_news);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void loadInitialFragment() {
        addFragment(NewsFragment.newInstance());
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.newsFrameLayout;
    }

    @Override
    public String getName() {
        return TAG;
    }
}
