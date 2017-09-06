package com.sharesmile.share.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sharesmile.share.R;

import butterknife.BindView;

/**
 * Created by ankitmaheshwari on 9/4/17.
 */

public abstract class ToolbarActivity extends BaseActivity {

    private static final String TAG = "ToolbarActivity";

    Toolbar toolbar;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
    }

    public Toolbar getToolbar(){
        return toolbar;
    }

    @Override
    public void performOperation(int operationId, Object input) {
        switch (operationId) {
            case HIDE_TOOLBAR:
                if (toolbar != null){
                    toolbar.setVisibility(View.GONE);
                }
                break;
            default:
                super.performOperation(operationId, input);
        }
    }

}
