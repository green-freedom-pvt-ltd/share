package com.sharesmile.share.tracking.workout.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.tracking.workout.tracker.RunTracker;
import com.sharesmile.share.tracking.workout.tracker.Tracker;
import com.sharesmile.share.tracking.workout.data.WorkoutDataStore;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.tracking.event.GpsStateChangeEvent;
import com.sharesmile.share.tracking.event.MockLocationDetected;
import com.sharesmile.share.tracking.event.PauseWorkoutEvent;
import com.sharesmile.share.tracking.event.ResumeWorkoutEvent;
import com.sharesmile.share.tracking.event.UpdateUiOnAutoFlagWorkout;
import com.sharesmile.share.tracking.event.UpdateUiOnMockLocation;
import com.sharesmile.share.tracking.event.UpdateUiOnWorkoutPauseEvent;
import com.sharesmile.share.tracking.event.UpdateUiOnWorkoutResumeEvent;
import com.sharesmile.share.tracking.event.UsainBoltForceExit;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.config.ClientConfig;
import com.sharesmile.share.core.config.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.notifications.NotificationActionReceiver;
import com.sharesmile.share.core.TTS;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.core.sync.SyncService;
import com.sharesmile.share.tracking.stepcount.GoogleFitStepCounter;
import com.sharesmile.share.tracking.google.tracker.GoogleFitTracker;
import com.sharesmile.share.tracking.activityrecognition.ActivityDetector;
import com.sharesmile.share.tracking.location.GoogleLocationTracker;
import com.sharesmile.share.tracking.models.WorkoutBatch;
import com.sharesmile.share.tracking.models.WorkoutData;
import com.sharesmile.share.core.cause.model.CauseData;
import com.sharesmile.share.tracking.workout.data.model.FraudData;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.tracking.stepcount.AndroidStepCounter;
import com.sharesmile.share.tracking.stepcount.StepCounter;
import com.sharesmile.share.tracking.vigilance.VigilanceTimer;
import com.sharesmile.share.utils.CircularQueue;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.timekeeping.ServerTimeKeeper;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.sharesmile.share.core.application.MainApplication.getContext;
import static com.sharesmile.share.core.config.Config.WORKOUT_BEGINNING_LOCATION_CIRCULAR_QUEUE_MAX_SIZE;
import static com.sharesmile.share.core.Constants.PAUSE_REASON_GPS_DISABLED;
import static com.sharesmile.share.core.Constants.PAUSE_REASON_USAIN_BOLT;
import static com.sharesmile.share.core.Constants.PAUSE_REASON_USER_CLICKED_NOTIFICATION;
import static com.sharesmile.share.core.Constants.PREF_DISABLE_VOICE_UPDATES;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.WORKOUT_NOTIFICATION_BAD_GPS_ID;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.WORKOUT_NOTIFICATION_DISABLE_MOCK_ID;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.WORKOUT_NOTIFICATION_GPS_INACTIVE_ID;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.WORKOUT_NOTIFICATION_STILL_ID;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.WORKOUT_NOTIFICATION_USAIN_BOLT_FORCE_EXIT_ID;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.WORKOUT_NOTIFICATION_USAIN_BOLT_ID;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.WORKOUT_TRACK_NOTIFICATION_ID;
import static com.sharesmile.share.tracking.workout.WorkoutSingleton.GPS_STATE_BAD;
import static com.sharesmile.share.tracking.workout.WorkoutSingleton.GPS_STATE_INACTIVE;
import static com.sharesmile.share.tracking.workout.WorkoutSingleton.GPS_STATE_OK;
import static com.sharesmile.share.tracking.ui.RunFragment.NOTIFICATION_TIMER_TICK;

//import com.squareup.leakcanary.RefWatcher;


/**
 * Created by ankitmaheshwari1 on 20/02/16.
 */
