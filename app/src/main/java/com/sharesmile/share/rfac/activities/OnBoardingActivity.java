package com.sharesmile.share.rfac.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseActivity;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.PermissionCallback;
import com.sharesmile.share.rfac.adapters.OnBoardingAdapter;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OnBoardingActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.done)
    TextView done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_on_boarding);
        ButterKnife.bind(this);
        mViewPager.setAdapter(new OnBoardingAdapter(getSupportFragmentManager()));
        CirclePageIndicator pageIndicator = (CirclePageIndicator) findViewById(R.id.circularBarPager);
        pageIndicator.setViewPager(mViewPager);
        pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public int selectedIndex = 0;
            public boolean mPageEnd;
            boolean callHappened;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mPageEnd && position == mViewPager.getAdapter().getCount() - 1 && !callHappened) {
                    mPageEnd = false;
                    callHappened = true;
                    startLoginActivity();
                } else {
                    mPageEnd = false;
                }
            }

            @Override
            public void onPageSelected(int position) {
                selectedIndex = position;
                done.setVisibility(position == 3 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (selectedIndex == mViewPager.getAdapter().getCount() - 1) {
                    mPageEnd = true;
                }
            }
        });
        done.setOnClickListener(this);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_FIRST_TIME_USER, false);
        startActivity(intent);
    }

    @Override
    public int getFrameLayoutId() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void exit() {

    }

    @Override
    public void requestPermission(int requestCode, PermissionCallback permissionsCallback) {

    }

    @Override
    public void unregisterForPermissionRequest(int requestCode) {

    }

    @Override
    public void setToolbarTitle(String toolbarTitle) {
        // Do Nothing
    }

    @Override
    public void updateToolBar(String title, boolean showAsUpEnable) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done:
                startLoginActivity();
                break;
        }
    }
}
