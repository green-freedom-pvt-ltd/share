package com.sharesmile.share.core;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.rfac.activities.MainActivity;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements IFragmentController {

    private static final String TAG = "BaseActivity";
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private CauseData mCauseData;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mCauseData = (CauseData) getIntent().getSerializableExtra(BUNDLE_CAUSE_DATA);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
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
                    checkLocationEnabled();
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
        }
    }


    private void checkLocationEnabled() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Config.LOCATION_UPDATE_INTERVAL); // milliseconds
        locationRequest.setSmallestDisplacement(Config.SMALLEST_DISPLACEMENT);
        locationRequest.setFastestInterval(Config.LOCATION_UPDATE_INTERVAL); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        showTrackingActivity();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    BaseActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    showTrackingActivity();
                }
                break;
        }
    }

    private void showTrackingActivity() {
        Intent intent = new Intent(this, TrackerActivity.class);
        intent.putExtra(TrackerActivity.BUNDLE_CAUSE_DATA, mCauseData);
        startActivity(intent);
    }


}
