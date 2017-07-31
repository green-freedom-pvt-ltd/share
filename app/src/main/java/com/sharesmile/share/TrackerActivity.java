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
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.gson.Gson;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.BaseActivity;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.PermissionCallback;
import com.sharesmile.share.gps.GoogleFitStepCounter;
import com.sharesmile.share.gps.WorkoutService;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.RealRunFragment;
import com.sharesmile.share.rfac.RunFragment;
import com.sharesmile.share.rfac.TestRunFragment;
import com.sharesmile.share.rfac.fragments.StartRunFragment;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

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
    private boolean openHomeOnExit;

    private static final int HOME = 0;
    private static final int PROFILE = 1;
    private static final int FEEDBACK = 2;
    private static final int LOGOUT = 3;

    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private CauseData mCauseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        runInTestMode = SharedPrefsManager.getInstance().getBoolean(Constants.KEY_WORKOUT_TEST_MODE_ON);
        mCauseData = (CauseData) getIntent().getSerializableExtra(BUNDLE_CAUSE_DATA);
        if (mCauseData != null) {
            SharedPrefsManager.getInstance().setString(Constants.PREF_CAUSE_DATA, new Gson().toJson(mCauseData));
        } else {
            mCauseData = new Gson().fromJson(SharedPrefsManager.getInstance().getString(Constants.PREF_CAUSE_DATA), CauseData.class);
        }

        if (isTaskRoot()){
            openHomeOnExit = true;
        }

        loadInitialFragment();

        LocalBroadcastManager.getInstance(this).registerReceiver(workoutServiceReceiver,
                new IntentFilter(Constants.WORKOUT_SERVICE_BROADCAST_ACTION));

        if (isWorkoutActive()) {
            // If workout sesion going on then bind to service
            invokeWorkoutService();
        }

