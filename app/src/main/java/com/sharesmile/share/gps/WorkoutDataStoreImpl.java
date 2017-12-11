package com.sharesmile.share.gps;

import android.location.Location;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.ClientConfig;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.Calorie;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.gps.models.WorkoutDataImpl;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sharesmile.share.core.Config.CONSECUTIVE_USAIN_BOLT_WAIVER_TIME_INTERVAL;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class WorkoutDataStoreImpl implements WorkoutDataStore{

    private static final String TAG = "WorkoutDataStoreImpl";

    private WorkoutData dirtyWorkoutData;
    private WorkoutData approvedWorkoutData;
    private int numStepsWhenBatchBegan;
    private int numStepsAtPreviousRecord;

    private List<Long> usainBoltOcurredTimeStamps;

    WorkoutDataStoreImpl(){
        Logger.d(TAG, "Empty constructor called");
        dirtyWorkoutData = retrieveFromPersistentStorage(Constants.PREF_WORKOUT_DATA_DIRTY);
        approvedWorkoutData = retrieveFromPersistentStorage(Constants.PREF_WORKOUT_DATA_APPROVED);
        if (dirtyWorkoutData == null){
            init(DateUtil.getServerTimeInMillis());
        }
        if (dirtyWorkoutData.getCalories() == null){
            dirtyWorkoutData.setCalories(new Calorie(0,0));
            approvedWorkoutData.setCalories(new Calorie(0,0));
        }
        numStepsWhenBatchBegan = SharedPrefsManager.getInstance().getInt(Constants.PREF_WORKOUT_DATA_NUM_STEPS_WHEN_BATCH_BEGIN);
        numStepsAtPreviousRecord = SharedPrefsManager.getInstance().getInt(Constants.PREF_WORKOUT_DATA_NUM_STEPS_AT_PREVIOUS_RECORD);
        usainBoltOcurredTimeStamps = SharedPrefsManager.getInstance().getCollection(Constants.PREF_USAIN_BOLT_OCURRED_TIME_STAMPS,
                new TypeToken<ArrayList<Long>>(){}.getType());
    }

    WorkoutDataStoreImpl(long beginTimeStamp){
        init(beginTimeStamp);
    }

    public void init(long beginTimeStamp){
        String workoutId = UUID.randomUUID().toString();
        dirtyWorkoutData = new WorkoutDataImpl(beginTimeStamp, workoutId);
        approvedWorkoutData = new WorkoutDataImpl(beginTimeStamp, workoutId);
        usainBoltOcurredTimeStamps = new ArrayList<>();
        SharedPrefsManager.getInstance().setInt(Constants.PREF_NEXT_VOICE_UPDATE_SCHEDULD_AT_INDEX, 0);
        //Persist dirtyWorkoutData object over here
        persistBothWorkoutData();
    }

    @Override
    public float getTotalDistance(){
        return dirtyWorkoutData.getDistance();
    }

    @Override
    public Calorie getCalories() {
        return dirtyWorkoutData.getCalories();
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

            float averageStrideLength = (Utils.getAverageStrideLength() == 0)
                        ? (ClientConfig.getInstance().GLOBAL_AVERAGE_STRIDE_LENGTH) : Utils.getAverageStrideLength();

            // Normalising averageStrideLength obtained
            if (averageStrideLength < 0.25f){
                averageStrideLength = 0.25f;
            }
            if (averageStrideLength > 1f){
                averageStrideLength = 1f;
            }

            float extraPolatedDistance = stepsRanWhileSearchingForSource * averageStrideLength;

            dirtyWorkoutData.addDistance(extraPolatedDistance);
            Location startLocation = record.getLocation();
            if (dirtyWorkoutData.getStartPoint() == null){
                dirtyWorkoutData.setStartPoint(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()));
            }
            AnalyticsEvent.create(Event.ON_START_LOCATION_AFTER_RESUME)
                    .addBundle(getWorkoutBundle())
                    .put("start_location_latitude", startLocation.getLatitude())
                    .put("start_location_longitude", startLocation.getLongitude())
                    .put("extrapolated_distance", extraPolatedDistance)
                    .buildAndDispatch();
        }

        Logger.d(TAG, "addRecord: adding record to ApprovalQueue: " + record.toString());

        record.normaliseStepCountWrtStepCount(getTotalSteps(), numStepsAtPreviousRecord,
                getTotalDistance());

        dirtyWorkoutData.addRecord(record, true);
        setNumStepsAtPreviousRecord();
    }


    @Override
    public Properties getWorkoutBundle(){
        return WorkoutSingleton.getInstance().getWorkoutBundle();
    }

    @Override
    public float getAvgSpeed() {
        return dirtyWorkoutData.getAvgSpeed();
    }

    @Override
    public void addSteps(int numSteps){
        if (isWorkoutRunning()){
            dirtyWorkoutData.addSteps(numSteps);
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
    public void workoutPause(String reason) {
        Logger.d(TAG, "workoutPause");
        dirtyWorkoutData.workoutPause(reason);
        approvedWorkoutData.workoutPause(reason);
        // If it was a defaulter scenario then the queue has already been discarded
        approveWorkoutData();
    }

    @Override
    public void workoutResume() {
        Logger.d(TAG, "workoutResume");
        dirtyWorkoutData.workoutResume();
        approvedWorkoutData.workoutResume();
        numStepsWhenBatchBegan = getTotalSteps();
        setNumStepsAtPreviousRecord();
        persistBothWorkoutData();
    }

    private void setNumStepsAtPreviousRecord(){
        numStepsAtPreviousRecord = getTotalSteps();
        // Persist the number of steps
        SharedPrefsManager.getInstance().setInt(Constants.PREF_WORKOUT_DATA_NUM_STEPS_AT_PREVIOUS_RECORD,
                        numStepsAtPreviousRecord);
    }

    @Override
    public void incrementUsainBoltCounter() {
        // increment UsainBoltCount in both the workoutDataObjects
        dirtyWorkoutData.incrementUsainBoltCounter();
        approvedWorkoutData.incrementUsainBoltCounter();
        usainBoltOcurredTimeStamps.add(DateUtil.getServerTimeInMillis());
        persistBothWorkoutData();
    }

    @Override
    public boolean isWorkoutRunning() {
        return dirtyWorkoutData.isRunning();
    }

    @Override
    public synchronized void approveWorkoutData() {
        Logger.d(TAG, "approveWorkoutData");
        approveTheProgressSoFar();
        persistBothWorkoutData();
    }

    @Override
    public WorkoutData clear(){
        Logger.d(TAG, "clear: approving workoutData one last time because this is the end");
        approveTheProgressSoFar();
        clearPersistentStorage();
        return approvedWorkoutData.close();
    }

    private void approveTheProgressSoFar(){
        approvedWorkoutData = dirtyWorkoutData.copy();
    }

    @Override
    public synchronized void discardApprovalQueue() {
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
    public int getUsainBoltCount() {
        return dirtyWorkoutData.getUsainBoltCount();
    }

    @Override
    public boolean hasConsecutiveUsainBolts() {
        if (getUsainBoltCount() < 3){
            return false;
        }else {
            int count = getUsainBoltCount();
            if (usainBoltOcurredTimeStamps.size() != count){
                return false;
            }
            long latestTimeStamp = usainBoltOcurredTimeStamps.get(count - 1);
            long firstTimeStamp = usainBoltOcurredTimeStamps.get(count - 3);
            if (latestTimeStamp - firstTimeStamp <  CONSECUTIVE_USAIN_BOLT_WAIVER_TIME_INTERVAL){
                // If three consecutive usain bolts have ocurred within 20 mins then user must be in a vehicle
                return true;
            }
        }
        return false;
    }

    @Override
    public void setMockLocationDetected(boolean detected) {
        dirtyWorkoutData.setMockLocationDetected(detected);
        approvedWorkoutData.setMockLocationDetected(detected);
        persistBothWorkoutData();
    }

    @Override
    public boolean isMockLocationDetected() {
        return dirtyWorkoutData.isMockLocationDetected();
    }

    @Override
    public int getNumGpsSpikes() {
        return dirtyWorkoutData.getNumGpsSpikes();
    }

    @Override
    public void incrementGpsSpike() {
        dirtyWorkoutData.incrementGpsSpike();
        approvedWorkoutData.incrementGpsSpike();
    }

    @Override
    public int getNumUpdateEvents() {
        return dirtyWorkoutData.getNumUpdateEvents();
    }

    @Override
    public void incrementNumUpdateEvents() {
        dirtyWorkoutData.incrementNumUpdates();
        approvedWorkoutData.incrementNumUpdates();
    }

    @Override
    public int getNextVoiceUpdateScheduledAt() {
        int index = SharedPrefsManager.getInstance().getInt(Constants.PREF_NEXT_VOICE_UPDATE_SCHEDULD_AT_INDEX);
        return (int) ClientConfig.getInstance().getVoiceUpdateIntervalAtIndexInSecs(index);
    }

    @Override
    public void updateAfterVoiceUpdate() {
        Logger.d(TAG, "updateAfterVoiceUpdate");
        int currentIndex = SharedPrefsManager.getInstance().getInt(Constants.PREF_NEXT_VOICE_UPDATE_SCHEDULD_AT_INDEX);
        SharedPrefsManager.getInstance().setInt(Constants.PREF_NEXT_VOICE_UPDATE_SCHEDULD_AT_INDEX,
                currentIndex + 1);
    }

    @Override
    public int getCurrentBatchIndex() {
        return dirtyWorkoutData.getCurrentBatchIndex();
    }

    private void persistBothWorkoutData() {
        persistDirtyWorkoutData();
        if (approvedWorkoutData != null){
            SharedPrefsManager.getInstance().setObject(Constants.PREF_WORKOUT_DATA_APPROVED, approvedWorkoutData.copy());
        }
        SharedPrefsManager.getInstance().setInt(Constants.PREF_WORKOUT_DATA_NUM_STEPS_WHEN_BATCH_BEGIN, numStepsWhenBatchBegan);
        SharedPrefsManager.getInstance().setCollection(Constants.PREF_USAIN_BOLT_OCURRED_TIME_STAMPS, usainBoltOcurredTimeStamps);
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
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_USAIN_BOLT_OCURRED_TIME_STAMPS);
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_NEXT_VOICE_UPDATE_SCHEDULD_AT_INDEX);
    }
}
