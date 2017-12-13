package com.sharesmile.share.googleapis;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.WorkoutDataStore;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

/**
 * Created by ankitmaheshwari on 12/1/17.
 */

public class GoogleFitnessSessionRecorder implements  GoogleApiHelper.Listener, GoogleTracker{

    private static final String TAG = "GoogleFitnessSessionRecorder";

    List<String> sessionIds;

    public GoogleFitnessSessionRecorder(Context context) {
        this.sessionIds = SharedPrefsManager.getInstance().getCollection(Constants.PREF_FITNESS_SESSION_IDS,
                new TypeToken<ArrayList<String>>(){}.getType());
        if (sessionIds == null){
            sessionIds = new ArrayList<>();
        }
    }

    @Override
    public void onConnected() {
        Logger.d(TAG, "onConnected");
        subscribeToFitnessData();
        // Call resume to start the first session
        resume();
    }

    @Override
    public void connectionFailed() {
        Logger.d(TAG, "User Denied permission to access Google Fit Data");
        MainApplication.showToast(R.string.google_fit_permission_rationale);
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        GoogleApiHelper.getInstance().register(this);
    }

    private void subscribeToFitnessData(){

        subscribeToDataType(DataType.TYPE_STEP_COUNT_DELTA);
        subscribeToDataType(DataType.TYPE_DISTANCE_DELTA);
        subscribeToDataType(DataType.TYPE_CALORIES_EXPENDED);

    }

