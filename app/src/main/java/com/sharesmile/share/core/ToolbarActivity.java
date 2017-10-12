package com.sharesmile.share.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sharesmile.share.R;

/**
 * Created by ankitmaheshwari on 9/4/17.
 */

public abstract class ToolbarActivity extends BaseActivity {

    private static final String TAG = "ToolbarActivity";

    Toolbar toolbar;

    TextView mToolbarTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_main_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract int getLayoutId();

    @Override
    public void setToolbarTitle(String title){
        mToolbarTitle.setText(title);
    }

    @Override
    public void updateToolBar(String title, boolean showAsUpEnable) {
        setToolbarTitle(title);
        showToolbar();
    }

    public Toolbar getToolbar(){
        return toolbar;
    }

    @Override
    public void showToolbar() {
        if (toolbar != null){
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideToolbar() {
        if (toolbar != null){
            toolbar.setVisibility(View.GONE);
        }
    }
}
