package com.sharesmile.share.gps;

import android.hardware.SensorEvent;
import android.location.Location;
import android.text.TextUtils;

import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
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
    private DistRecord lastRecord;

    public RunTracker(ScheduledExecutorService executorService, UpdateListner listener){
        synchronized (RunTracker.class){
            this.executorService = executorService;
            this.listener = listener;
            if (isActive()){
                dataStore = new WorkoutDataStoreImpl();
                String prevRecordAsString = SharedPrefsManager.getInstance().getString(Constants.PREF_PREV_DIST_RECORD);
                if (!TextUtils.isEmpty(prevRecordAsString)){
                    lastRecord = Utils.createObjectFromJSONString(prevRecordAsString, DistRecord.class);
                }
                setState(State.valueOf(SharedPrefsManager.getInstance().getString(Constants.PREF_WORKOUT_STATE,
                                State.PAUSED.name())));
            }else{
                // User started workout
                setState(State.RUNNING);
                dataStore = new WorkoutDataStoreImpl(System.currentTimeMillis());
                resumeRun();
            }
        }
    }

    public synchronized WorkoutData endRun(){
        Logger.d(TAG, "endRun");
        WorkoutData workoutData = dataStore.clear();
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_PREV_DIST_RECORD);
        dataStore = null;
        setState(State.IDLE);
        listener = null;
        return workoutData;
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
    public float getDistanceCoveredSinceLastResume() {
        return dataStore.getDistanceCoveredSinceLastResume();
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
        if (isActive()){
            return lastRecord;
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

    @Override
    public void discardApprovalQueue() {
        dataStore.discardApprovalQueue();
    }

    private synchronized void processSteps(int deltaSteps){
        if (isRunning()){
            // Below logic was required when we were getting cumulative steps, with google fit, it is not required

            long reportimeStamp = System.currentTimeMillis();
            Logger.d(TAG, "Adding " + deltaSteps + "steps.");
            dataStore.addSteps(deltaSteps);
            listener.updateStepsRecord(reportimeStamp);
        }
    }


    //TODO: revise this method to handle pause resume
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
                    lastRecord = startRecord;
                    SharedPrefsManager.getInstance().setObject(Constants.PREF_PREV_DIST_RECORD, lastRecord);
                }
            }else{
                Logger.d(TAG,"Processing Location:\n " + point.toString());
                long ts = point.getTime();
                Location prevLocation = lastRecord.getLocation();
                long prevTs = prevLocation.getTime();
                float interval = ((float) (ts - prevTs)) / 1000;
                float dist = prevLocation.distanceTo(point);

                // Step 1: Check whether threshold interval for recording has elapsed
                if (interval > Config.THRESHOLD_INTEVAL){

                    boolean toRecord = false;
                    float accuracy = point.getAccuracy();
                    /*
                     Step 2: Record if point is accurate, i.e. accuracy better/lower than our threshold
                             Else
                             Apply formula to check whether to record the point or not
                      */
                    if (accuracy < Config.THRESHOLD_ACCURACY){
                        Logger.d(TAG, "Accuracy Wins");
                        toRecord = true;
                    }else{
                        toRecord = checkUsingFormula(dist, point.getAccuracy());
                    }
                    // Step 3: Record if needed, else wait for next location
                    if (toRecord){
                        DistRecord record = new DistRecord(point, prevLocation, dist);
                        Logger.d(TAG,"Distance Recording: " + record.toString());
                        dataStore.addRecord(record);
                        lastRecord = record;
                        SharedPrefsManager.getInstance().setObject(Constants.PREF_PREV_DIST_RECORD, lastRecord);
                        listener.updateWorkoutRecord(dataStore.getTotalDistance(), record.getSpeed());
                    }
                }
            }
        }
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

        void updateWorkoutRecord(float totalDistance, float currentSpeed);

        void updateStepsRecord(long timeStampMillis);

    }

}
