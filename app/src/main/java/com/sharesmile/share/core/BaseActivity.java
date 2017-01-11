package com.sharesmile.share.core;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.gps.GoogleLocationTracker;
import com.sharesmile.share.rfac.activities.MainActivity;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;


/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements IFragmentController {

    private static final String TAG = "BaseActivity";
    private static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private CauseData mCauseData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCauseData = (CauseData) getIntent().getSerializableExtra(BUNDLE_CAUSE_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationTrackerReceiver,
                new IntentFilter(Constants.LOCATION_TRACKER_BROADCAST_ACTION));
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

    @Override
    public void performOperation(int operationId, Object input) {
        switch (operationId) {
            case START_RUN:
                if (input instanceof CauseData) {
                    mCauseData = (CauseData) input;
                    showTrackingActivity();
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.CODE_REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GoogleLocationTracker.getInstance().onPermissionsGranted();
            } else {
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
        Intent intent = new Intent(this, TrackerActivity.class);
        intent.putExtra(TrackerActivity.BUNDLE_CAUSE_DATA, mCauseData);
        startActivity(intent);
    }

    private BroadcastReceiver locationTrackerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int broadcastCategory = bundle
                        .getInt(Constants.LOCATION_TRACKER_BROADCAST_CATEGORY);
                switch (broadcastCategory) {
                    case Constants.BROADCAST_FIX_LOCATION_SETTINGS_CODE:
                        Status status = bundle.getParcelable(Constants.KEY_LOCATION_SETTINGS_PARCELABLE);
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(BaseActivity.this,
                                    Constants.CODE_LOCATION_SETTINGS_RESOLUTION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case Constants.BROADCAST_REQUEST_PERMISSION_CODE:
                        ActivityCompat.requestPermissions(BaseActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                Constants.CODE_REQUEST_LOCATION_PERMISSION);
                        break;

                }
            }
        }
    };


    private void showMessageCenter() {
        replaceFragment(new MessageCenterFragment(), true);
    }

}
