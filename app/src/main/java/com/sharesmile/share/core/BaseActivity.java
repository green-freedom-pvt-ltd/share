package com.sharesmile.share.core;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.sharesmile.share.CauseDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.gps.GoogleLocationTracker;
import com.sharesmile.share.rfac.activities.FeedbackActivity;
import com.sharesmile.share.rfac.activities.MainActivity;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.FeedbackCategory;
import com.sharesmile.share.rfac.models.Run;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import activities.ImpactLeagueActivity;
import fragments.MessageCenterFragment;

import static com.sharesmile.share.core.Constants.CODE_GOOGLE_PLAY_SERVICES_RESOLUTION;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements IFragmentController {

    private static final String TAG = "BaseActivity";
    private static final int REQUEST_CHECK_SETTINGS = 102;
    public static final int REQUEST_LEAGUE_REGISTRATION = 103;

    private static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private CauseData mCauseData;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCauseData = (CauseData) getIntent().getSerializableExtra(BUNDLE_CAUSE_DATA);
        if (!CauseDataStore.getInstance().isCauseAvailableForRun(mCauseData)){
            mCauseData = CauseDataStore.getInstance().getFirstCause();
        }
        IntentFilter filter = new IntentFilter(Constants.LOCATION_TRACKER_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationTrackerReceiver, filter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_CAUSE_DATA, mCauseData);
    }

    @Override
    public void addFragment(BaseFragment fragmentToBeLoaded, boolean addToBackStack) {
        boolean allowStateLoss = true;

        if (!getSupportFragmentManager().isDestroyed()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .add(getFrameLayoutId(), fragmentToBeLoaded, fragmentToBeLoaded.getName());
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(fragmentToBeLoaded.getName());
            }
            if (allowStateLoss) {
                fragmentTransaction.commitAllowingStateLoss();
            } else {
                fragmentTransaction.commit();
            }
        } else {
            Logger.e(getName(), "addFragmen: Actvity Destroyed, won't perform FT to load" +
                    " Fragment " + fragmentToBeLoaded.getName());
        }
    }

    @Override
    public void replaceFragment(BaseFragment fragmentToBeLoaded, boolean addToBackStack) {
        boolean allowStateLoss = true;

        if (!getSupportFragmentManager().isDestroyed()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(getFrameLayoutId(), fragmentToBeLoaded,
                    fragmentToBeLoaded.getName());
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(fragmentToBeLoaded.getName());
            }
            if (allowStateLoss) {
                fragmentTransaction.commitAllowingStateLoss();
            } else {
                fragmentTransaction.commit();
            }
        } else {
            Logger.e(getName(), "replaceFragment: Actvity Destroyed, won't perform FT to load" +
                    " Fragment " + fragmentToBeLoaded.getName());
        }
    }

    private GoogleLocationTracker.Listener googleLocationTrackerListener
            = new GoogleLocationTracker.Listener() {
        @Override
        public void onLocationTrackerReady() {
            Logger.i(TAG, "onLocationTrackerReady, will start tracking activity");
            GoogleLocationTracker.getInstance().unregister(this);
            showTrackingActivity();
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onPermissionDenied() {
            Logger.i(TAG, "onPermissionDenied: Can't do nothing");
            GoogleLocationTracker.getInstance().unregister(this);
        }

        @Override
        public void onConnectionFailure() {
            Logger.i(TAG, "onConnectionFailure");
            GoogleLocationTracker.getInstance().unregister(this);
        }

        @Override
        public void onGpsEnabled() {

        }

        @Override
        public void onGpsDisabled() {

        }
    };

    @Override
    public void performOperation(int operationId, Object input) {
        switch (operationId) {
            case START_RUN:
                if (input instanceof CauseData) {
                    mCauseData = (CauseData) input;
                    if (!GoogleLocationTracker.getInstance().isFetchingLocation()) {
                        GoogleLocationTracker.getInstance().register(googleLocationTrackerListener);
                    } else {
                        showTrackingActivity();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case START_RUN_TEST:
                if (input instanceof CauseData) {
                    Intent intent = new Intent(this, TrackerActivity.class);
                    intent.putExtra(BUNDLE_CAUSE_DATA, (CauseData) input);
                    SharedPrefsManager.getInstance().setBoolean(Constants.KEY_WORKOUT_TEST_MODE_ON, true);
                    startActivity(intent);
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case START_MAIN_ACTIVITY:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            case SHOW_MESSAGE_CENTER:
                showMessageCenter();
                break;
            case SHOW_LEAGUE_ACTIVITY:
                startActivityForResult(new Intent(this, ImpactLeagueActivity.class), REQUEST_LEAGUE_REGISTRATION);
                break;
            case OPEN_HELP_CENTER:
                openHelpCenter();
                break;
            case TAKE_FLAGGED_RUN_FEEDBACK:
                if (input instanceof Run){
                    takeFlaggedRunFeedback((Run)input);
                }else {
                    throw new IllegalArgumentException("Input should be a Run object for TAKE_FLAGGED_RUN_FEEDBACK");
                }
                break;
            case OPEN_MUSIC_PLAYER:
                openMusicPlayer();
                break;
            case TAKE_POST_RUN_SAD_FEEDBACK:
                if (input instanceof Run){
                    takePostRunSadFeedback((Run)input);
                }else {
                    throw new IllegalArgumentException("Input should be a Run object for TAKE_POST_RUN_SAD_FEEDBACK");
                }
                break;
        }
    }

    @Override
    public void goBack() {
        onBackPressed();
    }

    private void openMusicPlayer(){
        Logger.d(TAG, "openMusicPlayer");
        try{
            if(android.os.Build.VERSION.SDK_INT>=15){
                Intent intent=Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
                        Intent.CATEGORY_APP_MUSIC);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Min SDK 15
                startActivity(intent);
            }else{
                Intent intent = new Intent("android.intent.action.MUSIC_PLAYER");//Min SDK 8
                startActivity(intent);
            }
        }catch (Exception e){
            String message = "Can't find application for playing music: " + e.getMessage();
            Logger.e(TAG, message);
            e.printStackTrace();
            Crashlytics.log(message);
            Crashlytics.logException(e);
            MainApplication.showToast(R.string.cant_find_music_application);
        }
    }

    private void takePostRunSadFeedback(Run concernedRun){
        // Start FeedbackActivty with concernedRun
        Logger.d(TAG, "takePostRunSadFeedback: Will start FeedbackActivity");
        Intent intent = new Intent(this, FeedbackActivity.class);
        intent.putExtra(FeedbackActivity.FEEDBACK_CATEGORY_ARG, FeedbackCategory.POST_RUN_SAD.copy());
        intent.putExtra(FeedbackActivity.FEEDBACK_CONCERNED_RUN_ARG, concernedRun);
        startActivity(intent);
    }

    private void takeFlaggedRunFeedback(Run concernedRun) {
        // Start FeedbackActivty with concernedRun
        Logger.d(TAG, "takeFlaggedRunFeedback: Will start FeedbackActivity");
        Intent intent = new Intent(this, FeedbackActivity.class);
        intent.putExtra(FeedbackActivity.FEEDBACK_CATEGORY_ARG, FeedbackCategory.FLAGGED_RUN_HISTORY.copy());
        intent.putExtra(FeedbackActivity.FEEDBACK_CONCERNED_RUN_ARG, concernedRun);
        startActivity(intent);
    }

    private void openHelpCenter(){
        Logger.d(TAG, "openHelpCenter: Will start FeedbackActivity");
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.CODE_REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GoogleLocationTracker.getInstance().onPermissionsGranted();
            } else if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                GoogleLocationTracker.getInstance().onPermissionsRejected();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.CODE_LOCATION_SETTINGS_RESOLUTION:
                GoogleLocationTracker.getInstance().onActivityResult(requestCode, resultCode, data);
                break;
            case Constants.CODE_GOOGLE_PLAY_SERVICES_RESOLUTION:
                GoogleLocationTracker.getInstance().onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void showTrackingActivity() {
        Logger.d(TAG, "showTrackingActivity: Will start TrackerActivity");
        Intent intent = new Intent(this, TrackerActivity.class);
        intent.putExtra(BUNDLE_CAUSE_DATA, mCauseData);
        startActivity(intent);
    }

    private static boolean blockRequestPermission = false;
    private static boolean blockLocationEnablePopup = false;
    private static boolean blockGooglePlayServicesResolutionPopup = false;

    private BroadcastReceiver locationTrackerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int broadcastCategory = bundle
                        .getInt(Constants.LOCATION_TRACKER_BROADCAST_CATEGORY);
                Logger.d(TAG, "locationTrackerReceiver onReceive with broadcastCategory = " + broadcastCategory);
                switch (broadcastCategory) {
                    case Constants.BROADCAST_FIX_LOCATION_SETTINGS_CODE:
                        handleFixLocationSettingsBroadcast(bundle);
                        break;

                    case Constants.BROADCAST_REQUEST_PERMISSION_CODE:
                        handleRequestPermissionBroadcast();
                        break;

                    case Constants.BROADCAST_FIX_GOOGLE_PLAY_SERVICES_CODE:
                        handleFixGooglePlayServiceBroadcast();
                        break;

                }
            }
        }
    };

    private void handleFixGooglePlayServiceBroadcast(){
        synchronized (BaseActivity.class){
            if (!blockGooglePlayServicesResolutionPopup){
                GoogleApiAvailability api = GoogleApiAvailability.getInstance();
                int code = api.isGooglePlayServicesAvailable(this);
                if (code == ConnectionResult.SUCCESS) {
                    // Everything good
                    GoogleLocationTracker.getInstance().onActivityResult(CODE_GOOGLE_PLAY_SERVICES_RESOLUTION, Activity.RESULT_OK, null);
                } else if (api.isUserResolvableError(code)) {
                    // Show resolution dialog to user and wait for onActivityResult
                    api.showErrorDialogFragment(this, code, CODE_GOOGLE_PLAY_SERVICES_RESOLUTION);
                }
                // Hack to block subsequent location enable broadcast for 1 sec
                blockGooglePlayServicesResolutionPopup = true;
                MainApplication.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        blockGooglePlayServicesResolutionPopup = false;
                    }
                }, 1000);
            }
        }
    }

    private void handleFixLocationSettingsBroadcast(Bundle bundle) {
        synchronized (BaseActivity.class) {
            if (!blockLocationEnablePopup) {
                Status status = bundle.getParcelable(Constants.KEY_LOCATION_SETTINGS_PARCELABLE);
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(BaseActivity.this,
                            Constants.CODE_LOCATION_SETTINGS_RESOLUTION);
                    // Hack to block subsequent location enable broadcast for 1 sec
                    blockLocationEnablePopup = true;
                    MainApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            blockLocationEnablePopup = false;
                        }
                    }, 1000);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
            }
        }
    }

    private void handleRequestPermissionBroadcast() {
        synchronized (BaseActivity.class) {
            if (!blockRequestPermission) {
                ActivityCompat.requestPermissions(BaseActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.CODE_REQUEST_LOCATION_PERMISSION);
                // Hack to block subsequent request permission broadcast for 1 sec
                blockRequestPermission = true;
                MainApplication.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        blockRequestPermission = false;
                    }
                }, 1000);
            }
        }
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(getFrameLayoutId());
    }

    @Override
    public void onBackPressed() {
        // if current fragment is null, or has not handled backpress, handle activity back press
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null && currentFragment instanceof BaseFragment) {
            final BaseFragment baseFragment = (BaseFragment) currentFragment;
            Logger.d(TAG, "onBackPressed: Current fragment is "
                    + baseFragment.getName());
            if (baseFragment.handleBackPress()) {
                Logger.d(TAG, "Current fragment handled back press");
                return;
            }
        }
        super.onBackPressed();
    }

    public void showMessageCenter() {
        replaceFragment(new MessageCenterFragment(), true);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationTrackerReceiver);
        super.onDestroy();
    }

}