public class WorkoutService extends Service implements
        IWorkoutService, RunTracker.UpdateListner, StepCounter.Listener,
        GoogleLocationTracker.Listener, GoogleFitTracker.GoogleFitListener {

    private static final String TAG = "WorkoutService";

    private static boolean currentlyTracking = false;
    private boolean currentlyProcessingSteps = false;

    private VigilanceTimer vigilanceTimer;
    private Location acceptedLocationFix;
    private CircularQueue<Location> beginningLocationsRotatingQueue;

    private StepCounter stepCounter;
    private GoogleFitTracker googleFitTracker;
    private ScheduledExecutorService backgroundExecutorService;

    private Tracker tracker;
    private CauseData mCauseData;
    private Handler handler;

    TTS textToSpeech;

    private float distanceInKmsOnLastUpdateEvent = 0f;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate");
        mCauseData = Utils.createObjectFromJSONString(
                SharedPrefsManager.getInstance().getString(Constants.PREF_CAUSE_DATA),
                CauseData.class);
        handler = new Handler();
        EventBus.getDefault().register(this);
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
        if (backgroundExecutorService == null){
            return;
        }
        if (tracker == null) {
            tracker = new RunTracker(backgroundExecutorService, this);
            googleFitTracker = new GoogleFitTracker(this, this);
            googleFitTracker.start();
            ActivityDetector.getInstance().startActivityDetection();
        }
        synchronized (this){
            acceptedLocationFix = null;
            beginningLocationsRotatingQueue = null;
        }
        if (vigilanceTimer == null){
            vigilanceTimer = new VigilanceTimer(this, backgroundExecutorService);
        }
        makeForegroundAndSticky();
        AnalyticsEvent.create(Event.ON_WORKOUT_START)
                .buildAndDispatch();
    }

    private void sendWorkoutResultBroadcast(WorkoutData result){
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_WORKOUT_RESULT_CODE);
        bundle.putParcelable(Constants.KEY_WORKOUT_RESULT, result);
        Intent intent = new Intent(Constants.WORKOUT_SERVICE_BROADCAST_ACTION);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void updateGoogleFitDetailsInSyncedWorkout(WorkoutData data){
        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        Workout storedWorkout = mWorkoutDao.queryBuilder()
                .where(WorkoutDao.Properties.WorkoutId.eq(data.getWorkoutId()))
                .unique();

        storedWorkout.setEstimatedSteps(data.getEstimatedSteps());
        storedWorkout.setEstimatedDistance((double)data.getEstimatedDistance());
        storedWorkout.setEstimatedCalories((double)data.getEstimatedCalories());

        mWorkoutDao.insertOrReplace(storedWorkout);
    }

    private void persistWorkoutInDb(WorkoutData data){

        WorkoutDao workoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        Workout workout = new Workout();

        workout.setIsValidRun(!data.isAutoFlagged());
        workout.setAvgSpeed(data.getAvgSpeed());
        workout.setDistance(data.getDistance() / 1000); // in Kms
        workout.setElapsedTime(Utils.secondsToHHMMSS((int) data.getElapsedTime()));
        int rupees = Utils.convertDistanceToRupees(mCauseData.getConversionRate(), data.getDistance());
        workout.setRunAmount((float) rupees);
        workout.setRecordedTime(data.getRecordedTime());
        workout.setSteps(data.getTotalSteps());
        workout.setCauseBrief(mCauseData.getTitle());
        workout.setCauseId(Integer.parseInt(String.valueOf(mCauseData.getId())));
        workout.setDate(new Date(data.getBeginTimeStamp()));
        workout.setIs_sync(false);
        workout.setWorkoutId(data.getWorkoutId());
        if (data.getStartPoint() != null){
            workout.setStartPointLatitude(data.getStartPoint().latitude);
            workout.setStartPointLongitude(data.getStartPoint().longitude);
        }
        if (data.getLatestPoint() != null){
            workout.setEndPointLatitude(data.getLatestPoint().latitude);
            workout.setEndPointLongitude(data.getLatestPoint().longitude);
        }
        workout.setBeginTimeStamp(data.getBeginTimeStamp());
        workout.setEndTimeStamp(DateUtil.getServerTimeInMillis());
        workout.setCalories(data.getCalories().getCalories());
        workout.setTeamId(LeaderBoardDataStore.getInstance().getMyTeamId());
        workout.setNumSpikes(data.getNumGpsSpikes());
        workout.setNumUpdates(data.getNumUpdateEvents());
        workout.setAppVersion(Utils.getAppVersion());
        workout.setOsVersion(Build.VERSION.SDK_INT);
        workout.setDeviceId(Utils.getUniqueId(getContext()));
        workout.setDeviceName(Utils.getDeviceName());
        workout.setShouldSyncLocationData(true);
        workout.setUsainBoltCount(data.getUsainBoltCount());
        workout.setGoogleFitStepCount(data.getGoogleFitSteps());
        workout.setGoogleFitDistance((double)data.getGoogleFitDistance());

        workoutDao.insertOrReplace(workout);

        Utils.updateTrackRecordFromDb();
        String key = Utils.getWorkoutLocationDataPendingQueuePrefKey(data.getWorkoutId());
        SharedPrefsManager.getInstance().setObject(key, data);
    }

    @Override
    public void startWorkout() {
        //If tracking is already in progress then no need to setup again
        if (!currentlyTracking) {
            currentlyTracking = true;
            Logger.d(TAG, "startWorkout");
            WorkoutServiceRetainerAlarm.setRepeatingAlarm(this);
            GoogleLocationTracker.getInstance().registerForWorkout(this);
            handler.removeCallbacks(handleGpsInactivityRunnable);
            handler.postDelayed(handleGpsInactivityRunnable, ClientConfig.getInstance().GPS_INACTIVITY_NOTIFICATION_DELAY);

            if (!currentlyProcessingSteps) {
                if (isKitkatWithStepSensor(getApplicationContext())) {
                    Logger.d(TAG, "Step Detector present! Will register");
                    stepCounter = new AndroidStepCounter(this, this);
                    stepCounter.start();
                } else {
                    Logger.d(TAG, "Will initiate  GoogleFitStepCounter");
                    stepCounter = new GoogleFitStepCounter(this, this);
                    stepCounter.start();
                }
            }
        }
    }

    @Override
    public synchronized void stopWorkout() {
        Logger.d(TAG, "stopWorkout");
        if (currentlyTracking) {
            handler.removeCallbacks(handleGpsInactivityRunnable);

            if (WorkoutSingleton.getInstance().getDataStore() != null){
                AnalyticsEvent.create(Event.ON_WORKOUT_END)
                        .addBundle(getWorkoutBundle())
                        .put("bolt_count", WorkoutSingleton.getInstance().getDataStore().getUsainBoltCount())
                        .put("num_spikes", WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes())
                        .put("num_update_events", WorkoutSingleton.getInstance().getDataStore().getNumUpdateEvents())
                        .buildAndDispatch();
            }

            if (tracker != null){
                tracker.endRun();
            }

            WorkoutData result = WorkoutSingleton.getInstance().endWorkout();
            handleWorkoutResult(result);

            stopTimer();
            GoogleLocationTracker.getInstance().unregisterWorkout(this);
            currentlyTracking = false;
            WorkoutServiceRetainerAlarm.cancelAlarm(this);
            if (stepCounter != null) {
                stepCounter.stop();
            }
            currentlyProcessingSteps = false;
            unBindFromActivityAndStop();
            distanceInKmsOnLastUpdateEvent = 0f;
            cancelAllWorkoutNotifications();
        }
    }

    private void handleWorkoutResult(final WorkoutData result){

        if (result.isMockLocationDetected()){
            EventBus.getDefault().post(new UpdateUiOnMockLocation());
        }
        else if (result.hasConsecutiveUsainBolts()){
            if (result.isAutoFlagged()){
                EventBus.getDefault().post(new UsainBoltForceExit(true));
            }else {
                EventBus.getDefault().post(new UsainBoltForceExit(false));
            }
        }else if (result.isAutoFlagged()){
            EventBus.getDefault().post(new UpdateUiOnAutoFlagWorkout(result.getAvgSpeed(),
                    Math.round(result.getRecordedTime())));
        }

        sendWorkoutResultBroadcast(result);
        // Do not perform activity detection until the next 24 hours, unless user starts workout
        ActivityDetector.getInstance().stopActivityDetection();

        // Persist run only when distance is more than 100m
        //      & MockLocation is not enabled
        if (result.getDistance() >= mCauseData.getMinDistance()
                && !result.isMockLocationDetected()) {
            persistWorkoutInDb(result);
            // We send the Workout complete event only after we have the estimated data with us
            AnalyticsEvent.create(Event.ON_WORKOUT_COMPLETE)
                    .addBundle(result.getWorkoutBundle())
                    .put("cause_id", mCauseData.getId())
                    .put("cause_title", mCauseData.getTitle())
                    .put("num_spikes", result.getNumGpsSpikes())
                    .put("bolt_count", result.getUsainBoltCount())
                    .put("num_update_events", result.getNumUpdateEvents())
                    .buildAndDispatch();

            // Start background thread to read estimated data from GoogleFit.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    readGoogleFitHistoryAndTriggerSync(result.copy());
                }
            }).start();
        }else {
            // Delete the files in which location data of all the batches of this workout was stored
            for (int i=0; i< result.getBatches().size(); i++) {
                WorkoutBatch batch = result.getBatches().get(i);
                if (MainApplication.getContext().deleteFile(batch.getLocationDataFileName())){
                    Logger.d(TAG, batch.getLocationDataFileName() + " was successfully deleted");
                }
            }
        }
    }

    private void readGoogleFitHistoryAndTriggerSync(WorkoutData result){
        if (googleFitTracker != null){
            // Synchronously read estimated data and update the WorkoutData result object
            googleFitTracker.readAndStop(result);
            updateGoogleFitDetailsInSyncedWorkout(result);
        }

        // Trigger Sync on server
        SyncService.pushWorkoutDataWithBackoff();
    }


    public Properties getWorkoutBundle(){
        return WorkoutSingleton.getInstance().getWorkoutBundle();
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
        if (!WorkoutSingleton.getInstance().isWorkoutActive()) {
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
        EventBus.getDefault().unregister(this);
//        RefWatcher refWatcher = MainApplication.getRefWatcher(getApplicationContext());
//        refWatcher.watch(this);
    }

    final IBinder mBinder = new MyBinder();

    @Override
    public void stepCounterNotAvailable(int reasonCode) {
        Logger.d(TAG, "distanceTrackerNotAvailable, reasonCode = " + reasonCode);
        currentlyProcessingSteps = false;
        SharedPrefsManager.getInstance().setString(Constants.PREF_TYPE_STEP_COUNTER,
                StepCounter.TYPE_NOT_AVAILABLE);
        MainApplication.showToast(R.string.google_fit_permission_rationale);
    }

    @Override
    public void stepCounterReady() {
        Logger.d(TAG, "stepCounterReady");
        currentlyProcessingSteps = true;
        if (isKitkatWithStepSensor(getApplicationContext())){
            SharedPrefsManager.getInstance().setString(Constants.PREF_TYPE_STEP_COUNTER,
                    StepCounter.TYPE_SENSOR_SERVICE);
        }else {
            SharedPrefsManager.getInstance().setString(Constants.PREF_TYPE_STEP_COUNTER,
                    StepCounter.TYPE_GOOGLE_FIT);
        }
    }


    @Override
    public void onStepCount(int deltaSteps) {
        if (tracker != null && tracker.isActive()) {
            tracker.feedSteps(deltaSteps);
            if (stepCounter.getMovingAverageOfStepsPerSec() > 1){
                // User is stepping with average cadence of 1 step per sec for the past few (StepCounter.STEP_COUNT_READING_VALID_INTERVAL) secs
                // Lets notify the ActivityDetector about it
                ActivityDetector.getInstance().persistentMovementDetectedFromOutside();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MockLocationDetected mockLocationDetected) {
        Logger.d(TAG, "onEvent: MockLocationDetected");
        try{
            if (!WorkoutSingleton.getInstance().isMockLocationEnabled()){
                WorkoutSingleton.getInstance().mockLocationDetected();
                pushFraudDataOnServer();
                Logger.d(TAG, "MockLocation detected, Will readAndStop workout");
                stopWorkout();
                MainApplication.showRunNotification(
                        getString(R.string.notification_disable_mock_location_title),
                        WORKOUT_NOTIFICATION_DISABLE_MOCK_ID,
                        getString(R.string.notification_disable_mock_location));
            }
        }catch (Exception e) {
            Logger.e(TAG, "Problem while handling MockLocationDetected event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PauseWorkoutEvent pauseWorkoutEvent) {
        pause(PAUSE_REASON_USER_CLICKED_NOTIFICATION);
        EventBus.getDefault().post(new UpdateUiOnWorkoutPauseEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ResumeWorkoutEvent resumeWorkoutEvent) {
        if (resume()){
            EventBus.getDefault().post(new UpdateUiOnWorkoutResumeEvent());
        }
    }

    @Override
    public void onGoogleFitStepCount(int deltaSteps) {
        if (WorkoutSingleton.getInstance().getDataStore() != null){
            WorkoutSingleton.getInstance().getDataStore().addGoogleFitSteps(deltaSteps);
        }
    }

    @Override
    public void onGoogleFitDistanceUpdate(float deltaDistance) {
        if (WorkoutSingleton.getInstance().getDataStore() != null){
            WorkoutSingleton.getInstance().getDataStore().addGoogleFitDistance(deltaDistance);
        }
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
        Logger.i(TAG, "onLocationChanged with \n" + location.toString());
        cancelGpsInactiveNotification();
        handler.removeCallbacks(handleGpsInactivityRunnable);
        handler.postDelayed(handleGpsInactivityRunnable, ClientConfig.getInstance().GPS_INACTIVITY_NOTIFICATION_DELAY);
        if(tracker != null && tracker.isRunning()) {
            if (!ActivityDetector.getInstance().isStill()){
                // Process GPS fix only when the device is not Still
                if (acceptedLocationFix == null){
                    if (beginningLocationsRotatingQueue == null){
                        Logger.d(TAG, "Hunt for first accepted location begins");
                        beginningLocationsRotatingQueue = new CircularQueue<>(WORKOUT_BEGINNING_LOCATION_CIRCULAR_QUEUE_MAX_SIZE);
                        beginningLocationsRotatingQueue.add(location);
                        // Will not send to tracker as it is the very first location fix received
                        // Will first fill the beginningLocationsRotatingQueue, which will help us in identifying the first accepted point
                        // Will start sending subsequent location fixes, only after they are approved by spike filter
                    } else {
                        // First fill
                        beginningLocationsRotatingQueue.add(location);
                        if (beginningLocationsRotatingQueue.isFull()){
                            Logger.d(TAG, "Rotating queue is full, will check if we can " +
                                    "accept the oldest location: "
                                    + beginningLocationsRotatingQueue.peekOldest().toString());
                            // Check if oldest location is a fit
                            if (isOldestLocationAccepted()){
                                Logger.d(TAG, "Oldest location accepted, will feed to tracker");
                                acceptedLocationFix = beginningLocationsRotatingQueue.peekOldest();
                                tracker.feedLocation(acceptedLocationFix);
                            }
                        }
                    }
                } else {
                    // Spike filter check
                    if (!checkForSpike(acceptedLocationFix, location)){
                        tracker.feedLocation(location);
                        acceptedLocationFix = location;
                    }
                }
            }else {
                Logger.d(TAG, "Will ignore the GPS fix because the device is STILL");
            }
        }

    }

    private boolean isOldestLocationAccepted(){
        synchronized (beginningLocationsRotatingQueue){
            Location oldest = beginningLocationsRotatingQueue.peekOldest();
            for (int i=1; i<beginningLocationsRotatingQueue.getMaxSize(); i++){
                if (checkForSpike(oldest, beginningLocationsRotatingQueue.getElemAtPosition(i))){
                    // There is a spike, must discard Oldest
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Simple spike filter which detects whether given couple of subsequent location fixes is having spike
     * @param loc1
     * @param loc2
     * @return
     */
    public boolean checkForSpike(Location loc1, Location loc2){

        float deltaDistance = loc1.distanceTo(loc2);

        // If distance is less than 35 meters then we will not perform spike filter check
        if (deltaDistance < Config.PRIMARY_SPIKE_FILTER_CHECK_WAIVER_DISTANCE){
            return false;
        }

        long firstLocationTime = loc1.getTime();
        if (LocationManager.NETWORK_PROVIDER.equals(loc1.getProvider())){
            // Location returned by NETWORK_PROVIDER
            // Converting it into servertime as timestamp returned by location object is system time stamp
            firstLocationTime = ServerTimeKeeper.getInstance()
                    .getServerTimeAtSystemTime(firstLocationTime);
        }
        long secondLocationTime = loc2.getTime();
        if (LocationManager.NETWORK_PROVIDER.equals(loc2.getProvider())){
            // Location returned by NETWORK_PROVIDER
            // Converting it into servertime as timestamp returned by location object is system time stamp
            secondLocationTime = ServerTimeKeeper.getInstance()
                    .getServerTimeAtSystemTime(secondLocationTime);
        }

        long deltaTimeInMillis = Math.abs(secondLocationTime - firstLocationTime);
        long deltaTime = deltaTimeInMillis / 1000;

        float deltaSpeed = (deltaDistance * 1000) / deltaTimeInMillis;

        // Determine speed threshold based on User's current context

        float spikeFilterSpeedThreshold;
        String thresholdApplied;

        // recent is avg GPS speed from recent few samples of accepted GPS points in the past 24 secs
        // GPS speed is obtained directly from location object and is calculated using doppler shift
        float recentGpsSpeed = GoogleLocationTracker.getInstance().getRecentGpsSpeed();

        // If recentGpsSpeed is above USAIN_BOLT_GPS_SPEED_LIMIT (21 km/hr) then user must be in a vehicle
        if (ActivityDetector.getInstance().isIsInVehicle()
                || recentGpsSpeed > ClientConfig.getInstance().USAIN_BOLT_GPS_SPEED_LIMIT){
            spikeFilterSpeedThreshold = ClientConfig.getInstance().SPIKE_FILTER_SPEED_THRESHOLD_IN_VEHICLE;
            thresholdApplied = "in_vehicle";
        }else {
            if ( ActivityDetector.getInstance().isOnFoot() ||
                    (isCountingSteps() && stepCounter != null && stepCounter.getMovingAverageOfStepsPerSec() >= ClientConfig.getInstance().MIN_CADENCE_FOR_WALK)){
                // Can make a safe assumption that the person is on foot
                spikeFilterSpeedThreshold = ClientConfig.getInstance().SPIKE_FILTER_SPEED_THRESHOLD_ON_FOOT;
                thresholdApplied = "on_foot";
            } else {
                spikeFilterSpeedThreshold = ClientConfig.getInstance().SPIKE_FILTER_SPEED_THRESHOLD_DEFAULT;
                thresholdApplied = "default";
            }
        }

        // If speed is greater than threshold, then it is considered as GPS spike

        if (deltaSpeed > spikeFilterSpeedThreshold){
            Logger.e(TAG, "Detected GPS spike, between locations loc1:\n" + loc1.toString()
                        + "\n, loc2:\n" + loc2.toString()
                        + "\n Spike distance = " + deltaDistance + " meters in " + deltaTime + " seconds");
            tracker.incrementGpsSpike();
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
        Logger.e(TAG, "GoogleLocationTracker onConnectionFailure, will readAndStop workoutService");
        stopWorkout();
        stopSelf();
    }

    @Override
    public void onGpsEnabled() {
        if (tracker != null && tracker.isPaused()){
            Logger.i(TAG, "onGpsEnabled: Gps enabled while workout was ongoing, user can resume the workout now");
            // User can resume the workout now, if it was paused because of disabled GPS.
            resume();
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                    Constants.BROADCAST_RESUME_WORKOUT_CODE);
            sendBroadcast(bundle);
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
            pause(PAUSE_REASON_GPS_DISABLED);
            // Popup to enable location again
            GoogleLocationTracker.getInstance().startLocationTracking(true);
        }
    }

    @Override
    public void updateWorkoutRecord(float totalDistance, float avgSpeed, float deltaDistance,
                                    int deltaTime, float deltaSpeed, double deltaCalories) {
        Logger.d(TAG, "updateWorkoutRecord: totalDistance = " + totalDistance
                + " avgSpeed = " + avgSpeed + ", deltaSpeed = " + deltaSpeed
                + ", deltaTime = " + deltaTime + ", deltaCalories = " + deltaCalories);
        // Send an update broadcast to Activity
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_WORKOUT_UPDATE_CODE);
        bundle.putFloat(Constants.KEY_WORKOUT_UPDATE_SPEED, deltaSpeed);
        bundle.putFloat(Constants.KEY_WORKOUT_UPDATE_TOTAL_DISTANCE, totalDistance);
        bundle.putInt(Constants.KEY_WORKOUT_UPDATE_ELAPSED_TIME_IN_SECS, tracker.getElapsedTimeInSecs());
        sendBroadcast(bundle);
        updateStickyNotification();
        float totalDistanceKmsTwoDecimal = Float.parseFloat(Utils.formatToKmsWithTwoDecimal(totalDistance));
        if (Math.abs(totalDistanceKmsTwoDecimal - distanceInKmsOnLastUpdateEvent) >= ClientConfig.getInstance().MIN_DISPLACEMENT_FOR_WORKOUT_UPDATE_EVENT){
            // Send event only when the distance counter increment by 100 meters
            AnalyticsEvent.create(Event.ON_WORKOUT_UPDATE)
                    .addBundle(getWorkoutBundle())
                    .put("delta_distance", deltaDistance) // in meters
                    .put("delta_time", deltaTime) // in secs
                    .put("delta_speed", deltaSpeed) // in km/hrs
                    .put("activity", ActivityDetector.getInstance().getCurrentActivity())
                    .put("recent_speed", getCurrentSpeed())
                    .buildAndDispatch();
            distanceInKmsOnLastUpdateEvent = totalDistanceKmsTwoDecimal;
            cancelWorkoutNotification(WORKOUT_NOTIFICATION_STILL_ID);
            if (tracker != null){
                tracker.incrementNumUpdateEvents();
            }
        }
        if (totalDistanceKmsTwoDecimal > Config.MIN_DISTANCE_FOR_FEEDBACK_POPUP){
            WorkoutSingleton.getInstance().setToShowFeedbackDialog(true);
        }
    }

    private void sendBroadcast(Bundle bundle) {
        Intent intent = new Intent(Constants.WORKOUT_SERVICE_BROADCAST_ACTION);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void updateStepsRecord(long timeStampMillis) {
        // Send an update broadcast to Activity
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_STEPS_UPDATE_CODE);
        bundle.putInt(Constants.KEY_WORKOUT_UPDATE_STEPS, tracker.getTotalSteps());
        bundle.putInt(Constants.KEY_WORKOUT_UPDATE_ELAPSED_TIME_IN_SECS, tracker.getElapsedTimeInSecs());
        sendBroadcast(bundle);
    }

    @Override
    public float getMovingAverageOfStepsPerSec() {
        if (stepCounter != null){
            return stepCounter.getMovingAverageOfStepsPerSec();
        }
        return -1;

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
            tracker.pauseRun(reason);
            googleFitTracker.pause();
            //Test: Put stopping locationServices code over here
            Logger.d(TAG, "PauseWorkout");
            AnalyticsEvent.create(Event.ON_WORKOUT_PAUSE)
                    .addBundle(getWorkoutBundle())
                    .put("reason", reason)
                    .put("bolt_count", WorkoutSingleton.getInstance().getDataStore().getUsainBoltCount())
                    .put("num_update_events", WorkoutSingleton.getInstance().getDataStore().getNumUpdateEvents())
                    .buildAndDispatch();
        }
        if (stepCounter != null ){
            stepCounter.pause();
        }
        updateStickyNotification();
        cancelAllWorkoutNotifications();
    }

    @Override
    public boolean resume() {
        Logger.i(TAG, "resume");
        if (GoogleLocationTracker.getInstance().isFetchingLocation()){
            Logger.d(TAG, "resume: LocationFetching going on");
            if (vigilanceTimer != null) {
                vigilanceTimer.resumeTimer();
            }
            if (tracker != null && !tracker.isRunning()) {
                synchronized (this){
                    acceptedLocationFix = null;
                    beginningLocationsRotatingQueue = null;
                }
                tracker.resumeRun();
                googleFitTracker.resume();
                Logger.d(TAG, "ResumeWorkout");
                AnalyticsEvent.create(Event.ON_WORKOUT_RESUME)
                        .addBundle(getWorkoutBundle())
                        .put("bolt_count", WorkoutSingleton.getInstance().getDataStore().getUsainBoltCount())
                        .put("num_update_events", WorkoutSingleton.getInstance().getDataStore().getNumUpdateEvents())
                        .buildAndDispatch();
            }
            if (stepCounter != null){
                stepCounter.resume();
            }
            updateStickyNotification();
            cancelAllWorkoutNotifications();
            return true;
        }else {
            Logger.d(TAG, "resume: need to initiate location fetching");
            GoogleLocationTracker.getInstance().startLocationTracking(true);
            return false;
        }
    }

    private void cancelAllWorkoutNotifications(){
        Logger.d(TAG, "cancelAllWorkoutNotifications");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        cancelGpsInactiveNotification();
        cancelBadGpsNotification();
        manager.cancel(WORKOUT_NOTIFICATION_USAIN_BOLT_ID);
        manager.cancel(WORKOUT_NOTIFICATION_USAIN_BOLT_FORCE_EXIT_ID);
        manager.cancel(WORKOUT_NOTIFICATION_STILL_ID);
        manager.cancel(WORKOUT_NOTIFICATION_DISABLE_MOCK_ID);
    }

    public static void cancelWorkoutNotification(int notificationId){
        Logger.d(TAG, "cancelWorkoutNotification with id = " + notificationId);
        NotificationManager manager = (NotificationManager) MainApplication.getContext()
                .getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }

    @Override
    public void workoutVigilanceSessiondefaulted(int problem) {
        Logger.d(TAG, "workoutVigilanceSessiondefaulted");
        float distanceReduction;
        String distReductionString = null;
        if (tracker != null && tracker.isActive()) {
            distanceReduction = tracker.discardApprovalQueue();
            distReductionString =
                    (distanceReduction == 0) ? null :
                            UnitsManager.formatToMyDistanceUnitWithTwoDecimal(Math.abs(distanceReduction));
        }

        WorkoutSingleton.getInstance().incrementUsainBoltsCounter();
        pushFraudDataOnServer();

        if (!WorkoutSingleton.getInstance().hasConsecutiveUsainBolts()){
            pause(PAUSE_REASON_USAIN_BOLT);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.KEY_PAUSE_WORKOUT_PROBLEM, problem);
            bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                    Constants.BROADCAST_PAUSE_WORKOUT_CODE);
            if (!TextUtils.isEmpty(distReductionString)){
                bundle.putString(Constants.KEY_USAIN_BOLT_DISTANCE_REDUCED, distReductionString);
            }
            sendBroadcast(bundle);
            MainApplication.showRunNotification(
                    getString(R.string.notification_usain_bolt_title),
                    WORKOUT_NOTIFICATION_USAIN_BOLT_ID,
                    getString(R.string.notification_usain_bolt),
                    getString(R.string.notification_action_stop)
            );
        }else {
            // Force stop workout, show notif, And blocking exit popup on UI
            stopWorkout();
            MainApplication.showRunNotification(getString(R.string.notification_usain_bolt_force_exit_title),
                    WORKOUT_NOTIFICATION_USAIN_BOLT_FORCE_EXIT_ID,
                    getString(R.string.notification_usain_bolt_force_exit));
        }
    }

    private void pushFraudDataOnServer(){
        Logger.d(TAG, "pushFraudDataOnServer");
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        if (userDetails == null){
            Logger.e(TAG, "Can't push fraud data as MemberDetails are not present");
            return;
        }
        FraudData data = new FraudData();
        data.setUserId(userDetails.getUserId());
        data.setCauseId((int) mCauseData.getId());
        data.setClientRunId(WorkoutSingleton.getInstance().getDataStore().getWorkoutId());
        data.setMockLocationUsed(WorkoutSingleton.getInstance().isMockLocationEnabled());
        data.setTeamId(LeaderBoardDataStore.getInstance().getMyTeamId());
        data.setTimeStamp(DateUtil.getServerTimeInMillis() / 1000); // epoch in secs
        data.setUsainBoltCount(WorkoutSingleton.getInstance().getDataStore().getUsainBoltCount());

        SyncHelper.pushFraudData(data);
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
            return tracker.getTotalDistanceCovered();
        }
        return 0;
    }

    @Override
    public long getWorkoutElapsedTimeInSecs() {
        if (tracker != null && tracker.isActive()){
            long elapsedTime = tracker.getElapsedTimeInSecs();
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


    private void makeForegroundAndSticky() {
        Notification notification = getForegroundNotificationBuilder().build();
        startForeground(WORKOUT_TRACK_NOTIFICATION_ID, notification);
        stopTimer();
        handler.postDelayed(notificationUpdateTimer, NOTIFICATION_TIMER_TICK);
    }

    private void updateStickyNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (tracker != null && tracker.isActive()){
            // This try catch bug is to avoid crash due to runtimeexception
            try{
                mNotificationManager.notify(WORKOUT_TRACK_NOTIFICATION_ID, getForegroundNotificationBuilder().build());
            }catch (RuntimeException rte){
                rte.printStackTrace();
                Crashlytics.logException(rte);
                Logger.e(TAG, "RuntimeException while updating sticky notification");
            }
        }
    }

    private NotificationCompat.Builder getForegroundNotificationBuilder() {

        int rupees = Utils.convertDistanceToRupees(mCauseData.getConversionRate(), getTotalDistanceCoveredInMeters());
        String amountString = UnitsManager.formatRupeeToMyCurrency(rupees);

        String pauseResumeAction, pauseResumeLabel, contentTitle;
        int pauseResumeIntent;
        int pauseResumeDrawable;
        if (tracker.isRunning()){
            pauseResumeAction = getString(R.string.notification_action_pause);
            pauseResumeLabel = getString(R.string.pause);
            contentTitle = getString(R.string.impact_with_sponsor, mCauseData.getSponsor().getName());
            pauseResumeIntent = MainActivity.INTENT_PAUSE_RUN;
            pauseResumeDrawable = R.drawable.ic_pause_black_24px;
        }else {
            pauseResumeAction = getString(R.string.notification_action_resume);
            pauseResumeLabel = getString(R.string.resume);
            contentTitle = getString(R.string.paused);
            pauseResumeIntent = MainActivity.INTENT_RESUME_RUN;
            pauseResumeDrawable = R.drawable.ic_play_arrow_black_24px;
        }
        /*Intent pauseResumeIntent = new Intent(this, NotificationActionReceiver.class);
        pauseResumeIntent.setAction(pauseResumeAction);
        PendingIntent pendingIntentPauseResume = PendingIntent.getBroadcast(getContext(), 100, pauseResumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);*/

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(contentTitle)
                        .setContentText(amountString
                                + ((getWorkoutElapsedTimeInSecs() >= 60)
                                        ? " raised in " + Utils.secondsToHoursAndMins((int) getWorkoutElapsedTimeInSecs())
                                        : "")
                        )
                        .setSmallIcon(getNotificationIcon())
                        .setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue))
                        .setLargeIcon(getLargeIcon())
                        .setTicker(getBaseContext().getResources().getString(R.string.app_name))
                        .setOngoing(true)
                        .setVisibility(1);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH){
            mBuilder.addAction(pauseResumeDrawable, pauseResumeLabel , MainApplication.getInstance().createNotificationActionIntent(pauseResumeIntent,pauseResumeAction))
                    .addAction(R.drawable.ic_stop_black_24px, "Stop" , MainApplication.getInstance().createNotificationActionIntent(MainActivity.INTENT_STOP_RUN,getString(R.string.notification_action_stop)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        mBuilder.setContentIntent(MainApplication.getInstance().createAppIntent());
        return mBuilder;
    }

    private Bitmap largeIcon;

    private Bitmap getLargeIcon(){
        if (largeIcon == null){
            largeIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.mipmap.ic_launcher);
        }
        return largeIcon;
    }

    private void stopTimer(){
        handler.removeCallbacks(notificationUpdateTimer);
    }

    private Runnable notificationUpdateTimer = new Runnable() {
        @Override
        public void run() {
            updateStickyNotification();
            playVoiceUpdate();
            handler.postDelayed(this, NOTIFICATION_TIMER_TICK);
        }
    };

    private void playVoiceUpdate(){
        WorkoutDataStore dataStore = WorkoutSingleton.getInstance().getDataStore();
        if (dataStore != null
                && !SharedPrefsManager.getInstance().getBoolean(PREF_DISABLE_VOICE_UPDATES)
                && dataStore.isWorkoutRunning()){
            int nextVoiceUpdateAt = dataStore.getNextVoiceUpdateScheduledAt();
            Logger.d(TAG, "nextVoiceUpdateAt = " + nextVoiceUpdateAt);
            if (dataStore.getElapsedTime() >= nextVoiceUpdateAt){
                final String toSpeak = getVoiceUpdateMessage();

                textToSpeech = new TTS(getApplicationContext(), new TTS.InitCallback() {
                    @Override
                    public void initSuccess(TTS tts) {
                        if (WorkoutSingleton.getInstance().getDataStore() != null){
                            textToSpeech.queueSpeech(toSpeak);
                            WorkoutSingleton.getInstance().getDataStore().updateAfterVoiceUpdate();
                        }
                    }

                    @Override
                    public void initFail(int reason) {
                        Logger.d(TAG, "initFail: can't play voice update");
                    }
                });
            }
        }
    }

    public String getVoiceUpdateMessage() {
        float distance = getTotalDistanceCoveredInMeters();
        int rupees = Utils.convertDistanceToRupees(mCauseData.getConversionRate(), distance);

        return getString(R.string.impact_voice_update_text,
                UnitsManager.impactToVoice(rupees),
                UnitsManager.distanceToVoice(distance),
                Utils.secondsToVoiceUpdate((int)getWorkoutElapsedTimeInSecs()));

    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notification_small : R.mipmap.ic_launcher;
    }

    final Runnable handleGpsInactivityRunnable = new Runnable() {
        @Override
        public void run() {
            // GoogleLocationTracker has not sent GPS fix since a long time, need to notify the user about it
            // But will do it only when user is on the move on foot
            Logger.d(TAG, "Not receiving GPS updates for quite sometime now");
            if ( ActivityDetector.getInstance().isOnFoot()
                    || stepCounter.getMovingAverageOfStepsPerSec() > ClientConfig.getInstance().MIN_CADENCE_FOR_WALK){
                notifyUserAboutGpsInactivity();
            }
        }
    };

    public void notifyUserAboutGpsInactivity(){
        MainApplication.showRunNotification(
                getString(R.string.notification_gps_inactivity_title),
                WORKOUT_NOTIFICATION_GPS_INACTIVE_ID,
                getString(R.string.notification_gps_inactivity_content),
                getString(R.string.notification_action_pause),
                getString(R.string.notification_action_stop)
        );
        WorkoutSingleton.getInstance().setGpsState(WorkoutSingleton.GPS_STATE_INACTIVE);
        EventBus.getDefault().post(new GpsStateChangeEvent());
        AnalyticsEvent.create(Event.DISP_GPS_NOT_ACTIVE_NOTIF)
                .put("time_considered_ad", ActivityDetector.getInstance().getTimeCoveredByHistoryQueueInSecs())
                .buildAndDispatch();
    }

    @Override
    public void notifyUserAboutBadGps(){
        MainApplication.showRunNotification(
                getString(R.string.notification_bad_gps_title),
                WORKOUT_NOTIFICATION_BAD_GPS_ID,
                getString(R.string.notification_bad_gps_content),
                getString(R.string.notification_action_pause),
                getString(R.string.notification_action_stop)
        );
        WorkoutSingleton.getInstance().setGpsState(WorkoutSingleton.GPS_STATE_BAD);
        EventBus.getDefault().post(new GpsStateChangeEvent());
        AnalyticsEvent.create(Event.DISP_BAD_GPS_NOTIF)
                .put("time_considered_ad", ActivityDetector.getInstance().getTimeCoveredByHistoryQueueInSecs())
                .buildAndDispatch();
    }

    @Override
    public void cancelBadGpsNotification() {
        cancelWorkoutNotification(WORKOUT_NOTIFICATION_BAD_GPS_ID);
        if (WorkoutSingleton.getInstance().getGpsState() == GPS_STATE_BAD){
            WorkoutSingleton.getInstance().setGpsState(GPS_STATE_OK);
            EventBus.getDefault().post(new GpsStateChangeEvent());
        }
    }

    public void cancelGpsInactiveNotification(){
        cancelWorkoutNotification(WORKOUT_NOTIFICATION_GPS_INACTIVE_ID);
        if (WorkoutSingleton.getInstance().getGpsState() == GPS_STATE_INACTIVE){
            WorkoutSingleton.getInstance().setGpsState(GPS_STATE_OK);
            EventBus.getDefault().post(new GpsStateChangeEvent());
        }
    }

}
