package com.sharesmile.share.googleapis;

import android.content.Context;

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
import com.sharesmile.share.core.ClientConfig;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by ankitmaheshwari on 12/12/17.
 */

public class GoogleFitSensorTracker implements GoogleTracker, GoogleApiHelper.Listener,
        OnDataPointListener {

    private static final String TAG = "GoogleFitSensorTracker";

    DataType dataType;
    Listener listener;

    boolean isPaused;
    long countingbeganTsMillis;

    public GoogleFitSensorTracker(Context context, DataType dataType, Listener listener) {
        this.dataType = dataType;
        this.listener = listener;
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        GoogleApiHelper.getInstance().register(this);
        countingbeganTsMillis = DateUtil.getServerTimeInMillis();
    }

    @Override
    public void stop() {
        final GoogleApiHelper helper = GoogleApiHelper.getInstance();
        if (!helper.isConnected()){
            return;
        }
        Fitness.SensorsApi.remove( helper.getGoogleApiClient(), this )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            helper.unregister(GoogleFitSensorTracker.this);
                        }
                    }
                });
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {
        isPaused = false;
        countingbeganTsMillis = DateUtil.getServerTimeInMillis();
    }

    private void registerFitnessDataListener(final DataSource dataSource, final DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate(ClientConfig.getInstance().GOOGLE_FIT_SENSOR_TRACKER_SAMPLING_RATE,
                        TimeUnit.SECONDS)
                .build();

        Fitness.SensorsApi.add(GoogleApiHelper.getInstance().getGoogleApiClient(), request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Logger.i(TAG, "SensorApi successfully added for " + dataType.getName());
                            listener.isTrackerReady();
                        }else{
                            Logger.e(TAG, "SensorApi couldn't be added for " + dataType.getName());
                            listener.trackerNotAvailable();
                        }
                    }
                });
    }

    @Override
    public void onConnected() {
        Logger.d(TAG, "onConnected");

        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( dataType )
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources () ) {
                    Logger.d(TAG, "onResult of dataSourcesResultCallback for dataType = " +dataType.getName()
                            +", dataSource found: " + dataSource.toDebugString()
                            + ", type: " + dataSource.getDataType().getName()
                            + ", stream: " + dataSource.getStreamName()
                            + ", streamIdentifier: " + dataSource.getStreamIdentifier()
                            + ", name: " + dataSource.getName());
                    if( dataType.equals(dataSource.getDataType())) {

                        if (dataType.equals(DataType.TYPE_DISTANCE_DELTA)){
                            // For distance sources we want the stream live_distance_from_steps only
                            if ("live_distance_from_steps".equals(dataSource.getStreamName())){
                                registerFitnessDataListener(dataSource, dataSource.getDataType());
                                return;
                            }
                        }else {
                            registerFitnessDataListener(dataSource, dataSource.getDataType());
                            return;
                        }
                    }
                }
                // If we reach here that means that we couldn't find an appropriate datatype
                listener.trackerNotAvailable();
            }
        };

        Fitness.SensorsApi
                .findDataSources(GoogleApiHelper.getInstance().getGoogleApiClient(), dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    @Override
    public void connectionFailed() {
        Logger.d(TAG, "User Denied permission to access Google Fit Data");
        if (listener != null){
            listener.trackerNotAvailable();
        }
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        if (isPaused){
            return;
        }
        for( Field field : dataPoint.getDataType().getFields() ) {
            Value value = dataPoint.getValue( field );
            long startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime < countingbeganTsMillis){
                // This stepcount reading's interval started before the beginning of step counting, will ignore
                Logger.d(TAG, "Older step count reading, will ignore");
            } else {
//                String message = "Field: " + field.getName() + " Value: " + value + " stream: "
//                        + dataPoint.getDataSource().getStreamName();
//                Logger.d(TAG, message);
                if (GoogleApiHelper.getInstance().getFieldFor(dataType).getName().equals(field.getName())){
                    // Step count data
                    Float deltaCount = Float.parseFloat(value.toString());
                    listener.onDeltaCount(
                            dataPoint.getStartTime(TimeUnit.SECONDS),
                            dataPoint.getEndTime(TimeUnit.SECONDS),
                            deltaCount.floatValue());
                }
            }
        }
    }

    interface Listener{
        void isTrackerReady();
        void trackerNotAvailable();
        void onDeltaCount(long beginTs, long endTs, float deltaIncrement);
    }
}
