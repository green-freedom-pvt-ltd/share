package com.sharesmile.share.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseActivity;
import com.sharesmile.share.core.base.PermissionCallback;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.onboarding.fragments.FragmentAskReminder;
import com.sharesmile.share.onboarding.fragments.FragmentBirthday;
import com.sharesmile.share.onboarding.fragments.FragmentGender;
import com.sharesmile.share.onboarding.fragments.FragmentGoals;
import com.sharesmile.share.onboarding.fragments.FragmentHeight;
import com.sharesmile.share.onboarding.fragments.FragmentSetReminder;
import com.sharesmile.share.onboarding.fragments.FragmentThankYou;
import com.sharesmile.share.onboarding.fragments.FragmentWeight;
import com.sharesmile.share.onboarding.fragments.FragmentWelcome;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sharesmile.share.core.Constants.PREF_DISABLE_ALERTS;


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

    @BindView(R.id.level_progress_bar)
    View levelProgressBar;

    private static final String TAG = "OnBoardingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, true);
        setContentView(R.layout.activity_onboarding);
        ButterKnife.bind(this);
        MainApplication.getInstance().setGoalDetails(null);
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
        addFragment(new FragmentWelcome(), false);
    }

    @OnClick({R.id.continue_tv, R.id.back_tv})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.continue_tv:
                int i = getSupportFragmentManager().getBackStackEntryCount();
                if (i != 0)
                    continueAction(getSupportFragmentManager().findFragmentByTag(getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName()));
                else
                    continueAction(null);
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
                int i = getSupportFragmentManager().getBackStackEntryCount();

                if (i == 0) {
                    setBackAndContinue(FragmentWelcome.TAG, getResources().getString(R.string.start_my_journey_txt));
                } else {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName());
                }
            }
        }, 100);
    }

    private void continueAction(Fragment fragment) {
        if (fragment instanceof FragmentWelcome || fragment == null) {
            replaceFragment(new FragmentGender(), true);
            AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_WELCOME_CONTINUE).buildAndDispatch();
        } else {
            if (continueTv.getCurrentTextColor() == getResources().getColor(R.color.white_10)) {
                MainApplication.showToast(getResources().getString(R.string.select_an_option));
            } else {
                if (fragment instanceof FragmentGender) {
                    replaceFragment(new FragmentWeight(), true);
                    AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_GENDER_CONTINUE).buildAndDispatch();
                } else if (fragment instanceof FragmentWeight) {
                    replaceFragment(new FragmentHeight(), true);
                    AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_WEIGHT_CONTINUE).buildAndDispatch();
                } else if (fragment instanceof FragmentHeight) {
                    replaceFragment(new FragmentBirthday(), true);
                    AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_HEIGHT_CONTINUE).buildAndDispatch();
                } else if (fragment instanceof FragmentBirthday) {
                    replaceFragment(new FragmentGoals(), true);
                    AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_BIRTHDAY_CONTINUE).buildAndDispatch();
                } else if (fragment instanceof FragmentGoals) {
                    replaceFragment(new FragmentAskReminder(), true);
                    AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_GOAL_CONTINUE).buildAndDispatch();
                } else if (fragment instanceof FragmentAskReminder) {
                    String continueText = continueTv.getText().toString();
                    if (continueText.equalsIgnoreCase(getResources().getString(R.string.set_reminder))) {
                        replaceFragment(new FragmentSetReminder(), true);
                        AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_YES_ASK_REMINDER_VALUE_SELECT_CONTINUE).buildAndDispatch();
                    } else if (continueText.equalsIgnoreCase(getResources().getString(R.string.continue_txt))) {
                        Utils.setReminderTime("", this);
                        replaceFragment(new FragmentThankYou(), true);
                        AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_NO_ASK_REMINDER_VALUE_SELECT_CONTINUE).buildAndDispatch();
                    }
                } else if (fragment instanceof FragmentSetReminder) {
                    replaceFragment(new FragmentThankYou(), true);
                    AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_SET_REMINDER_CONTINUE).buildAndDispatch();
                } else if (fragment instanceof FragmentThankYou) {
                    SyncHelper.oneTimeUploadUserData();
                    AnalyticsEvent.create(Event.ON_CLICK_ONBOARDING_THANK_YOU_CONTINUE).buildAndDispatch();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Utils.setOnboardingShown();
                    finish();
                }
            }
        }


    }

    @Override
    public void setExplainText(String question, String text) {
        onboardingQuestion.setText(question);
        screenExplainTv.setText(text);
    }

    @Override
    public void setBackAndContinue(String name, String continueTextName) {
        onboardingQuestion.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.VISIBLE);
        continueTv.setText(getString(R.string.continue_txt));

        backTv.setVisibility(View.VISIBLE);
        continueTv.setVisibility(View.VISIBLE);
        setContinueTextColor(R.color.white);
        switch (name) {
            case FragmentWelcome.TAG:
                backTv.setVisibility(View.INVISIBLE);
                continueTv.setVisibility(View.VISIBLE);
                continueTv.setText(continueTextName);
                onboardingQuestion.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
                break;
            case FragmentGender.TAG:
                setProgressLevel(1.0f);
                setContinueTextColor(R.color.white_10);
                break;
            case FragmentWeight.TAG:
                setProgressLevel(2.0f);
                break;
            case FragmentHeight.TAG:
                setProgressLevel(3.0f);
                break;
            case FragmentBirthday.TAG:
                setProgressLevel(4.0f);
                break;
            case FragmentGoals.TAG:
                setProgressLevel(5.0f);
                break;
            case FragmentAskReminder.TAG:
//                setProgressLevel(6.0f);
                setContinueTextColor(R.color.white_10);
                continueTv.setText(continueTextName);
                break;
            case FragmentSetReminder.TAG:
                setProgressLevel(7.0f);
                break;
            case FragmentThankYou.TAG:
                continueTv.setText(continueTextName);
                onboardingQuestion.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void setContinueTextColor(int color) {
        continueTv.setTextColor(getResources().getColor(color));
    }

    public void setProgressLevel(float weight) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight / 7);
        levelProgressBar.setLayoutParams(layoutParams);
    }
}
