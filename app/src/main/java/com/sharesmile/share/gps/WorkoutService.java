package com.sharesmile.share.gps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.gson.Gson;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.activities.LoginActivity;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Created by ankitmaheshwari1 on 20/02/16.
 */
public class WorkoutService extends Service implements
        IWorkoutService, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RunTracker.UpdateListner, StepCounter.Listener, GoogleLocationTracker.Listener {

    private static final String TAG = "WorkoutService";

    private static boolean currentlyTracking = false;
    private static boolean currentlyProcessingSteps = false;
    private GoogleApiClient googleApiClient;
    private VigilanceTimer vigilanceTimer;
    private Location prevLocationFix;

    private StepCounter stepCounter;

    private ScheduledExecutorService backgroundExecutorService;

    private Tracker tracker;
    private float mDistance;
    private CauseData mCauseData;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate");
        mCauseData = new Gson().fromJson(SharedPrefsManager.getInstance().getString(Constants.PREF_CAUSE_DATA),
                CauseData.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand");
        if (backgroundExecutorService == null) {
            backgroundExecutorService = Executors.newScheduledThreadPool(5);
        }
        startWorkout();
        return START_STICKY;
    }

    private void startTracking() {
        Logger.d(TAG, "startTracking");
        if (tracker == null) {
            tracker = new RunTracker(backgroundExecutorService, this);
        }
        synchronized (this){
            prevLocationFix = null;
        }
        vigilanceTimer = new VigilanceTimer(this, backgroundExecutorService);
        mDistance = tracker.getTotalDistanceCovered();
        makeForeground();
    }


    private void stopTracking() {
        Logger.d(TAG, "stopTracking");
        if (tracker != null) {
            WorkoutData result = tracker.endRun();
            tracker = null;
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                    Constants.BROADCAST_WORKOUT_RESULT_CODE);
            bundle.putParcelable(Constants.KEY_WORKOUT_RESULT, result);
            Intent intent = new Intent(Constants.WORKOUT_SERVICE_BROADCAST_ACTION);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void startWorkout() {
        //If tracking is already in progress then no need to setup again
        if (!currentlyTracking) {
            currentlyTracking = true;
            Logger.d(TAG, "startWorkout");

            GoogleLocationTracker.getInstance().registerForWorkout(this);
            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(ActivityRecognition.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                    googleApiClient.connect();
                }
            } else {
                Logger.e(TAG, "unable to connect to google play services.");
                Toast.makeText(getApplicationContext(), "Unable to connect to google play services", Toast.LENGTH_SHORT);
            }
            if (!currentlyProcessingSteps) {
                if (isKitkatWithStepSensor(getApplicationContext())) {
                    Logger.d(TAG, "Step Detector present! Will register");
                    stepCounter = new AndroidStepCounter(this, this);
                } else {
                    Logger.d(TAG, "Will initiate  GoogleFitStepCounter");
                    stepCounter = new GoogleFitStepCounter(this, this);
                }
            }
            AnalyticsEvent.create(Event.ON_WORKOUT_START)
                    .addBundle(mCauseData.getCauseBundle())
                    .buildAndDispatch();
        }

    }

    @Override
    public synchronized void stopWorkout() {
        Logger.d(TAG, "stopWorkout");
        if (currentlyTracking) {
            stopTracking();
            GoogleLocationTracker.getInstance().unregisterWorkout(this);
            Intent intent = new Intent(this, ActivityRecognizedService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (googleApiClient != null && googleApiClient.isConnected()) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates( googleApiClient, pendingIntent );
                googleApiClient.disconnect();
            }
            googleApiClient = null;
            currentlyTracking = false;
            if (stepCounter != null) {
                stepCounter.stopCounting();
            }
            currentlyProcessingSteps = false;
            unBindFromActivityAndStop();
            NotificationManagerCompat.from(this).cancel(0);

            AnalyticsEvent.create(Event.ON_WORKOUT_END)
                    .addBundle(mCauseData.getCauseBundle())
                    .addBundle(getWorkoutBundle())
                    .buildAndDispatch();
        }
    }

    public Properties getWorkoutBundle(){
        if (tracker != null){
            Properties p = new Properties();
            p.put("distance", Utils.formatToKms(getTotalDistanceCoveredInMeters()));
            p.put("time_elapsed", getWorkoutElapsedTimeInSecs());
            p.put("avg_speed", tracker.getAvgSpeed() * (3.6f));
            p.put("num_steps", getTotalStepsInWorkout());
            return p;
        }
        return  null;
    }

    private void unBindFromActivityAndStop() {
        Logger.d(TAG, "unBindFromActivityAndStop");
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_UNBIND_SERVICE_CODE);
        sendBroadcast(bundle);
        stopSelf();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.d(TAG, "onUnbind");
        super.onUnbind(intent);
        if (!RunTracker.isWorkoutActive()) {
            // Stop service only when workout session is not going on
            Logger.d(TAG, "onUnbind: Will stopWorkout service");
            stopSelf();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        super.onDestroy();
        backgroundExecutorService.shutdownNow();
        backgroundExecutorService = null;
        stopForeground(true);
    }

    final IBinder mBinder = new MyBinder();

    @Override
    public void notAvailable(int reasonCode) {
        Logger.d(TAG, "notAvailable, reasonCode = " + reasonCode);
        currentlyProcessingSteps = false;
    }

    @Override
    public void isReady() {
        Logger.d(TAG, "isReady");
        currentlyProcessingSteps = true;
    }

    @Override
    public void onStepCount(int deltaSteps) {
        if (tracker != null && tracker.isActive()) {
            tracker.feedSteps(deltaSteps);
        }
    }

    public static boolean isCurrentlyTracking() {
        return currentlyTracking;
    }

    public static boolean isCurrentlyProcessingSteps() {
        return currentlyProcessingSteps;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MyBinder extends Binder {

        public WorkoutService getService() {
            // Return this instance of LocalService so clients can call public methods
            return WorkoutService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onLocationTrackerReady() {
        Logger.i(TAG, "onLocationTrackerReady");
        startTracking();
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        if (location == null){
            return;
        }
        if (prevLocationFix == null){
            prevLocationFix = location;
            // Will not send to tracker as it is the very first location fix received
            // Will start sending subsequent location fixes, only after they are approved by spike filter
            return;
        }
        // Spike filter check
        if (!checkForSpike(prevLocationFix, location)){
            if (tracker != null) {
                tracker.feedLocation(location);
            }
        }
        prevLocationFix = location;
    }

    /**
     * Simple spike filter which detects whether given couple of subsequent location fixes is having spike
     * @param loc1
     * @param loc2
     * @return
     */
    public boolean checkForSpike(Location loc1, Location loc2){

        long deltaTime = Math.abs(loc2.getTime() - loc1.getTime()) / 1000;

        if (deltaTime > Config.SPIKE_FILTER_ELIGIBLE_TIME_INTERVAL){
            // If time interval between two location fixes is sufficiently large then those fixes are not considered as spikes
            return false;
        }

        float deltaDistance = loc1.distanceTo(loc2);
        float deltaSpeed = deltaDistance / deltaTime;

        // If speed is greater than threshold, then it is considered as GPS spike
        if (deltaSpeed > Config.SPIKE_FILTER_SPEED_THRESHOLD){
            Logger.e(TAG, "Detected GPS spike, between locations loc1:\n" + loc1.toString()
                        + "\n, loc2:\n" + loc2.toString()
                        + "\n Spike distance = " + deltaDistance + " meters in " + deltaTime + " seconds");
            AnalyticsEvent.create(Event.DETECTED_GPS_SPIKE)
                    .addBundle(getWorkoutBundle())
                    .put("spikey_distance", deltaDistance)
                    .put("time_interval", deltaTime)
                    .buildAndDispatch();
            return true;
        }else {
            return false;
        }

    }

    @Override
    public void onPermissionDenied() {
        Logger.i(TAG, "onPermissionDenied");
        stopWorkout();
        stopSelf();
    }

    @Override
    public void onConnectionFailure() {
        Logger.e(TAG, "GoogleLocationTracker onConnectionFailure, will stop workoutService");
        stopWorkout();
        stopSelf();
    }

    @Override
    public void onGpsEnabled() {
        if (tracker != null && tracker.isPaused()){
            Logger.i(TAG, "onGpsEnabled: Gps enabled while workout was ongoing, user can resume the workout now");
            // User can resume the workout now, if it was paused because of disabled GPS.
        }
    }

    @Override
    public void onGpsDisabled() {
        if (tracker != null && tracker.isRunning()){
            Logger.i(TAG, "onGpsDisabled: Gps disabled while workout was ongoing, will pause the workout");
            // Pause workout
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.KEY_PAUSE_WORKOUT_PROBLEM, Constants.PROBLEM_GPS_DISABLED);
            bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                    Constants.BROADCAST_PAUSE_WORKOUT_CODE);
            sendBroadcast(bundle);
            pause("gps_disabled");
            // Popup to enable location again
            GoogleLocationTracker.getInstance().startLocationTracking(true);
        }
    }

    @Override
    public void updateWorkoutRecord(float totalDistance, float avgSpeed, float deltaDistance,
                                    int deltaTime, float deltaSpeed) {
        Logger.d(TAG, "updateWorkoutRecord: totalDistance = " + totalDistance
                + " avgSpeed = " + avgSpeed + ", deltaSpeed = " + deltaSpeed + ", deltaTime = " + deltaTime);
        // Send an update broadcast to Activity
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_WORKOUT_UPDATE_CODE);
        bundle.putFloat(Constants.KEY_WORKOUT_UPDATE_SPEED, deltaSpeed);
        bundle.putFloat(Constants.KEY_WORKOUT_UPDATE_TOTAL_DISTANCE, totalDistance);
        mDistance = totalDistance;
        bundle.putInt(Constants.KEY_WORKOUT_UPDATE_ELAPSED_TIME_IN_SECS, tracker.getElapsedTimeInSecs());
        sendBroadcast(bundle);
        updateNotification();
        AnalyticsEvent.create(Event.ON_WORKOUT_UPDATE)
                .addBundle(mCauseData.getCauseBundle())
                .addBundle(getWorkoutBundle())
                .put("delta_distance", deltaDistance)
                .put("delta_time", deltaTime)
                .put("delta_speed", deltaSpeed)
                .buildAndDispatch();
    }

    private void sendBroadcast(Bundle bundle) {
        Intent intent = new Intent(Constants.WORKOUT_SERVICE_BROADCAST_ACTION);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void updateStepsRecord(long timeStampMillis) {
        Logger.d(TAG, "Time to show steps count, totalSteps = " + tracker.getTotalSteps());
        // Send an update broadcast to Activity
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_STEPS_UPDATE_CODE);
        bundle.putInt(Constants.KEY_WORKOUT_UPDATE_STEPS, tracker.getTotalSteps());
        bundle.putInt(Constants.KEY_WORKOUT_UPDATE_ELAPSED_TIME_IN_SECS, tracker.getElapsedTimeInSecs());
        sendBroadcast(bundle);
    }

    /**
     * Returns true if this device is supported. It needs to be running Android KitKat (4.4) or
     * higher and has a step counter and step detector sensor.
     * This check is useful when an app provides an alternative implementation or different
     * functionality if the step sensors are not available or this code runs on a platform version
     * below Android KitKat. If this functionality is required, then the minSDK parameter should
     * be specified appropriately in the AndroidManifest.
     *
     * @return True iff the device can run this sample
     */
    public static boolean isKitkatWithStepSensor(Context appContext) {
        // Require at least Android KitKat
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        PackageManager packageManager = appContext.getPackageManager();
        boolean hasStepDetector = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
        Logger.i(TAG, "isKitkatWithStepSensor: currentApiVersion = " + currentApiVersion +
                ", hasStepDetector = " + hasStepDetector);
        return currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT
                && hasStepDetector;
    }

    @Override
    public void pause(String reason) {
        Logger.i(TAG, "pause");
        if (vigilanceTimer != null) {
            vigilanceTimer.pauseTimer();
        }
        if (tracker != null && tracker.isRunning()) {
            tracker.pauseRun();
            //Test: Put stopping locationServices code over here
            Logger.d(TAG, "PauseWorkout");
            if (currentlyTracking) {
                Intent intent = new Intent(this, ActivityRecognizedService.class);
                PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );

                if (googleApiClient != null && googleApiClient.isConnected()) {
                    ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, pendingIntent);
                }
                NotificationManagerCompat.from(this).cancel(0);
            }
            AnalyticsEvent.create(Event.ON_WORKOUT_PAUSE)
                    .addBundle(mCauseData.getCauseBundle())
                    .addBundle(getWorkoutBundle())
                    .put("reason", reason)
                    .buildAndDispatch();
        }
    }

    @Override
    public void resume() {
        Logger.i(TAG, "resume");
        if (tracker != null && tracker.getState() != Tracker.State.RUNNING) {
            synchronized (this){
                prevLocationFix = null;
            }
            tracker.resumeRun();
            Logger.d(TAG, "ResumeWorkout");
            if (currentlyTracking) {
                Intent intent = new Intent(this, ActivityRecognizedService.class);
                PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );

                if (googleApiClient != null && googleApiClient.isConnected()) {
                    ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, 3000, pendingIntent );
                }
                NotificationManagerCompat.from(this).cancel(0);
            }
            AnalyticsEvent.create(Event.ON_WORKOUT_RESUME)
                    .addBundle(mCauseData.getCauseBundle())
                    .addBundle(getWorkoutBundle())
                    .buildAndDispatch();
        }
    }

    public void sendStopWorkoutBroadcast(int problem) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_STOP_WORKOUT_PROBLEM, problem);
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_STOP_WORKOUT_CODE);
        sendBroadcast(bundle);
        stopWorkout();
    }

    @Override
    public void workoutVigilanceSessiondefaulted(int problem) {
        Logger.d(TAG, "workoutVigilanceSessiondefaulted");
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_PAUSE_WORKOUT_PROBLEM, problem);
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_PAUSE_WORKOUT_CODE);
        sendBroadcast(bundle);
        if (tracker != null && tracker.isActive()) {
            tracker.discardApprovalQueue();
        }
        pause("usain_bolt");
    }

    @Override
    public void workoutVigilanceSessionApproved(long sessionStartTime, long sessionEndTime) {
        Logger.d(TAG, "workoutVigilanceSessionApproved");
        if (tracker != null && tracker.isActive()) {
            tracker.approveWorkoutData();
        }
    }

    @Override
    public boolean isCountingSteps() {
        return currentlyProcessingSteps;
    }

    @Override
    public float getTotalDistanceCoveredInMeters() {
        if (tracker != null && tracker.isActive()){
            Logger.d(TAG, "getTotalDistanceCoveredInMeters from Tracker");
            return tracker.getTotalDistanceCovered();
        }
        return 0;
    }

    @Override
    public long getWorkoutElapsedTimeInSecs() {
        if (tracker != null && tracker.isActive()){
            long elapsedTime = tracker.getElapsedTimeInSecs();
            Logger.d(TAG, "getElapsedTimeInSecs from Tracker = " + elapsedTime);
            return elapsedTime;
        }
        return 0;
    }

    @Override
    public int getTotalStepsInWorkout() {
        if (tracker != null && tracker.isActive()){
            return tracker.getTotalSteps();
        }
        return 0;
    }

    @Override
    public float getCurrentSpeed() {
        if (tracker != null && tracker.isActive()){
            return tracker.getCurrentSpeed();
        }
        return 0;
    }

    @Override
    public float getAvgSpeed() {
        if (tracker != null && tracker.isActive()){
            return tracker.getAvgSpeed();
        }
        return 0;
    }

    @Override
    public Tracker getTracker() {
        return tracker;
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or startWorkout periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Logger.d(TAG, "onConnected");
        Intent intent = new Intent(this, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, 3000, pendingIntent );
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GoogleFitStepCounter.REQUEST_OAUTH:
                stepCounter.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.e(TAG, "GoogleApiClient connection has been suspend");
    }

    private void makeForeground() {
        Notification notification = getNotificationBuilder().build();
        startForeground(1000, notification);
    }

    private void updateNotification() {

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1000, getNotificationBuilder().build());
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        String distDecimal = String.format("%1$.1f", (mDistance / 1000));
        int rupees = 0;

        try{
            float fDistance = Float.parseFloat(distDecimal);
            rupees = (int) Math.ceil(mCauseData.getConversionRate() * fDistance);
        }
        catch ( NumberFormatException e){

        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Running")
                        .setContentText("Raised  : " + getString(R.string.rs_symbol) + rupees)
                        .setSmallIcon(getNotificationIcon()).setColor(getResources().getColor(R.color.denim_blue))
                        .setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(),
                                R.mipmap.ic_launcher))
                        .setTicker(getBaseContext().getResources().getString(R.string.app_name))
                        .setOngoing(true)
                        .setVisibility(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Intent resultIntent = new Intent(this, LoginActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder;
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_stat_onesignal_default : R.mipmap.ic_launcher;
    }

}
