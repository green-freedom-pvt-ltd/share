package com.sharesmile.share.gps;

import android.location.Location;
import android.text.TextUtils;

import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.CircularQueue;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class RunTracker implements Tracker {

    private static final String TAG = "RunTracker";

    private WorkoutDataStore dataStore;
    private UpdateListner listener;
    private ScheduledExecutorService executorService;
    private CircularQueue<DistRecord> recordHistoryQueue;

    public RunTracker(ScheduledExecutorService executorService, UpdateListner listener){
        synchronized (RunTracker.class){
            this.executorService = executorService;
            this.listener = listener;
            recordHistoryQueue = new CircularQueue<>(5);
            if (isActive()){
                dataStore = new WorkoutDataStoreImpl();
                String prevRecordAsString = SharedPrefsManager.getInstance().getString(Constants.PREF_PREV_DIST_RECORD);
                if (!TextUtils.isEmpty(prevRecordAsString)){
                    recordHistoryQueue.add(Utils.createObjectFromJSONString(prevRecordAsString, DistRecord.class));
                }
                setState(State.valueOf(SharedPrefsManager.getInstance().getString(Constants.PREF_WORKOUT_STATE,
                                State.PAUSED.name())));
            }else{
                // User started workout
                setState(State.RUNNING);
                dataStore = new WorkoutDataStoreImpl(DateUtil.getServerTimeInMillis());
                resumeRun();
            }
        }
    }

    public synchronized WorkoutData endRun(){
        Logger.d(TAG, "endRun");
        WorkoutData workoutData = dataStore.clear();

        // Update total distance and steps in lifetime
        updateTrackRecord(workoutData);

        SharedPrefsManager.getInstance().removeKey(Constants.PREF_PREV_DIST_RECORD);
        dataStore = null;
        setState(State.IDLE);
        listener = null;
        return workoutData;
    }

    private static void updateTrackRecord(WorkoutData workoutData){
        long lifetimeDistance = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE);
        long lifetimeSteps = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_STEPS);
        long lifetimeWorkingOut = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_WORKING_OUT);
        SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE, lifetimeDistance
                + (long) workoutData.getDistance());
        SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_LIFETIME_STEPS, lifetimeSteps
                + (long) workoutData.getTotalSteps());
        SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_LIFETIME_WORKING_OUT, lifetimeWorkingOut
                + (long) workoutData.getRecordedTime());

        Analytics.getInstance().setUserProperty("LifeTimeDistance",
                SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE));
        Analytics.getInstance().setUserProperty("LifeTimeSteps",
                SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_STEPS));
        Analytics.getInstance().setUserProperty("AvgStrideLength", getAverageStrideLength());
        Analytics.getInstance().setUserProperty("AvgSpeed", getLifetimeAverageSpeed());
        Analytics.getInstance().setUserProperty("AvgCadence", getLifetimeAverageStepsPerSec());
    }

    public static float getAverageStrideLength(){
        long lifetimeDistance = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE);
        long lifetimeSteps = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_STEPS);

        if (lifetimeDistance == 0 || lifetimeSteps == 0){
            return 0;
        }
        return ((float) lifetimeDistance ) / ((float) lifetimeSteps);
    }

    public static float getLifetimeAverageSpeed(){
        long lifetimeDistance = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE);
        long lifetimeWorkingOut = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_WORKING_OUT);

        if (lifetimeDistance == 0 || lifetimeWorkingOut == 0){
            return 0;
        }
        return ((float) lifetimeDistance ) / ((float) lifetimeWorkingOut);
    }

    public static float getLifetimeAverageStepsPerSec(){
        long lifetimeSteps = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_STEPS);
        long lifetimeWorkingOut = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_WORKING_OUT);

        if (lifetimeSteps == 0 || lifetimeWorkingOut == 0){
            return 0;
        }
        return ((float) lifetimeSteps ) / ((float) lifetimeWorkingOut);
    }

    @Override
    public Tracker.State getState() {
        return State.valueOf(SharedPrefsManager.getInstance().getString(Constants.PREF_WORKOUT_STATE, State.IDLE.name()));
    }

    private void setState(Tracker.State state){
        SharedPrefsManager.getInstance().setString(Constants.PREF_WORKOUT_STATE, state.name());
    }

    @Override
    public synchronized void pauseRun() {
        Logger.d(TAG, "pauseRun");
        setState(State.PAUSED);
        dataStore.workoutPause();
    }

    @Override
    public synchronized void resumeRun() {
        Logger.d(TAG, "resumeRun");
        setState(State.RUNNING);
        dataStore.workoutResume();
    }

    @Override
    public long getLastResumeTimeStamp() {
        return dataStore.getLastResumeTimeStamp();
    }

    @Override
    public int getElapsedTimeInSecs() {
        return (int)dataStore.getElapsedTime();
    }

    @Override
    public float getRecordedTimeInSecs() {
        return dataStore.getRecordedTime();
    }

    @Override
    public float getDistanceCoveredSinceLastResume() {
        return dataStore.getDistanceCoveredSinceLastResume();
    }

    @Override
    public float getAvgSpeed() {
        if (dataStore != null){
            return dataStore.getAvgSpeed();
        }
        return 0;
    }

    @Override
    public float getCurrentSpeed() {
        float distCovered = 0f; // in meters
        float timeTaken = 0; // in millis
        if (!recordHistoryQueue.isEmpty()){
            if (recordHistoryQueue.getCurrentSize() == 1){
                // Only one record is present
                if (!getLastRecord().isFirstRecordAfterResume() && !getLastRecord().isTooOld()){
                    return getLastRecord().getSpeed();
                }
            }else {
                // More than one record present in the list
                for (int i=recordHistoryQueue.getCurrentSize(); i >= 0; i--){
                    // latest record first
                    DistRecord qRecord = recordHistoryQueue.getElemAtPosition(i);
                    if (qRecord.isTooOld()){
                        break;
                    }else {
                        distCovered += qRecord.getDist();
                        timeTaken += qRecord.getInterval();
                    }
                }
            }
        }

        if (distCovered == 0){
            return 0;
        }else if (timeTaken > 0){
            return (distCovered*1000f) / timeTaken;
        }else {
            return 0;
        }
    }

    /**
     * Returns true iff state is PAUSED
     * @return
     */
    @Override
    public synchronized boolean isPaused() {
        return State.PAUSED.equals(getState());
    }

    /**
     * Returns true iff state is RUNNING
     * @return
     */
    @Override
    public synchronized boolean isRunning() {
        return State.RUNNING.equals(getState());
    }

    @Override
    public long getBeginTimeStamp(){
        if (isActive() && dataStore != null){
            return dataStore.getBeginTimeStamp();
        }
        return 0;
    }

    @Override
    public int getTotalSteps(){
        if (isActive() && dataStore != null){
            return dataStore.getTotalSteps();
        }
        return 0;
    }

    @Override
    public DistRecord getLastRecord(){
        if (isActive() && !recordHistoryQueue.isEmpty()){
            return recordHistoryQueue.peekLatest();
        }
        return null;
    }

    @Override
    public String getCurrentWorkoutId() {
        if (isActive() && dataStore != null){
            return dataStore.getWorkoutId();
        }
        return null;
    }

    @Override
    public float getTotalDistanceCovered(){
        if (isActive() && dataStore != null){
            return dataStore.getTotalDistance();
        }
        return 0;
    }

    @Override
    public void feedLocation(final Location point){
        if (!isActive()){
            throw new IllegalStateException("Can't feed locations without beginning run");
        }
        if (isPaused()){
            Logger.d(TAG, "Wont process location, as the workout is paused");
        }else{
            // state must be running
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    processLocation(point);
                }
            });
        }
    }


    @Override
    public void feedSteps(final int deltaSteps){
        if (isPaused()){
            Logger.d(TAG, "Wont process steps, as the workout is paused");
        }else if (isRunning()){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    processSteps(deltaSteps);
                }
            });
        }
    }

    @Override
    public void approveWorkoutData() {
        dataStore.approveWorkoutData();
    }

    /**
     * Discards distance and steps pending approval and return the distance amount which was reduced (negative value)
     * @return
     */
    @Override
    public float discardApprovalQueue() {

        float distBeforeDiscard = dataStore.getTotalDistance();
        float recordedTimeBeforeDiscard = dataStore.getRecordedTime();

        dataStore.discardApprovalQueue();

        float deltaDistance = dataStore.getTotalDistance() - distBeforeDiscard; // in meters, should be negative
        float deltaTime = dataStore.getRecordedTime() - recordedTimeBeforeDiscard; // in secs
        float deltaSpeed = (deltaDistance / deltaTime) * 3.6f; // in km/hrs
        listener.updateWorkoutRecord(dataStore.getTotalDistance(), dataStore.getAvgSpeed(),
                deltaDistance, Math.round(deltaTime), deltaSpeed);
        return deltaDistance;
    }

    private synchronized void processSteps(int deltaSteps){
        if (isRunning()){
            // Below logic was required when we were getting cumulative steps, with google fit, it is not required

            long reportimeStamp = DateUtil.getServerTimeInMillis();
            dataStore.addSteps(deltaSteps);
            listener.updateStepsRecord(reportimeStamp);
        }
    }


    private synchronized void processLocation(Location point){
        if (isRunning()){
            //Check if the start point has been detected since the workout started/resumed
            if (dataStore.coldStartAfterResume()){
                // This is a potential source location
                Logger.i(TAG, "Checking for source, accuracy = " + point.getAccuracy());
                if (point.getAccuracy() < Config.SOURCE_ACCEPTABLE_ACCURACY){
                    // Set has source only when it has acceptable accuracy
                    Logger.i(TAG, "Source Location with good accuracy fetched:\n " + point.toString());
                    DistRecord startRecord = new DistRecord(point);
                    dataStore.addRecord(startRecord);
                    recordHistoryQueue.add(startRecord);
                    SharedPrefsManager.getInstance().setObject(Constants.PREF_PREV_DIST_RECORD, startRecord);
                }
            }else{
                Logger.d(TAG,"Processing Location:\n " + point.toString());
                long ts = point.getTime();
                Location prevLocation = getLastRecord().getLocation();
                long prevTs = prevLocation.getTime();
                float interval = ((float) (ts - prevTs)) / 1000;
                float dist = prevLocation.distanceTo(point);

                // Step 1: Check whether threshold interval for recording has elapsed
                if (interval > Config.THRESHOLD_INTEVAL){

                    boolean toRecord = false;
                    /*
                    Step 2: Secondary check for spike
                     */
                    float deltaSpeedMs = dist / interval;
                    if (deltaSpeedMs > Config.SPIKE_FILTER_SPEED_THRESHOLD_IN_VEHICLE){
                        // Insanely high velocity, must be a GPS spike
                        toRecord = false;
                        Logger.i(TAG, "GPS spike detected in RunTracker, through secondary check");
                        AnalyticsEvent.create(Event.DETECTED_GPS_SPIKE)
                                .addBundle(getWorkoutBundle())
                                .put("spikey_distance", dist)
                                .put("time_interval", interval)
                                .put("accuracy", point.getAccuracy())
                                .put("threshold_applied", "secondary_check")
                                .put("steps_per_sec_moving_average", listener.getMovingAverageOfStepsPerSec())
                                .buildAndDispatch();
                    }else {
                        /*
                         Step 3: Record if point is accurate, i.e. accuracy better/lower than our threshold
                                 Else
                                 Apply formula to check whether to record the point or not
                          */
                        float accuracy = point.getAccuracy();
                        if (accuracy < Config.THRESHOLD_ACCURACY){
                            Logger.d(TAG, "Accuracy Wins");
                            toRecord = true;
                        }else{
                            toRecord = checkUsingFormula(dist, point.getAccuracy());
                        }
                    }

                    // Step 4: Record if needed, else wait for next location
                    if (toRecord){
                        DistRecord record = new DistRecord(point, prevLocation, dist);
                        Logger.d(TAG, "Distance Recording: " + record.toString());
                        dataStore.addRecord(record);
                        recordHistoryQueue.add(record);
                        SharedPrefsManager.getInstance().setObject(Constants.PREF_PREV_DIST_RECORD, record);
                        float deltaDistance = dist; // in meters
                        int deltaTime = Math.round(record.getInterval()); // in secs
                        float deltaSpeed = record.getSpeed() * 3.6f; // in km/hrs
                        listener.updateWorkoutRecord(dataStore.getTotalDistance(), dataStore.getAvgSpeed(),
                                deltaDistance, deltaTime, deltaSpeed);
                    }
                }
            }
        }
    }

    public Properties getWorkoutBundle(){
        if (dataStore != null){
            return dataStore.getWorkoutBundle();
        }
        return null;
    }

    private boolean checkUsingFormula(float dist, float accuracy){
        float deltaAccuracy = accuracy - (Config.THRESHOLD_ACCURACY - Config.THRESHOLD_ACCURACY_OFFSET);
        float value = (dist / deltaAccuracy);
        Logger.d(TAG, "Applying formula, dist = " + dist + " accuracy = " + accuracy + " value = " + value);
        if ( value > Config.THRESHOLD_FACTOR){
            return true;
        }
        return false;
    }


    /**
     * Returns true if a workout session is currently in progress, could be in paused OR running state
     * @return
     */
    @Override
    public boolean isActive(){
        return (getState() != State.IDLE);
    }

    public static boolean isWorkoutActive(){
        return (State.valueOf(SharedPrefsManager.getInstance().getString(Constants.PREF_WORKOUT_STATE, State.IDLE.name()))
                != State.IDLE);
    }

    interface UpdateListner {

        void updateWorkoutRecord(float totalDistance, float avgSpeed,
                                 float deltaDistance, int deltaTime,
                                 float deltaSpeed);

        void updateStepsRecord(long timeStampMillis);

        float getMovingAverageOfStepsPerSec();

    }

}
