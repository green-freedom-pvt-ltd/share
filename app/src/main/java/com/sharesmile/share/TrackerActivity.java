package com.sharesmile.share;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.sharesmile.share.core.BaseActivity;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.PermissionCallback;
import com.sharesmile.share.gps.GoogleFitStepCounter;
import com.sharesmile.share.gps.WorkoutService;
import com.sharesmile.share.gps.RunTracker;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.RealRunFragment;
import com.sharesmile.share.rfac.RunFragment;
import com.sharesmile.share.rfac.TestRunFragment;
import com.sharesmile.share.rfac.activities.ThankYouActivity;
import com.sharesmile.share.rfac.fragments.StartRunFragment;
import com.sharesmile.share.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TrackerActivity extends BaseActivity {


    private static final String TAG = "TrackerActivity";
    private DrawerLayout drawerLayout;
    private WorkoutService locationService;
    private RunFragment runFragment;
    private boolean runInTestMode;

    private static final int HOME = 0;
    private static final int PROFILE = 1;
    private static final int FEEDBACK = 2;
    private static final int LOGOUT = 3;

    public static final String RUN_IN_TEST_MODE = "run_in_test_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        runInTestMode = getIntent().getBooleanExtra(RUN_IN_TEST_MODE, false);

        loadInitialFragment();

        LocalBroadcastManager.getInstance(this).registerReceiver(locationServiceReceiver,
                new IntentFilter(Constants.LOCATION_SERVICE_BROADCAST_ACTION));

        if (isWorkoutActive()){
            // If workout sesion going on then bind to service
            invokeLocationService();
        }
    }

    public boolean isWorkoutActive(){
        return RunTracker.isWorkoutActive();
    }

    @Override
    public void loadInitialFragment(){
        if (isWorkoutActive()){
            runFragment = createRunFragment();
            addFragment(runFragment, false);
        }else{
            addFragment(StartRunFragment.newInstance(), false);
        }
    }

    private RunFragment createRunFragment(){
        if (runInTestMode){
            return TestRunFragment.newInstance();
        }else{
            return RealRunFragment.newInstance();
        }
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.mainFrameLayout;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void performOperation(int operationId, Object input) {
        super.performOperation(operationId, input);
        switch (operationId){
            case END_RUN_START_COUNTDOWN:
                runFragment = createRunFragment();
                replaceFragment(runFragment, false);
                break;
            case SAY_THANK_YOU:
                Intent intent = new Intent(this, ThankYouActivity.class);
                startActivity(intent);
                finish();
                break;
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isBoundToLocationService()) {
            locationService.onActivityResult(requestCode, resultCode, data);
        }
    }

    public File writeLogsToFile(Context context){

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            // All required permissions available
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM");
            String month = sdf.format(cal.getTime());

            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            int minsOfHour = cal.get(Calendar.MINUTE);

            String fileName = "RFAC_log_" + dayOfMonth + "_" + month
                    + "_" + hourOfDay + ":" + minsOfHour + ".log";

            Logger.d(TAG, "writeLogsToFile " + fileName);

            File outputFile = new File(getLogsFileDir(),fileName);
            String filePath = outputFile.getAbsolutePath();
            try {
                Logger.d(TAG, "filePath " + filePath);
                @SuppressWarnings("unused")
                Process process = Runtime.getRuntime().exec("logcat -v threadtime -f "+ filePath);
            }catch (IOException ioe){
                Logger.e(TAG, "IOException while writing logs to file", ioe);
            }
            return outputFile;

        }else{
            //Need to get permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.CODE_REQUEST_WRITE_PERMISSION);
        }

        return null;

    }

    public static File getLogsFileDir() {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/data/share");
        dir.mkdirs();
        return dir;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBoundToLocationService()){
            unbindLocationService();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationServiceReceiver);
    }

    public void beginLocationTracking(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
            // All required permissions available
            invokeLocationService();
        }else{
            //Need to get permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.CODE_REQUEST_LOCATION_PERMISSION);
        }
    }

    public void endLocationTracking(){
        if (isBoundToLocationService()){
            locationService.stopWorkout();
        }
    }

    public void pauseWorkout(){
        if (isBoundToLocationService()){
            locationService.pause();
        }
    }

    public void resumeWorkout(){
        if (isBoundToLocationService()){
            locationService.resume();
        }
    }

    private void unbindLocationService(){
        Logger.d(TAG, "unbindLocationService");
        unbindService(locationServiceConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.CODE_REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                invokeLocationService();
            } else {
                // Permission was denied or request was cancelled
                Logger.i(TAG, "Location Permission denied, could'nt update the UI");
                Toast.makeText(this, "Please give permission to get Weather", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == Constants.CODE_REQUEST_WRITE_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Can re-capture logs
            } else {
                // Permission was denied or request was cancelled
                Logger.i(TAG, "Location Permission denied, could'nt update the UI");
                Toast.makeText(this, "Please give permission to get Weather", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void invokeLocationService(){
        Log.d(TAG, "invokeLocationService");
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, WorkoutService.class);
        intent.putExtras(bundle);
        startService(intent);
        bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to WorkoutService, cast the IBinder and get WorkoutService instance
            WorkoutService.MyBinder binder = (WorkoutService.MyBinder) service;
            locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            locationService = null;
        }
    };

    public boolean isBoundToLocationService(){
        return (locationService != null);
    }


    private BroadcastReceiver locationServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int broadcastCategory = bundle
                        .getInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY);
                switch (broadcastCategory){
                    case Constants.BROADCAST_FIX_LOCATION_SETTINGS_CODE:

                        Status status = (Status) bundle.getParcelable(Constants.KEY_LOCATION_SETTINGS_PARCELABLE);
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(TrackerActivity.this,
                                    Constants.CODE_LOCATION_SETTINGS_RESOLUTION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }

                        break;

                    case Constants.BROADCAST_GOOGLE_FIT_READ_PERMISSION:

                        ConnectionResult connectionResult =
                                bundle.getParcelable(Constants.KEY_GOOGLE_FIT_RESOLUTION_PARCELABLE);
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            connectionResult.startResolutionForResult(TrackerActivity.this,
                                    GoogleFitStepCounter.REQUEST_OAUTH);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }

                        break;

                    case Constants.BROADCAST_WORKOUT_RESULT_CODE:
                        Logger.i(TAG, "onReceive of locationServiceReceiver,  BROADCAST_WORKOUT_RESULT_CODE");
                        WorkoutData result = bundle.getParcelable(Constants.KEY_WORKOUT_RESULT);
                        //TODO: Display Result on UI
                        runFragment.onWorkoutResult(result);
                        break;

                    case Constants.BROADCAST_WORKOUT_UPDATE_CODE:
                        float currentSpeed = bundle.getFloat(Constants.KEY_WORKOUT_UPDATE_SPEED);
                        float currentTotalDistanceCovered = bundle.getFloat(Constants.KEY_WORKOUT_UPDATE_TOTAL_DISTANCE);
                        //TODO: Display Result on UI
                        runFragment.showUpdate(currentSpeed, currentTotalDistanceCovered);
                        break;

                    case Constants.BROADCAST_STEPS_UPDATE_CODE:
                        int totalSteps = bundle.getInt(Constants.KEY_WORKOUT_UPDATE_STEPS);
                        //TODO: Display Result on UI
                        runFragment.showSteps(totalSteps);
                        break;

                    case Constants.BROADCAST_UNBIND_SERVICE_CODE:
                        Logger.i(TAG, "onReceive of locationServiceReceiver, BROADCAST_UNBIND_SERVICE_CODE");
                        if (isBoundToLocationService()){
                            unbindLocationService();
                            locationService = null;
                        }
                        break;
                    case Constants.BROADCAST_PAUSE_WORKOUT_CODE:
                        Logger.i(TAG, "onReceive of locationServiceReceiver,  BROADCAST_PAUSE_WORKOUT_CODE");
                        synchronized (this){
                            if (runFragment != null && runFragment.isRunActive()){
                                int problem = bundle.getInt(Constants.KEY_PAUSE_WORKOUT_PROBLEM);
                                String errorMessage = "";
                                switch (problem){
                                    case Constants.PROBELM_TOO_FAST:
                                        errorMessage = getString(R.string.rfac_usain_bolt_message);
                                        break;
                                    case Constants.PROBELM_TOO_SLOW:
                                        errorMessage = getString(R.string.rfac_too_slow_message);
                                        break;
                                    case Constants.PROBELM_NOT_MOVING:
                                        errorMessage = getString(R.string.rfac_lazy_ass_message);
                                        break;
                                }
                                if (!TextUtils.isEmpty(errorMessage)){
                                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                                    runFragment.showErrorMessage(errorMessage);
                                }
                                runFragment.pauseRun(false);
                            }
                        }
                        break;
                }
            }
        }
    };

}
