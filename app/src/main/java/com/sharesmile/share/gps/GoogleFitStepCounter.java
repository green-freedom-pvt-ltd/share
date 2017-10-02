package com.sharesmile.share.gps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
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
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ankitm on 09/05/16.
 */
public class GoogleFitStepCounter implements StepCounter,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnDataPointListener{

    private static final String TAG = "GoogleFitStepCounter";

    public static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";

    private Context context;
    private Listener listener;
    private GoogleApiClient mApiClient;
    boolean isPaused;

    long countingbeganTsMillis;

    private LinkedHashMap historyQueue = new LinkedHashMap<Long, Long>()
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return this.size() > NUM_ELEMS_IN_HISTORY_QUEUE + 1;
        }
    };


    public GoogleFitStepCounter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        startCounting();
    }

    @Override
    public void startCounting() {
        Logger.d(TAG, "startCounting");
        synchronized (historyQueue){
            historyQueue.clear();
        }
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
        countingbeganTsMillis = DateUtil.getServerTimeInMillis();
    }

    @Override
    public void stopCounting() {
        Logger.d(TAG, "stopCounting");
        if (mApiClient.isConnected()){
            Fitness.SensorsApi.remove( mApiClient, this )
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess() && mApiClient.isConnected()) {
                                mApiClient.disconnect();
                            }
                        }
                    });
            synchronized (historyQueue){
                historyQueue.clear();
            }
        }
    }

    @Override
    public void pauseCounting() {
        isPaused = true;
        synchronized (historyQueue){
            historyQueue.clear();
        }
    }

    @Override
    public void resumeCounting() {
        isPaused = false;
        synchronized (historyQueue){
            historyQueue.clear();
        }
        countingbeganTsMillis = DateUtil.getServerTimeInMillis();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == Activity.RESULT_OK) {
            if( !mApiClient.isConnecting() && !mApiClient.isConnected() ) {
                mApiClient.connect();
            }
            return;
        } else if( resultCode == Activity.RESULT_CANCELED ) {
            Logger.e(TAG, "RESULT_CANCELED");
        }
        listener.notAvailable(PERMISSION_NOT_GRANTED_BY_USER);
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

    @Override
    public void onConnected(Bundle bundle) {
        Logger.d(TAG, "onConnected");
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_DELTA)
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                Log.i(TAG, "onResult of dataSourcesResultCallback, Status: " + dataSourcesResult.getStatus().toString());
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    Logger.d(TAG, "onResult of dataSourcesResultCallback, dataSource found: "
                            + dataSource.toDebugString() + ", type: " + dataSource.getDataType().getName());
                    if( DataType.TYPE_STEP_COUNT_DELTA.equals(dataSource.getDataType()) ) {
                        Logger.d(TAG, "onResult of dataSourcesResultCallback, will register FitnessDataListener");
                        registerFitnessDataListener(dataSource, dataSource.getDataType());
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    private void registerFitnessDataListener(final DataSource dataSource, final DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate( 1, TimeUnit.SECONDS )
                .build();

        Fitness.SensorsApi.add(mApiClient, request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Logger.i(TAG, "SensorApi successfully added for " + dataType.getName());
                            listener.isReady();
                        }else{
                            Logger.e(TAG, "SensorApi couldn't be added for " + dataType.getName());
                            listener.notAvailable(SENSOR_API_NOT_ADDED);
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.e(TAG, "onConnectionFailed, hasResolution = " + connectionResult.hasResolution());
        if (connectionResult.hasResolution()){
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.WORKOUT_SERVICE_BROADCAST_CATEGORY,
                    Constants.BROADCAST_GOOGLE_FIT_READ_PERMISSION);
            Intent intent = new Intent(Constants.WORKOUT_SERVICE_BROADCAST_ACTION);
            bundle.putParcelable(Constants.KEY_GOOGLE_FIT_RESOLUTION_PARCELABLE,
                    connectionResult);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
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
}
