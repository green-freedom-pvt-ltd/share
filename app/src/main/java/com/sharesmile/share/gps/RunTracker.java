package com.sharesmile.share.gps;

import android.location.Location;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.config.ClientConfig;
import com.sharesmile.share.core.config.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.WorkoutSingleton.State;
import com.sharesmile.share.gps.activityrecognition.ActivityDetector;
import com.sharesmile.share.gps.location.GoogleLocationTracker;
import com.sharesmile.share.gps.location.LocationDeserializer;
import com.sharesmile.share.gps.location.LocationSerializer;
import com.sharesmile.share.gps.models.Calorie;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.utils.CircularQueue;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import java.util.concurrent.ScheduledExecutorService;

import static com.sharesmile.share.core.config.Config.TRACKER_RECORD_HISTORY_QUEUE_MAX_SIZE;

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
            recordHistoryQueue = new CircularQueue<>(TRACKER_RECORD_HISTORY_QUEUE_MAX_SIZE);
            if (isActive()){
                dataStore = WorkoutSingleton.getInstance().getDataStore();
                DistRecord prevRecord = retrievePrevDistRecord();
                if (prevRecord != null){
                    recordHistoryQueue.add(prevRecord);
                }
            }else{
                // User started workout
                SharedPrefsManager.getInstance().removeKey(Constants.PREF_PREV_DIST_RECORD_AS_STRING);
                dataStore = WorkoutSingleton.getInstance().beginWorkout();
                resumeRun();
            }
        }
    }

    public synchronized void endRun(){
        Logger.d(TAG, "endRun");
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_PREV_DIST_RECORD_AS_STRING);
        listener = null;
    }



    @Override
    public State getState() {
        return WorkoutSingleton.getInstance().getState();
    }

    @Override
    public void incrementGpsSpike() {
        if (isRunning() && dataStore != null){
            dataStore.incrementGpsSpike();
        }
    }

    @Override
    public void incrementNumUpdateEvents() {
        if (isRunning() && dataStore != null){
            dataStore.incrementNumUpdateEvents();
        }
    }

    @Override
    public synchronized void pauseRun(String reason) {
        Logger.d(TAG, "pauseRun");
        WorkoutSingleton.getInstance().pauseWorkout(reason);
    }

    /**
     * Called when Workout is resumed, also called when workout begins
     */
    @Override
    public synchronized void resumeRun() {
        Logger.d(TAG, "resumeRun");
        WorkoutSingleton.getInstance().resumeWorkout();
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
        long timeTaken = 0; // in millis
        if (!recordHistoryQueue.isEmpty()){
            if (recordHistoryQueue.getCurrentSize() == 1){
                // Only one record is present
                if (!getLastRecord().isFirstRecordAfterResume() && !getLastRecord().isTooOld()){
                    // If a record is older than 12 secs then it is considered as too old
                    return getLastRecord().getSpeed();
                }
            }else {
                // More than one record present in the list
                synchronized (recordHistoryQueue){
                    for (int i=recordHistoryQueue.getCurrentSize()-1; i >= 0; i--){
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
    public Calorie getCalories() {
        if (isActive() && dataStore != null){
            return dataStore.getCalories();
        }
        return null;
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
                    try {
                        processLocation(point);
                    }catch (Exception e){
                        Logger.d(TAG, "Exception in processLocation: " + e.getMessage());
                        e.printStackTrace();
                    }
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
        double caloriesBeforeDiscard = dataStore.getCalories().getCalories();

        dataStore.discardApprovalQueue();

        float deltaDistance = dataStore.getTotalDistance() - distBeforeDiscard; // in meters, should be negative
        float deltaTime = dataStore.getRecordedTime() - recordedTimeBeforeDiscard; // in secs
        float deltaSpeed = (deltaDistance / deltaTime) * 3.6f; // in km/hrs
        double deltaCalories = dataStore.getCalories().getCalories() - caloriesBeforeDiscard;
        listener.updateWorkoutRecord(dataStore.getTotalDistance(), dataStore.getAvgSpeed(),
                deltaDistance, Math.round(deltaTime), deltaSpeed, deltaCalories);
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
        if (!isRunning()){
            // Nothing to do
            return;
        }
        //Check if the start point has been detected since the workout started/resumed
        if (dataStore.coldStartAfterResume()){
            // This is a potential source location
            Logger.i(TAG, "Checking for source, accuracy = " + point.getAccuracy());
            if (point.getAccuracy() < ClientConfig.getInstance().SOURCE_ACCEPTABLE_ACCURACY){
                // Set has source only when it has acceptable accuracy
                Logger.i(TAG, "Source Location with good accuracy fetched:\n " + point.toString());
                DistRecord startRecord = new DistRecord(point);
                dataStore.addRecord(startRecord);
                synchronized (recordHistoryQueue){
                    recordHistoryQueue.add(startRecord);
                }
                persistPrevDistRecord(startRecord);
            }
        }else{
            Logger.d(TAG,"Processing Location: " + point.toString());
            Location prevLocation = getLastRecord().getLocation();
            long prevTimeStamp = getLastRecord().getTimeStamp();
            float interval = ((float) (DateUtil.getServerTimeInMillis() - prevTimeStamp)) / 1000;
            float dist = prevLocation.distanceTo(point);

            // Step 1: Check whether threshold interval for recording has elapsed
            if (interval > Config.THRESHOLD_INTEVAL){

                boolean toRecord = false;
                    /*
                    Step 2: Secondary check for spike
                     */
                float deltaSpeedMs = dist / interval;

                float spikeFilterSpeedThreshold;

                // recent is avg GPS speed from recent few samples of accepted GPS points in the past 24 secs
                // GPS speed is obtained directly from location object and is calculated using doppler shift
                float recentGpsSpeed = GoogleLocationTracker.getInstance().getRecentGpsSpeed();

                // If recentGpsSpeed is above USAIN_BOLT_GPS_SPEED_LIMIT (21 km/hr) then user must be in a vehicle
                if (ActivityDetector.getInstance().isIsInVehicle()
                        || recentGpsSpeed > ClientConfig.getInstance().USAIN_BOLT_GPS_SPEED_LIMIT){
                    spikeFilterSpeedThreshold = ClientConfig.getInstance().SPIKE_FILTER_SPEED_THRESHOLD_IN_VEHICLE;
                }else {
                    if ( ActivityDetector.getInstance().isOnFoot() ||
                            (listener.isCountingSteps() &&
                                    listener.getMovingAverageOfStepsPerSec() >= ClientConfig.getInstance().MIN_CADENCE_FOR_WALK)){
                        // Can make a safe assumption that the person is on foot
                        spikeFilterSpeedThreshold = ClientConfig.getInstance().SPIKE_FILTER_SPEED_THRESHOLD_ON_FOOT;
                    }else {
                        spikeFilterSpeedThreshold = ClientConfig.getInstance().SECONDARY_SPIKE_FILTER_SPEED_THRESHOLD_DEFAULT;
                    }
                }

                if (deltaSpeedMs > spikeFilterSpeedThreshold){
                    // Insanely high velocity, must be a GPS spike
                    toRecord = false;
                    Logger.i(TAG, "GPS spike detected in RunTracker, through secondary check");
                    dataStore.incrementGpsSpike();
                }else {
                        /*
                         Step 3: Record if point is accurate, i.e. accuracy better/lower than our threshold
                                 Else
                                 Apply formula to check whether to record the point or not
                          */
                    float accuracy = point.getAccuracy();
                    if (accuracy < ClientConfig.getInstance().THRESHOLD_ACCURACY){
                        Logger.d(TAG, "Accuracy Wins");
                        toRecord = true;
                    }else{
                        toRecord = checkUsingFormula(dist, point.getAccuracy());
                    }
                }

                // Step 4: Record if needed, else wait for next location
                if (toRecord){
                    if (dist > ClientConfig.getInstance().DIST_INC_IN_SINGLE_GPS_UPDATE_UPPER_LIMIT){
                        // If it is making too big a jump in distance then we will not allow, even if the speed is in limit
                        dist = 0;
                    }
                    DistRecord record = new DistRecord(point, prevLocation, prevTimeStamp,  dist);
                    Logger.d(TAG, "Distance Recording: " + record.toString());
                    double prevCalories = dataStore.getCalories().getCalories();
                    dataStore.addRecord(record);
                    synchronized (recordHistoryQueue){
                        recordHistoryQueue.add(record);
                    }
                    persistPrevDistRecord(record);
                    // Change delta_distance
                    float deltaDistance = record.getDist(); // in meters
                    int deltaTime = Math.round( record.getInterval() / 1000f ); // in secs
                    float deltaSpeed = record.getSpeed() * 3.6f; // in km/hrs
                    double deltaCalories = dataStore.getCalories().getCalories() - prevCalories;
                    listener.updateWorkoutRecord(dataStore.getTotalDistance(), dataStore.getAvgSpeed(),
                            deltaDistance, deltaTime, deltaSpeed, deltaCalories);
                }
            }
        }
    }

    public DistRecord retrievePrevDistRecord(){
        String prevRecordAsString =
                SharedPrefsManager.getInstance().getString(Constants.PREF_PREV_DIST_RECORD_AS_STRING);
        if (!TextUtils.isEmpty(prevRecordAsString)){
            DistRecord prevRecord = getGsonInstanceForLocation().fromJson(prevRecordAsString, DistRecord.class);
            return prevRecord;
        }
        return null;
    }

    public void persistPrevDistRecord(DistRecord prevDistRecord){
        String recordAsString = getGsonInstanceForLocation().toJson(prevDistRecord);
        SharedPrefsManager.getInstance().setString(Constants.PREF_PREV_DIST_RECORD_AS_STRING,
                recordAsString);
    }

    private Gson getGsonInstanceForLocation(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Location.class, new LocationDeserializer());
        gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer());
        return gsonBuilder.create();
    }

    public Properties getWorkoutBundle(){
        return WorkoutSingleton.getInstance().getWorkoutBundle();
    }

    private boolean checkUsingFormula(float dist, float accuracy){
        float deltaAccuracy = accuracy -
                (ClientConfig.getInstance().THRESHOLD_ACCURACY - Config.THRESHOLD_ACCURACY_OFFSET);
        float value = (dist / deltaAccuracy);
        Logger.d(TAG, "Applying formula, dist = " + dist + " accuracy = " + accuracy + " value = " + value);
        if ( value > ClientConfig.getInstance().THRESHOLD_FACTOR){
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

    interface UpdateListner {

        void updateWorkoutRecord(float totalDistance, float avgSpeed,
                                 float deltaDistance, int deltaTime,
                                 float deltaSpeed, double deltaCalories);

        void updateStepsRecord(long timeStampMillis);

        float getMovingAverageOfStepsPerSec();

        boolean isCountingSteps();

    }

}
