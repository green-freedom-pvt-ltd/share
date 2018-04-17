package com.sharesmile.share.onboarding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.base.BaseActivity;
import com.sharesmile.share.core.base.PermissionCallback;
import com.sharesmile.share.onboarding.Fragments.FragmentBirthday;
import com.sharesmile.share.onboarding.Fragments.FragmentGender;
import com.sharesmile.share.onboarding.Fragments.FragmentHeight;
import com.sharesmile.share.onboarding.Fragments.FragmentWeight;
import com.sharesmile.share.onboarding.Fragments.FragmentWelcome;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class OnBoardingActivity extends BaseActivity implements CommonActions {

    @BindView(R.id.screen_explain_tv)
    TextView screenExplainTv;

    @BindView(R.id.back_tv)
    TextView backTv;

    @BindView(R.id.continue_tv)
    TextView continueTv;

    @BindView(R.id.onboarding_question_tv)
    TextView onboardingQuestion;

    @BindView(R.id.progress_lt)
    LinearLayout progressLayout;

    private static final String TAG = "OnBoardingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        ButterKnife.bind(this);
        loadInitialFragment();
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.main_frame_layout_onboarding;
    }


    @Override
    public String getName() {
        return TAG;
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

    }

    @Override
    public void updateToolBar(String title, boolean showAsUpEnable) {

    }

    @Override
    public void showToolbar() {

    }

    @Override
    public void hideToolbar() {

    }

    @Override
    public void setToolbarElevation(float dpValue) {

    }

    private void loadInitialFragment() {
        addFragment(new FragmentWelcome(), true);
    }

    @OnClick({R.id.continue_tv, R.id.back_tv})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.continue_tv:
                continueAction(getSupportFragmentManager().findFragmentByTag(getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName()));
                break;
            case R.id.back_tv:
                backAction(getSupportFragmentManager().findFragmentByTag(getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName()));
                break;
        }
    }

    private void backAction(Fragment fragmentByTag) {
        getSupportFragmentManager().popBackStack();
        Handler postPopBackStack = new Handler();
        postPopBackStack.postDelayed(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1).getName());
                if(fragment instanceof FragmentWelcome)
                {
                    setBackAndContinue(FragmentWelcome.TAG);
                }
            }
        },100);
    }

    private void continueAction(Fragment fragment) {
        if (fragment instanceof FragmentWelcome) {
            replaceFragment(new FragmentGender(), true);
        }else
        {
            if(continueTv.getCurrentTextColor() == getResources().getColor(R.color.white_10))
            {

            }else
            {
                if(fragment instanceof FragmentGender)
                {
                    replaceFragment(new FragmentWeight(), true);
                }else if(fragment instanceof FragmentWeight)
                {
                    replaceFragment(new FragmentHeight(), true);
                }else if(fragment instanceof FragmentHeight)
                {
                    replaceFragment(new FragmentBirthday(), true);
                }
            }
        }


    }

    @Override
    public void setExplainText(String question,String text) {
        onboardingQuestion.setText(question);
        screenExplainTv.setText(text);
    }

    @Override
    public void setBackAndContinue(String name) {
        onboardingQuestion.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.VISIBLE);
        continueTv.setText(getString(R.string.continue_txt));
        continueTv.setTextColor(getResources().getColor(R.color.white));
        switch (name) {
            case FragmentWelcome.TAG:
                backTv.setVisibility(View.INVISIBLE);
                continueTv.setVisibility(View.VISIBLE);
                continueTv.setText(getString(R.string.start_my_journey_txt));
                onboardingQuestion.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
                break;
            case FragmentGender.TAG:
                backTv.setVisibility(View.VISIBLE);
                continueTv.setVisibility(View.VISIBLE);
//                continueTv.setTextColor(getResources().getColor(R.color.white_10));
                break;
            case FragmentWeight.TAG :
                backTv.setVisibility(View.VISIBLE);
                continueTv.setVisibility(View.VISIBLE);
                break;
        }
    }
}
