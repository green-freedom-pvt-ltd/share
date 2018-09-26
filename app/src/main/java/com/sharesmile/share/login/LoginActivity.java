package com.sharesmile.share.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.ExpoBackoffTask;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.refer_program.model.ReferProgram;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by apurvgandhwani on 4/5/2016.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, LoginImpl.LoginListener {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_GOOGLE_SIGN_IN = 1001;

    @BindView(R.id.bt_fb_login)
    LinearLayout mFbLoginButton;

    @BindView(R.id.bt_google_login)
    LinearLayout mGoogleLoginButton;

    @BindView(R.id.container_login_skip)
    View skipContainer;

    @BindView(R.id.login_container)
    LinearLayout mLoginContainer;

    @BindView(R.id.progress_container)
    LinearLayout mProgressContainer;
    private LoginImpl mLoginHandler;

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginHandler = new LoginImpl(this, this);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        initUi();

    }


    private void initUi() {

        skipContainer.setOnClickListener(this);
        mFbLoginButton.setOnClickListener(this);
        mGoogleLoginButton.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        AnalyticsEvent.create(Event.ON_LOAD_LOGIN_SCREEN)
                .buildAndDispatch();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_fb_login:
                mLoginHandler.performFbLogin();
                AnalyticsEvent.create(Event.ON_CLICK_LOGIN_BUTTON)
                        .put("medium", "fb")
                        .buildAndDispatch();
                break;
            case R.id.container_login_skip:
                SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
                prefsManager.setBoolean(Constants.PREF_LOGIN_SKIP, true);
                exitWithResult(false);
                AnalyticsEvent.create(Event.ON_CLICK_LOGIN_SKIP)
                        .buildAndDispatch();
                break;
            case R.id.bt_google_login:

                mLoginHandler.performGoogleLogin();
                AnalyticsEvent.create(Event.ON_CLICK_LOGIN_BUTTON)
                        .put("medium", "gplus")
                        .buildAndDispatch();
                break;

        }
    }

    private void exitWithResult(boolean loginSuccess){
        if(loginSuccess)
            Toast.makeText(MainApplication.getContext(), "Logged in as " + MainApplication.getInstance().getUserDetails().getFirstName(), Toast.LENGTH_SHORT).show();

        if (getCallingActivity() != null){
            Logger.d(TAG, "Will return result loginSuccess = " + loginSuccess);
            // LoginActivity was started for result
            if (loginSuccess){
                setResult(RESULT_OK, null);
            }else {
                setResult(RESULT_CANCELED, null);
            }
            finish();
        }else {
            startMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mLoginHandler != null){
            mLoginHandler.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mLoginHandler != null){
            mLoginHandler.disconnect();
        }
        super.onDestroy();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onLoginSuccess() {
        exitWithResult(true);
    }

    @Override
    public void showHideProgress(boolean show, String title) {
        if (show) {
            mLoginContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.VISIBLE);
        } else {
            mLoginContainer.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onEvent(UpdateEvent.OnGetStreak onGetStreak)
    {
        if(onGetStreak.result == ExpoBackoffTask.RESULT_SUCCESS) {
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_IS_LOGIN, true);
            SyncHelper.forceRefreshEntireWorkoutHistory();
            onLoginSuccess();
        } else if (onGetStreak.result == -1) {
            if (ReferProgram.getReferProgramDetails() == null) {
                ReferProgram.syncDetails();
            }
        }else {
            MainApplication.showToast(getResources().getString(R.string.login_error));
        }
    }

}
