package com.sharesmile.share.gps;

import android.location.Location;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.gps.models.WorkoutDataImpl;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class WorkoutDataStoreImpl implements WorkoutDataStore{

    private static final String TAG = "WorkoutDataStoreImpl";

    private WorkoutData dirtyWorkoutData;
    private WorkoutData approvedWorkoutData;
    private int numStepsWhenBatchBegan;

    Queue<DistRecord> waitingForApprovalQueue = new ConcurrentLinkedQueue<>();
    volatile float extraPolatedDistanceToBeApproved = 0f;
    volatile int numStepsToBeApproved = 0;

    WorkoutDataStoreImpl(){
        dirtyWorkoutData = retrieveFromPersistentStorage(Constants.PREF_WORKOUT_DATA_DIRTY);
        approvedWorkoutData = retrieveFromPersistentStorage(Constants.PREF_WORKOUT_DATA_APPROVED);
        if (dirtyWorkoutData == null){
            init(DateUtil.getServerTimeInMillis());
        }
        numStepsWhenBatchBegan = SharedPrefsManager.getInstance().getInt(Constants.PREF_WORKOUT_DATA_NUM_STEPS_WHEN_BATCH_BEGIN);
    }

    WorkoutDataStoreImpl(long beginTimeStamp){
        init(beginTimeStamp);
    }

    public void init(long beginTimeStamp){
        String workoutId = UUID.randomUUID().toString();
        dirtyWorkoutData = new WorkoutDataImpl(beginTimeStamp, workoutId);
        approvedWorkoutData = new WorkoutDataImpl(beginTimeStamp, workoutId);
        //Persist dirtyWorkoutData object over here
        persistBothWorkoutData();
    }

    @Override
    public float getTotalDistance(){
        return dirtyWorkoutData.getDistance();
    }

    @Override
    public long getBeginTimeStamp() {
        return dirtyWorkoutData.getBeginTimeStamp();
    }

    @Override
    public void addRecord(DistRecord record) {
        if (!isWorkoutRunning()){
            Logger.i(TAG, "Won't add add record as user is not running");
            return;
        }

        if (record.isFirstRecordAfterResume()){
            //Source point fetched for the batch
            int stepsRanWhileSearchingForSource = getTotalSteps() - numStepsWhenBatchBegan;

            float timeElapsedWhileSearchingForSource =
                    (DateUtil.getServerTimeInMillis() - dirtyWorkoutData.getCurrentBatch().getStartTimeStamp()) / 1000; // in secs

            if (stepsRanWhileSearchingForSource > (Config.MAX_STEPS_PER_SECOND_FACTOR * timeElapsedWhileSearchingForSource)){
                // Num steps recorded is too large, will ignore
                stepsRanWhileSearchingForSource = 0;
            }

            float averageStrideLength = (RunTracker.getAverageStrideLength() == 0)
                        ? (Config.GLOBAL_AVERAGE_STRIDE_LENGTH) : RunTracker.getAverageStrideLength();

            // Normalising averageStrideLength obtained
            if (averageStrideLength < 0.25f){
                averageStrideLength = 0.25f;
            }
            if (averageStrideLength > 1f){
                averageStrideLength = 1f;
            }

            float extraPolatedDistance = stepsRanWhileSearchingForSource * averageStrideLength;

            dirtyWorkoutData.addDistance(extraPolatedDistance);
            extraPolatedDistanceToBeApproved = extraPolatedDistance;
            Location startLocation = record.getLocation();
            dirtyWorkoutData.setStartPoint(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()));
            approvedWorkoutData.setStartPoint(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()));
            Logger.d(TAG, "addRecord: Source record after begin/resume, extraPolatedDistanceToBeApproved = "
                    + extraPolatedDistance);
            AnalyticsEvent.create(Event.ON_START_LOCATION_AFTER_RESUME)
                    .addBundle(getWorkoutBundle())
                    .put("start_location_latitude", startLocation.getLatitude())
                    .put("start_location_longitude", startLocation.getLongitude())
                    .put("extrapolated_distance", extraPolatedDistance)
                    .buildAndDispatch();
        }

        Logger.d(TAG, "addRecord: adding record to ApprovalQueue: " + record.toString());
        dirtyWorkoutData.addRecord(record);
        waitingForApprovalQueue.add(record);
    }

    @Override
    public Properties getWorkoutBundle(){
        Properties p = new Properties();
        p.put("distance", Utils.formatToKmsWithOneDecimal(getTotalDistance()));
        p.put("time_elapsed", getElapsedTime());
        p.put("avg_speed", getAvgSpeed() * (3.6f));
        p.put("num_steps", getTotalSteps());
        p.put("client_run_id", getWorkoutId());
        return p;
    }

    @Override
    public float getAvgSpeed() {
        return dirtyWorkoutData.getAvgSpeed();
    }

    @Override
    public void addSteps(int numSteps){
        if (isWorkoutRunning()){
            dirtyWorkoutData.addSteps(numSteps);
            numStepsToBeApproved += numSteps;
        }
    }

    @Override
    public int getTotalSteps(){
        return dirtyWorkoutData.getTotalSteps();
    }

    @Override
    public float getDistanceCoveredSinceLastResume() {
        return dirtyWorkoutData.getCurrentBatch().getDistance();
    }

    @Override
    public long getLastResumeTimeStamp() {
        return dirtyWorkoutData.getCurrentBatch().getStartTimeStamp();
    }

    @Override
    public float getElapsedTime() {
        return dirtyWorkoutData.getElapsedTime();
    }

    @Override
    public float getRecordedTime() {
        return dirtyWorkoutData.getRecordedTime();
    }

    @Override
    public boolean coldStartAfterResume() {
        return dirtyWorkoutData.coldStartAfterResume();
    }

    @Override
    public void workoutPause() {
        Logger.d(TAG, "workoutPause");
        dirtyWorkoutData.workoutPause();
        approvedWorkoutData.workoutPause();
        // If it was a defaulter scenario then the queue has already been discarded
        approveWorkoutData();
        persistBothWorkoutData();
    }

    @Override
    public void workoutResume() {
        Logger.d(TAG, "workoutResume");
        dirtyWorkoutData.workoutResume();
        approvedWorkoutData.workoutResume();
        numStepsWhenBatchBegan = getTotalSteps();
        persistBothWorkoutData();
    }

    @Override
    public boolean isWorkoutRunning() {
        return dirtyWorkoutData.isRunning();
    }

    @Override
    public synchronized void approveWorkoutData() {
        Logger.d(TAG, "approveWorkoutData");
        if (extraPolatedDistanceToBeApproved > 0){
            approvedWorkoutData.addDistance(extraPolatedDistanceToBeApproved);
            extraPolatedDistanceToBeApproved = 0;
        }
        while (!waitingForApprovalQueue.isEmpty()){
            DistRecord record = waitingForApprovalQueue.remove();
            Logger.d(TAG, "Approving record: " + record.toString());
            approvedWorkoutData.addRecord(record);
        }
        approvePendingSteps();
        persistBothWorkoutData();
    }

    private synchronized void approvePendingSteps(){
        if (numStepsToBeApproved > 0){
            approvedWorkoutData.addSteps(numStepsToBeApproved);
            numStepsToBeApproved = 0;
        }
    }

    @Override
    public synchronized void discardApprovalQueue() {
        extraPolatedDistanceToBeApproved = 0;
        waitingForApprovalQueue.clear();
        numStepsToBeApproved = 0;
        // Cleansing DirtyWorkoutData as user defaulted
        dirtyWorkoutData = approvedWorkoutData.copy();
        persistDirtyWorkoutData();
    }

    @Override
    public LatLng getStartPoint() {
        return dirtyWorkoutData.getStartPoint();
    }

    @Override
    public String getWorkoutId() {
        return dirtyWorkoutData.getWorkoutId();
    }

    @Override
    public WorkoutData clear(){
        Logger.d(TAG, "clear: approving workoutData one last time because this is the end");
        if (extraPolatedDistanceToBeApproved > 0){
            approvedWorkoutData.addDistance(extraPolatedDistanceToBeApproved);
            extraPolatedDistanceToBeApproved = 0;
        }
        while (!waitingForApprovalQueue.isEmpty()){
            DistRecord record = waitingForApprovalQueue.remove();
            Logger.d(TAG, "Approving record: " + record.toString());
            approvedWorkoutData.addRecord(record);
        }
        approvePendingSteps();
        clearPersistentStorage();
        return approvedWorkoutData.close();
    }

    private void persistBothWorkoutData() {
        persistDirtyWorkoutData();
        if (approvedWorkoutData != null){
            SharedPrefsManager.getInstance().setObject(Constants.PREF_WORKOUT_DATA_APPROVED, approvedWorkoutData.copy());
        }
        SharedPrefsManager.getInstance().setInt(Constants.PREF_WORKOUT_DATA_NUM_STEPS_WHEN_BATCH_BEGIN, numStepsWhenBatchBegan);
    }

    private void persistDirtyWorkoutData(){
        if (dirtyWorkoutData != null){
            SharedPrefsManager.getInstance().setObject(Constants.PREF_WORKOUT_DATA_DIRTY, dirtyWorkoutData.copy());
        }
    }

    private WorkoutData retrieveFromPersistentStorage(String key) {
        Logger.d(TAG, "retrieveFromPersistentStorage, key = " + key);
        String workoutDataAsString = SharedPrefsManager.getInstance().getString(key);
        if (!TextUtils.isEmpty(workoutDataAsString)){
            return Utils.createObjectFromJSONString(workoutDataAsString, WorkoutDataImpl.class);
        }
        return null;
    }

    private void clearPersistentStorage() {
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_WORKOUT_DATA_DIRTY);
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_WORKOUT_DATA_APPROVED);
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_WORKOUT_DATA_NUM_STEPS_WHEN_BATCH_BEGIN);
    }
}
