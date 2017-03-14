package com.sharesmile.share.gps;

import android.location.Location;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.gps.models.WorkoutDataImpl;
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
            throw new IllegalStateException("Workout is active but Couldn't find workout data in persistent storage");
        }
    }

    WorkoutDataStoreImpl(long beginTimeStamp){
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
            float averageStrideLength = (RunTracker.getAverageStrideLength() == 0)
                                            ? (Config.GLOBAL_AVERAGE_STRIDE_LENGTH) : RunTracker.getAverageStrideLength();
            float extraPolatedDistance = stepsRanWhileSearchingForSource * averageStrideLength;
            //TODO: Add a startPoint identified analytics event.
            //TODO: Fix averageStrideLength logic to calculate extraPolatedDistanceToBeApproved.
            dirtyWorkoutData.addDistance(extraPolatedDistance);
            extraPolatedDistanceToBeApproved = extraPolatedDistance;
            Location startLocation = record.getLocation();
            dirtyWorkoutData.setStartPoint(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()));
            approvedWorkoutData.setStartPoint(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()));
            Logger.d(TAG, "addRecord: Source record after begin/resume, extraPolatedDistanceToBeApproved = "
                    + extraPolatedDistance);
        }

        Logger.d(TAG, "addRecord: adding record to ApprovalQueue: " + record.toString());
        dirtyWorkoutData.addRecord(record);
        waitingForApprovalQueue.add(record);
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
    }
}