    private void subscribeToDataType(final DataType dataType){
        Fitness.RecordingApi.subscribe(GoogleApiHelper.getInstance().getGoogleApiClient(), dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Logger.d(TAG, "Existing subscription for "+dataType.getName()+" detected.");
                            } else {
                                Logger.d(TAG, "Successfully subscribed to " + dataType.getName());
                            }
                        } else {
                            Logger.d(TAG, "There was a problem subscribing to " + dataType.getName());
                        }
                    }
                });
    }

    volatile int cancelCount = 0;
    private void cancelSubscriptions(){
        cancelCount = 0;
        unsubscribeDataType(DataType.TYPE_STEP_COUNT_DELTA);
        unsubscribeDataType(DataType.TYPE_DISTANCE_DELTA);
        unsubscribeDataType(DataType.TYPE_CALORIES_EXPENDED);

    }

    private void unsubscribeDataType(final DataType dataType){
        Fitness.RecordingApi.unsubscribe(GoogleApiHelper.getInstance().getGoogleApiClient(), dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Logger.d(TAG, "Successfully unsubscribed to: " + dataType.getName());
                        } else {
                            Logger.d(TAG, "There was a problem in unsubscribing " + dataType.getName());
                        }
                        updateCancelCount();
                    }
                });
    }

    private synchronized void updateCancelCount(){
        cancelCount++;
        if (cancelCount == 3){
            // All datatypes have been unsubscribed, we shall unregister from GoogleApiHelper
            GoogleApiHelper.getInstance().unregister(this);
            cancelCount = 0;
        }
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        if (!GoogleApiHelper.getInstance().isConnected()){
            return;
        }
        pause();
        cancelSubscriptions();
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
        /*

        final GoogleApiHelper helper = GoogleApiHelper.getInstance();
        if (!helper.isConnected()){
            return;
        }

        final String currentSessionId = getLatestSessionId();
        PendingResult<SessionStopResult> pendingResult =
                Fitness.SessionsApi.stopSession(helper.getGoogleApiClient(), currentSessionId);

        pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
            @Override
            public void onResult(SessionStopResult sessionStopResult) {
                if( sessionStopResult.getStatus().isSuccess() ) {
                    Logger.d(TAG, "Successfully stopped session " + currentSessionId);
                } else {
                    Logger.d(TAG, "Failed to stop session: " + currentSessionId);
                }
            }
        });

         */
    }

    public String getLatestSessionId(){
        return sessionIds == null ? null : sessionIds.get(sessionIds.size() - 1);
    }

    public static String getSessionIdentifier(String workoutId, int index){
        return "session_" + workoutId + "_" + index;
    }

    public static String getSessionName(String workoutId){
        return "session_" + workoutId;
    }

    public void readWorkoutHistory(){
        Logger.d(TAG, "readWorkoutHistory");
        if (!GoogleApiHelper.getInstance().isConnected()){
            return;
        }
        WorkoutDataStore dataStore = WorkoutSingleton.getInstance().getDataStore();

        // TODO: Need to segregate the read for batches

        final Map<String, Float> aggregateMap = new HashMap<>();

        Logger.d(TAG, "Reading step count data between " + dataStore.getBeginTimeStamp() + " and "
                + DateUtil.getServerTimeInMillis() + " for workoutId " + dataStore.getWorkoutId());

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS,    DataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.MINUTES)
                .setTimeRange(dataStore.getBeginTimeStamp(), System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();


        PendingResult<DataReadResult> result =
                Fitness.HistoryApi.readData(GoogleApiHelper.getInstance().getGoogleApiClient(),
                        readRequest);

        result.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                DateFormat dateFormat = getTimeInstance();
                if (dataReadResult.getBuckets().size() > 0) {
                    Logger.d(TAG, "Buckets in result, number of buckets = "
                            + dataReadResult.getBuckets().size());
                    aggregateMap.put(DISTANCE, 0f);
                    aggregateMap.put(STEPS, 0f);
                    aggregateMap.put(CALORIES, 0f);

                    int count = 1;
                    for (Bucket bucket : dataReadResult.getBuckets()) {
                        Logger.d(TAG, "Bucket number " + count + " ::::: number of datasets = "
                                + bucket.getDataSets().size());
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            Logger.d(TAG, "\ndataSet.dataType: " + dataSet.getDataType().getName());
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                describeDataPoint(dp, dateFormat);
                            }
                            updateMapWithDataSet(dataSet, aggregateMap);
                        }
                        count++;
                    }

                    String update = "DISTANCE : " + (aggregateMap.get(DISTANCE) / 1000)
                            + ", STEPS : " + aggregateMap.get(STEPS)
                            + ", CALORIES : " + aggregateMap.get(CALORIES);

                    Logger.d(TAG, update);
                    MainApplication.showToast(update);

                } else if (dataReadResult.getDataSets().size() > 0) {
                    Logger.d(TAG, "Datasets in result : dataSet.size(): " + dataReadResult.getDataSets().size());
                    for (DataSet dataSet : dataReadResult.getDataSets()) {
                        Logger.d(TAG, "\ndataType: " + dataSet.getDataType().getName());
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            describeDataPoint(dp, dateFormat);
                        }
                    }
                }
            }
        });
    }

    private void updateMapWithDataSet(DataSet dataSet, Map<String, Float> map){
        DataType dataType = dataSet.getDataType();
        String key = getKeyFor(dataType);
        if (key != null){
            float currValue = map.get(key);
            for (DataPoint dp : dataSet.getDataPoints()) {
                for(Field field : dataType.getFields()) {
                    if (field.equals(GoogleApiHelper.getInstance().getFieldFor(dataType))){
                        if (field.equals(Field.FIELD_STEPS)){
                            currValue = currValue +  dp.getValue(field).asInt();
                        }else {
                            currValue = currValue +  dp.getValue(field).asFloat();
                        }
                    }
                }
            }
            map.put(key, currValue);
        }
    }



    private String getKeyFor(DataType dataType){
        if (DataType.TYPE_DISTANCE_DELTA.equals(dataType)){
            return DISTANCE;
        }else if (DataType.TYPE_STEP_COUNT_DELTA.equals(dataType)){
            return STEPS;
        }else if (DataType.TYPE_CALORIES_EXPENDED.equals(dataType)){
            return CALORIES;
        }
        return null;
    }

    public static final String DISTANCE = "distance";
    public static final String STEPS = "steps";
    public static final String CALORIES = "calories";


    public void describeDataPoint(DataPoint dp, DateFormat dateFormat) {
        String msg = "dataPoint: "
                + "type: " + dp.getDataType().getName() +"\n"
                + ", range: [" + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + "-" + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + "]\n"
                + ", fields: [";

        for(Field field : dp.getDataType().getFields()) {
            msg += field.getName() + "=" + dp.getValue(field) + " ";
        }

        msg += "]";
        Logger.d(TAG, msg);
    }

    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Logger.d(TAG, "Data returned for Session: " + session.getName()
                + "\tDescription: " + session.getDescription()
                + "\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }


    private void dumpDataSet(DataSet dataSet) {
        Logger.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            DateFormat dateFormat = getTimeInstance();
            Logger.d(TAG, "Data point:");
            Logger.d(TAG, "\tType: " + dp.getDataType().getName());
            Logger.d(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Logger.d(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Logger.d(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    @Override
    public void resume() {
        Logger.d(TAG, "resume");

        /*

        WorkoutDataStore dataStore = WorkoutSingleton
                .getInstance().getDataStore();
        if (dataStore == null){
            // Do Nothing, silently return
            return;
        }
        int batchIndex = dataStore.getCurrentBatchIndex();
        final String currentSessionIdentifier = getSessionIdentifier(dataStore.getWorkoutId(), batchIndex);
        final String currentSessionName = getSessionName(dataStore.getWorkoutId());

        if (!sessionIds.isEmpty() && currentSessionIdentifier.equals(getLatestSessionId())){
            // Session is already created for ongoing batch, WorkoutService must've started after restart
            // Don't create any new sessions
            return;
        }

        // TODO: Make sure that this resume is called after batch is created

        // 1. Create a session object
        // (provide a name, identifier, description and start time)

        Session session = new Session.Builder()
                .setName(currentSessionName)
                .setIdentifier(currentSessionIdentifier)
                .setDescription("Running session on " + DateUtil.getDate().toString())
                .setStartTime(System.currentTimeMillis() - 6000, TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.RUNNING_JOGGING)
                .build();


        // 2. Invoke the Sessions API with:
        // - The Google API client object
        // - The request object
        PendingResult<Status> pendingResult =
                Fitness.SessionsApi.startSession(GoogleApiHelper.getInstance().getGoogleApiClient(), session);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()){
                    Logger.d(TAG, "Successfully started session " + currentSessionIdentifier);
                    sessionIds.add(currentSessionIdentifier);
                    SharedPrefsManager.getInstance().setCollection(Constants.PREF_FITNESS_SESSION_IDS,
                            sessionIds);
                }else {
                    Logger.d(TAG, "Problem while starting session: " + status.getStatusMessage());
                }
            }
        });

         */

    }
}
