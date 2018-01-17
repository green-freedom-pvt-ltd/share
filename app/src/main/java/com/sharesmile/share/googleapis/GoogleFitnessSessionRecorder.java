package com.sharesmile.share.googleapis;

import android.content.Context;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
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
import com.sharesmile.share.gps.models.WorkoutData;
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

public class GoogleFitnessSessionRecorder implements  GoogleApiHelper.Listener, GoogleRecorder{

    private static final String TAG = "GoogleFitnessSessionRecorder";

    List<Batch> batches;

    public GoogleFitnessSessionRecorder(Context context) {
        this.batches = SharedPrefsManager.getInstance().getCollection(Constants.PREF_FITNESS_BATCHES,
                new TypeToken<ArrayList<Batch>>(){}.getType());
    }

    @Override
    public void onConnected() {
        Logger.d(TAG, "onConnected");
        subscribeToFitnessData();
    }

    @Override
    public void connectionFailed() {
        Logger.d(TAG, "User Denied permission to access Google Fit Data");
        MainApplication.showToast(R.string.google_fit_permission_rationale);
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        if (batches == null){
            // Fresh workout and very first batch
            batches = new ArrayList<>();
            Batch batch = new Batch(System.currentTimeMillis());
            batches.add(batch);
            SharedPrefsManager.getInstance().setCollection(Constants.PREF_FITNESS_BATCHES, batches);
        }
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

    private void cancelSubscriptions(){

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

    volatile int cancelCount = 0;
    private synchronized void updateCancelCount(){
        Logger.d(TAG, "updateCancelCount: cancelCount = " + cancelCount);
        cancelCount++;
        if (cancelCount == 3){
            // All datatypes have been unsubscribed, we shall unregister from GoogleApiHelper
            GoogleApiHelper.getInstance().unregister(this);
            cancelCount = 0;
        }
    }

    @Override
    public void readAndStop(WorkoutData result) {
        Logger.d(TAG, "readAndStop");
        int numBatches = batches.size();
        Batch lastBatch = batches.get(numBatches - 1);
        lastBatch.setEndTs(System.currentTimeMillis());
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_FITNESS_BATCHES);

        if (!GoogleApiHelper.getInstance().isConnected()){
            return;
        }

        // Synchronous call to update WorkoutData result object
        readWorkoutHistory(result);

        cancelCount = 0;
        cancelSubscriptions();
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");

        int numBatches = batches.size();
        Batch lastBatch = batches.get(numBatches - 1);
        lastBatch.setEndTs(System.currentTimeMillis());
        SharedPrefsManager.getInstance().setCollection(Constants.PREF_FITNESS_BATCHES, batches);

    }

    public void readWorkoutHistory(WorkoutData resultToBeUpdated){
        Logger.d(TAG, "readWorkoutHistory");
        if (!GoogleApiHelper.getInstance().isConnected()){
            return;
        }

        for (Batch batch : batches){
            readBatchSynchronously(batch, resultToBeUpdated);
        }

        String update = "DISTANCE : " + (resultToBeUpdated.getEstimatedDistance() / 1000)
                + ", STEPS : " + resultToBeUpdated.getEstimatedSteps()
                + ", CALORIES : " + resultToBeUpdated.getEstimatedCalories();
        Logger.d(TAG, update);

    }

    private void readBatchSynchronously(Batch batch, final WorkoutData result){
        Logger.d(TAG, "Reading step count data between " + batch.getStartTs() + " and "
                + batch.getEndTs());

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
                .bucketByTime(5, TimeUnit.MINUTES)
                .setTimeRange(batch.getStartTs(), batch.getEndTs(), TimeUnit.MILLISECONDS)
                .build();


        PendingResult<DataReadResult> pendingResult =
                Fitness.HistoryApi.readData(GoogleApiHelper.getInstance().getGoogleApiClient(),
                        readRequest);

        try {
            Logger.d(TAG, "readBatchSynchronously: time before read = " + System.currentTimeMillis());
            DataReadResult dataReadResult = pendingResult.await(10, TimeUnit.SECONDS);
            Logger.d(TAG, "readBatchSynchronously: time after read = " + System.currentTimeMillis());
            handleDataReadResult(dataReadResult, result);
        }catch (Throwable e){
            String message = "Exception while reading batch for workoutId " + result.getWorkoutId()
                    + " Exception: " + e.getMessage();
            Crashlytics.log(message);
            Crashlytics.logException(e);
            Logger.e(TAG, message);
            e.printStackTrace();
        }

    }

    private void handleDataReadResult(DataReadResult dataReadResult, WorkoutData result){
        Logger.d(TAG, "handleDataReadResult");
        if (dataReadResult == null){
            return;
        }
        if (!dataReadResult.getStatus().isSuccess()){
            Logger.e(TAG, "Data read was not successful for workoutId " + result.getWorkoutId()
                    + ", statusMessage = " + dataReadResult.getStatus().getStatusMessage()
                    + ", status" + dataReadResult.getStatus().getStatus());
        }
        DateFormat dateFormat = getTimeInstance();
        if (dataReadResult.getBuckets().size() > 0) {
            Map<String, Float> aggregateMap = new HashMap<>();
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

            // Update result object
            result.addEstimatedSteps(aggregateMap.get(STEPS).intValue());
            result.addEstimatedDistance(aggregateMap.get(DISTANCE));
            result.addEstimatedCalories(aggregateMap.get(CALORIES));

        } else if (dataReadResult.getDataSets().size() > 0) {
            // This piece of code is just for logging purpose
            Logger.d(TAG, "Datasets in result : dataSet.size(): " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                Logger.d(TAG, "\ndataType: " + dataSet.getDataType().getName());
                for (DataPoint dp : dataSet.getDataPoints()) {
                    describeDataPoint(dp, dateFormat);
                }
            }
        }
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
                + ", range: [" + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS))
                + "-" + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + "]\n"
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

        // Create new batch and persist it
        Batch freshBatch = new Batch(System.currentTimeMillis());
        batches.add(freshBatch);
        SharedPrefsManager.getInstance().setCollection(Constants.PREF_FITNESS_BATCHES, batches);

    }


    private static class Batch {
        long startTs;
        long endTs;

        public Batch(long startTs) {
            this.startTs = startTs;
        }

        public long getStartTs() {
            return startTs;
        }

        public void setStartTs(long startTs) {
            this.startTs = startTs;
        }

        public long getEndTs() {
            return endTs;
        }

        public void setEndTs(long endTs) {
            this.endTs = endTs;
        }
    }
}
