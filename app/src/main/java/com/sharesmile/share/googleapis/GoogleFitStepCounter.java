package com.sharesmile.share.googleapis;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.sharesmile.share.gps.StepCounter;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ankitm on 09/05/16.
 */
public class GoogleFitStepCounter implements StepCounter, OnDataPointListener, GoogleApiHelper.Listener{

    private static final String TAG = "GoogleFitStepCounter";

    public static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";

    private Listener listener;
    boolean isPaused;
    GoogleApiHelper helper;

    long countingbeganTsMillis;

    private LinkedHashMap historyQueue = new LinkedHashMap<Long, Long>()
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return this.size() > NUM_ELEMS_IN_HISTORY_QUEUE + 1;
        }
    };


    public GoogleFitStepCounter(Context context, Listener listener) {
        this.listener = listener;
        this.helper = new GoogleApiHelper(GoogleApiHelper.API_LIVE_STEP_COUNTING, context);
        start();
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        synchronized (historyQueue){
            historyQueue.clear();
        }
        helper.setListener(this);
        helper.connect();
        countingbeganTsMillis = DateUtil.getServerTimeInMillis();
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        if (helper.isConnected()){
            Fitness.SensorsApi.remove( helper.getGoogleApiClient(), this )
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess() && helper.isConnected()) {
                                helper.disconnect();
                            }
                        }
                    });
            synchronized (historyQueue){
                historyQueue.clear();
            }
        }
    }

    @Override
    public void pause() {
        isPaused = true;
        synchronized (historyQueue){
            historyQueue.clear();
        }
    }

    @Override
    public void resume() {
        isPaused = false;
        synchronized (historyQueue){
            historyQueue.clear();
        }
        countingbeganTsMillis = DateUtil.getServerTimeInMillis();
    }

    @Override
    public float getMovingAverageOfStepsPerSec() {
        if (historyQueue.isEmpty() || historyQueue.size() == 1){
            return -1;
        }else {
            synchronized (historyQueue){
                Iterator iterator = historyQueue.entrySet().iterator();
                Map.Entry<Long, Long> first = null;
                Map.Entry<Long, Long> last = null;
                Long numSteps = 0L;
                while (iterator.hasNext()){
                    Map.Entry<Long, Long> thisEntry = (Map.Entry<Long, Long>) iterator.next();

                    if ( ((DateUtil.getServerTimeInMillis() / 1000) - thisEntry.getKey())
                            > STEP_COUNT_READING_VALID_INTERVAL){
                        // This entry is too old to be considered in calculation
                        continue;
                    }

                    numSteps += thisEntry.getValue();
                    if (first == null){
                        first = thisEntry;
                        last = thisEntry;
                    }else {
                        if (thisEntry.getKey() < first.getKey()){
                            first = thisEntry;
                        }
                        if (thisEntry.getKey() > last.getKey()){
                            last = thisEntry;
                        }
                    }
                }

                if (first == null){
                    // No entry picked for calculation
                    return 0;
                }

                Long numStepsInFirst = first.getValue();
                numSteps = numSteps - numStepsInFirst;
                //In a rare scenario when queue has just two entries with same keys (i.e. epoch in secs) we are considering delta as 1
                Long deltaTime = (last.getKey() - first.getKey()) > 0
                        ? last.getKey() - first.getKey() : 1;
                return  numSteps.floatValue()  / deltaTime;
            }
        }
    }

    private void registerFitnessDataListener(final DataSource dataSource, final DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate( 1, TimeUnit.SECONDS )
                .build();

        Fitness.SensorsApi.add(helper.getGoogleApiClient(), request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Logger.i(TAG, "SensorApi successfully added for " + dataType.getName());
                            listener.stepCounterReady();
                        }else{
                            Logger.e(TAG, "SensorApi couldn't be added for " + dataType.getName());
                            listener.notAvailable(SENSOR_API_NOT_ADDED);
                        }
                    }
                });
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        Logger.d(TAG, "onDataPoint");
        if (!isPaused){
            for( Field field : dataPoint.getDataType().getFields() ) {
                Value value = dataPoint.getValue( field );
                long startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                if (startTime < countingbeganTsMillis){
                    // This stepcount reading's interval started before the beginning of step counting, will ignore
                    Logger.d(TAG, "Older step count reading, will ignore");
                } else {
                    String message = "Field: " + field.getName() + " Value: " + value;
                    Logger.d(TAG, message);
                    if (Field.FIELD_STEPS.getName().equals(field.getName())){
                        // Step count data
                        Long deltaSteps = Long.parseLong(value.toString());
                        synchronized (historyQueue){
                            historyQueue.put(dataPoint.getEndTime(TimeUnit.SECONDS), deltaSteps);
                        }
                        listener.onStepCount(deltaSteps.intValue());
                    }
                }
            }
        }
    }

    @Override
    public void onConnected() {
        Logger.d(TAG, "onConnected");

        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_DISTANCE_DELTA)
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                Log.i(TAG, "onResult of dataSourcesResultCallback, Status: " + dataSourcesResult.getStatus().toString());
                for( DataSource dataSource : dataSourcesResult.getDataSources () ) {
                    Logger.d(TAG, "onResult of dataSourcesResultCallback, dataSource found: "
                            + dataSource.toDebugString() + ", type: " + dataSource.getDataType().getName());
                    if( DataType.TYPE_STEP_COUNT_DELTA.equals(dataSource.getDataType()) ) {
                        Logger.d(TAG, "onResult of dataSourcesResultCallback, will register FitnessDataListener");
                        registerFitnessDataListener(dataSource, dataSource.getDataType());
                    }
                    if( DataType.TYPE_DISTANCE_DELTA.equals(dataSource.getDataType()) ) {
                        Logger.d(TAG, "onResult of dataSourcesResultCallback, will register FitnessDataListener");
                        registerFitnessDataListener(dataSource, dataSource.getDataType());
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(helper.getGoogleApiClient(), dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    @Override
    public void connectionFailed() {
        Logger.d(TAG, "User Denied permission to access Google Fit Data");
        if (listener != null){
            listener.notAvailable(PERMISSION_NOT_GRANTED_BY_USER);
        }
    }
}
