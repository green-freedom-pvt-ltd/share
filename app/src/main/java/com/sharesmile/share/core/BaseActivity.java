package com.sharesmile.share.core;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.google.android.gms.common.api.Status;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.gps.GoogleLocationTracker;
import com.sharesmile.share.rfac.activities.MainActivity;
import com.sharesmile.share.rfac.fragments.FeedbackFragment;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import activities.ImpactLeagueActivity;
import fragments.MessageCenterFragment;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements IFragmentController {

    private static final String TAG = "BaseActivity";
    private static final int REQUEST_CHECK_SETTINGS = 102;
    public static final int REQUEST_LEAGUE_REGISTRATION = 103;

    private static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private CauseData mCauseData;

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCauseData = (CauseData) getIntent().getSerializableExtra(BUNDLE_CAUSE_DATA);
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
                    if ( !GoogleLocationTracker.getInstance().isFetchingLocation() ){
                        GoogleLocationTracker.getInstance().register(googleLocationTrackerListener);
                    }else {
                        showTrackingActivity();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case START_RUN_TEST:
                if (input instanceof CauseData) {
                    Intent intent = new Intent(this, TrackerActivity.class);
                    intent.putExtra(TrackerActivity.BUNDLE_CAUSE_DATA, (CauseData) input);
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.CODE_REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GoogleLocationTracker.getInstance().onPermissionsGranted();
            } else if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_DENIED){
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
        }
    }

    private void showTrackingActivity() {
        Logger.d(TAG, "showTrackingActivity: Will start TrackerActivity");
        Intent intent = new Intent(this, TrackerActivity.class);
        intent.putExtra(TrackerActivity.BUNDLE_CAUSE_DATA, mCauseData);
        startActivity(intent);
    }

    private static boolean blockRequestPermission = false;
    private static boolean blockLocationEnablePopup = false;

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

                }
            }
        }
    };

    private void handleFixLocationSettingsBroadcast(Bundle bundle){
        synchronized (BaseActivity.class){
            if (!blockLocationEnablePopup){
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

    private void handleRequestPermissionBroadcast(){
        synchronized (BaseActivity.class){
            if (!blockRequestPermission){
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


    public void showMessageCenter() {
        replaceFragment(new MessageCenterFragment(), true);
    }

    public void showFeedBackDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.post_fun_feedback_title)).setMessage(getString(R.string.post_fun_feedback_msg));
        builder.setPositiveButton("Great", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.redirectToPlayStore(BaseActivity.this);
            }
        });
        builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                replaceFragment(new FeedbackFragment(), true);
            }
        });

        builder.show();

    }
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationTrackerReceiver);
        super.onDestroy();
    }

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

}
