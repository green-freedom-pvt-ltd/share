package com.sharesmile.share.gps;

import android.location.Location;
import android.text.TextUtils;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.gps.models.WorkoutDataImpl;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class WorkoutDataStoreImpl implements WorkoutDataStore{

    private static final String TAG = "WorkoutDataStoreImpl";

    private WorkoutData workoutData;

    public WorkoutDataStoreImpl(){
        workoutData = retrieveFromPersistentStorage();
        if (workoutData == null){
            throw new IllegalStateException("Workout is active but Couldn't find workout data in persistent storage");
        }
    }

    public WorkoutDataStoreImpl(long beginTimeStamp){
        workoutData = new WorkoutDataImpl(beginTimeStamp);
        //Persist workoutData object over here
        persistWorkoutData();
    }

    @Override
    public float getTotalDistance(){
        return workoutData.getDistance();
    }

    @Override
    public long getBeginTimeStamp() {
        return workoutData.getBeginTimeStamp();
    }

    @Override
    public void addRecord(DistRecord record) {
        if (!isWorkoutRunning()){
            Logger.i(TAG, "Won't add add record as user is not running");
            return;
        }

        if (workoutData.getCurrentBatch().getPoints().size() == 1){
            //Start point has been added after start/resume of workout and it is the second record after it
            // Need to extrapolate the distance for time elapsed since start/resume and start point addition
            Location startPoint = record.getPrevLocation();
            long batchInitiateTimeStamp = workoutData.getCurrentBatch().getStartTimeStamp();
            float timeToFetchSource = ((float) (startPoint.getTime() - batchInitiateTimeStamp)) / 1000;
            float speedForExtrapolation = record.getSpeed();
            float extraPolatedDistance = timeToFetchSource * speedForExtrapolation;
            workoutData.addDistance(extraPolatedDistance);
        }

        workoutData.addRecord(record);

        // Persist workoutData object
        persistWorkoutData();
    }

    @Override
    public void addSteps(int numSteps){
        if (isWorkoutRunning()){
            workoutData.addSteps(numSteps);
            persistWorkoutData();
        }
    }

    @Override
    public int getTotalSteps(){
        return workoutData.getTotalSteps();
    }

    @Override
    public float getDistanceCoveredSinceLastResume() {
        return workoutData.getCurrentBatch().getDistance();
    }

    @Override
    public long getLastResumeTimeStamp() {
        return workoutData.getCurrentBatch().getStartTimeStamp();
    }

    @Override
    public boolean coldStartAfterResume() {
        return workoutData.coldStartAfterResume();
    }

    @Override
    public void workoutPause() {
        workoutData.workoutPause();
    }

    @Override
    public void workoutResume() {
        workoutData.workoutResume();
    }

    @Override
    public boolean isWorkoutRunning() {
        return workoutData.isRunning();
    }

    @Override
    public WorkoutData clear(){
        clearPersistentStorage();
        return workoutData.close();
    }

    @Override
    public void persistWorkoutData() {
        if (workoutData != null){
            SharedPrefsManager.getInstance().setObject(Constants.PREF_WORKOUT_DATA, workoutData);
        }
    }

    @Override
    public WorkoutData retrieveFromPersistentStorage() {
        String workoutDataAsString = SharedPrefsManager.getInstance().getString(Constants.PREF_WORKOUT_DATA);
        if (!TextUtils.isEmpty(workoutDataAsString)){
            return Utils.createObjectFromJSONString(workoutDataAsString, WorkoutData.class);
        }
        return null;
    }

    @Override
    public void clearPersistentStorage() {
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_WORKOUT_DATA);
    }
}