//        Window window = this.getWindow();
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//        {
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }

    }

    public boolean isWorkoutActive() {
        return WorkoutSingleton.getInstance().isWorkoutActive();
    }

    @Override
    public void loadInitialFragment() {
        if (isWorkoutActive()) {
            runFragment = createRunFragment();
            addFragment(runFragment, false);
        } else {
            addFragment(StartRunFragment.newInstance(mCauseData), false);
        }
    }

    private RunFragment createRunFragment() {
        if (runInTestMode) {
            return TestRunFragment.newInstance();
        } else {
            return RealRunFragment.newInstance(mCauseData);
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

        switch (operationId) {
            case END_RUN_START_COUNTDOWN:
                runFragment = createRunFragment();
                replaceFragment(runFragment, false);
                break;
            default:
                super.performOperation(operationId, input);
        }
    }

    @Override
    public void exit() {
        if (openHomeOnExit){
            performOperation(START_MAIN_ACTIVITY, null);
        }
        finish();
    }

    @Override
    public void requestPermission(int requestCode, PermissionCallback permissionsCallback) {

    }

    @Override
    public void unregisterForPermissionRequest(int requestCode) {

    }

    @Override
    public void updateToolBar(String title, boolean showAsUpEnable) {

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

    public File writeLogsToFile(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
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

            File outputFile = new File(getLogsFileDir(), fileName);
            String filePath = outputFile.getAbsolutePath();
            try {
                Logger.d(TAG, "filePath " + filePath);
                @SuppressWarnings("unused")
                Process process = Runtime.getRuntime().exec("logcat -v threadtime -f " + filePath);
            } catch (IOException ioe) {
                Logger.e(TAG, "IOException while writing logs to file", ioe);
            }
            return outputFile;

        } else {
            //Need to get permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.CODE_REQUEST_WRITE_PERMISSION);
        }

        return null;

    }

    public static File getLogsFileDir() {
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/data/share");
        dir.mkdirs();
        return dir;
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, "onDestroy");
        super.onDestroy();
        if (isBoundToLocationService()) {
            unbindLocationService();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(workoutServiceReceiver);
    }

    public void continuedRun(){
        invokeWorkoutService();
    }

    public void beginRun() {
        Logger.d(TAG, "beginRun");
        invokeWorkoutService();
    }

    public void endRun() {
        Logger.d(TAG, "endRun");
        if (isBoundToLocationService()) {
            Logger.d(TAG, "Bound to WorkoutService, will stopWorkout first");
            locationService.stopWorkout();
        }
    }

    public void pauseWorkout() {
        if (isBoundToLocationService()) {
            locationService.pause("user_clicked");
        }
    }

    public boolean resumeWorkout() {
        if (isBoundToLocationService()) {
            return locationService.resume();
        }
        return false;
    }

    public long getElapsedTimeInSecs(){
        if (isBoundToLocationService()) {
            long elapsedTime = locationService.getWorkoutElapsedTimeInSecs();
            Logger.d(TAG, "getElapsedTimeInSecs = " + elapsedTime);
            return elapsedTime;
        }
        return 0;
    }

    public Properties getWorkoutBundle(){
        return WorkoutSingleton.getInstance().getWorkoutBundle();
    }

    public float getTotalDistanceInMeters(){
        if (isBoundToLocationService()){
            return locationService.getTotalDistanceCoveredInMeters();
        }
        return 0;
    }

    public float getCurrentSpeed(){
        if (isBoundToLocationService()){
            return locationService.getCurrentSpeed();
        }
        return 0;
    }

    public float getAvgSpeed(){
        if (isBoundToLocationService()){
            return locationService.getAvgSpeed();
        }
        return 0;
    }

    public int getTotalSteps(){
        if (isBoundToLocationService()){
            return locationService.getTotalStepsInWorkout();
        }
        return 0;
    }

    private void unbindLocationService() {
        Logger.d(TAG, "unbindLocationService");
        unbindService(locationServiceConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CODE_REQUEST_WRITE_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Can re-capture logs
            } else {
                // Permission was denied or request was cancelled
                Logger.i(TAG, "WRITE Permission denied, couldn't update the UI");
                Toast.makeText(this, "Please give permission to WRITE", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void invokeWorkoutService() {
        Log.d(TAG, "invokeWorkoutService");
        Intent intent = new Intent(this, WorkoutService.class);
        startService(intent);
        bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE
                | Context.BIND_IMPORTANT);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to WorkoutService, cast the IBinder and get WorkoutService instance
            Logger.d(TAG, "onServiceConnected");
            WorkoutService.MyBinder binder = (WorkoutService.MyBinder) service;
            locationService = binder.getService();
            if (runFragment != null){
                runFragment.refreshWorkoutData();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            locationService = null;
        }
    };

    public boolean isBoundToLocationService() {
        return (locationService != null);
    }


    private BroadcastReceiver workoutServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int broadcastCategory = bundle
                        .getInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY);
                switch (broadcastCategory) {
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
                        Logger.i(TAG, "onReceive of workoutServiceReceiver,  BROADCAST_WORKOUT_RESULT_CODE");
                        WorkoutData result = bundle.getParcelable(Constants.KEY_WORKOUT_RESULT);
                        runFragment.onWorkoutResult(result);
                        AnalyticsEvent.create(Event.ON_WORKOUT_COMPLETE)
                                .addBundle(result.getWorkoutBundle())
                                .put("cause_id", mCauseData.getId())
                                .put("cause_title", mCauseData.getTitle())
                                .put("num_spikes", result.getNumGpsSpikes())
                                .put("bolt_count", result.getUsainBoltCount())
                                .put("num_update_events", result.getNumUpdateEvents())
                                .buildAndDispatch();
                        break;

                    case Constants.BROADCAST_WORKOUT_UPDATE_CODE:
                        float currentSpeed = bundle.getFloat(Constants.KEY_WORKOUT_UPDATE_SPEED);
                        float currentDistanceCovered = bundle.getFloat(Constants.KEY_WORKOUT_UPDATE_TOTAL_DISTANCE);
                        int elapsedTimeInSecs = bundle.getInt(Constants.KEY_WORKOUT_UPDATE_ELAPSED_TIME_IN_SECS);
                        runFragment.showUpdate(currentSpeed, currentDistanceCovered, elapsedTimeInSecs);
                        break;

                    case Constants.BROADCAST_STEPS_UPDATE_CODE:
                        int currentTotalSteps = bundle.getInt(Constants.KEY_WORKOUT_UPDATE_STEPS);
                        int elapsedTime = bundle.getInt(Constants.KEY_WORKOUT_UPDATE_ELAPSED_TIME_IN_SECS);
                        runFragment.showSteps(currentTotalSteps, elapsedTime);
                        break;

                    case Constants.BROADCAST_UNBIND_SERVICE_CODE:
                        Logger.i(TAG, "onReceive of workoutServiceReceiver, BROADCAST_UNBIND_SERVICE_CODE");
                        if (isBoundToLocationService()) {
                            unbindLocationService();
                            locationService = null;
                        }
                        break;
                    case Constants.BROADCAST_RESUME_WORKOUT_CODE:
                        Logger.i(TAG, "onReceive of workoutServiceReceiver, BROADCAST_RESUME_WORKOUT_CODE");
                        if (runFragment != null && runFragment.isRunActive() && !runFragment.isRunning()){
                            runFragment.resumeRun();
                        }
                        break;
                    case Constants.BROADCAST_PAUSE_WORKOUT_CODE:
                        Logger.i(TAG, "onReceive of workoutServiceReceiver,  BROADCAST_PAUSE_WORKOUT_CODE");
                        synchronized (this) {
                            if (runFragment != null && runFragment.isRunActive()) {
                                int problem = bundle.getInt(Constants.KEY_PAUSE_WORKOUT_PROBLEM);
                                String errorMessage = "";
                                switch (problem) {
                                    case Constants.PROBELM_TOO_FAST:
                                        String reducedDistance = bundle.getString(Constants.KEY_USAIN_BOLT_DISTANCE_REDUCED);
                                        if (TextUtils.isEmpty(reducedDistance)){
                                            errorMessage = getString(R.string.rfac_usain_bolt_message_without_distance);
                                        }else {
                                            errorMessage = getString(R.string.rfac_usain_bolt_message_with_distance, reducedDistance);
                                        }
                                        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                                        v.vibrate(3000);

                                        break;
                                    case Constants.PROBELM_TOO_SLOW:
                                        errorMessage = getString(R.string.rfac_too_slow_message);
                                        break;
                                    case Constants.PROBELM_NOT_MOVING:
                                        errorMessage = getString(R.string.rfac_lazy_ass_message);
                                        break;
                                    case Constants.PROBLEM_GPS_DISABLED:
                                        errorMessage = getString(R.string.rfac_gps_disabled_message);
                                        break;
                                }
                                if (!TextUtils.isEmpty(errorMessage)) {
                                    if (problem != Constants.PROBLEM_GPS_DISABLED){
                                        runFragment.showErrorMessage(errorMessage);
                                    }
                                }
                                runFragment.pauseRun(false);
                            }
                        }
                        break;
                }
            }
        }
    };


    @Override
    public void onBackPressed() {
        if (WorkoutSingleton.getInstance().isWorkoutActive()) {
            runFragment.showStopDialog();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
