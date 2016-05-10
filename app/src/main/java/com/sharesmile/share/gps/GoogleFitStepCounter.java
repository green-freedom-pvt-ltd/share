package com.sharesmile.share.gps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
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
import com.sharesmile.share.utils.Logger;

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

    public GoogleFitStepCounter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        startCounting();
    }

    @Override
    public void startCounting() {
        Logger.d(TAG, "startCounting");
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
    }

    @Override
    public void stopCounting() {
        Logger.d(TAG, "stopCounting");
        Fitness.SensorsApi.remove( mApiClient, this )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mApiClient.disconnect();
                        }
                    }
                });
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
    public void onConnected(Bundle bundle) {
        Logger.d(TAG, "onConnected");
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.AGGREGATE_STEP_COUNT_DELTA )
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                Log.i(TAG, "onResult of dataSourcesResultCallback, Status: " + dataSourcesResult.getStatus().toString());
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    Logger.d(TAG, "onResult of dataSourcesResultCallback, dataSource found: " + dataSource.toDebugString());
                    Logger.d(TAG, "onResult of dataSourcesResultCallback, dataSource type: " + dataSource.getDataType().getName());
                    if( DataType.AGGREGATE_STEP_COUNT_DELTA.equals( dataSource.getDataType() ) ) {
                        Logger.d(TAG, "onResult of dataSourcesResultCallback, will register FitnessDataListener");
                        registerFitnessDataListener(dataSource, DataType.AGGREGATE_STEP_COUNT_DELTA);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate( 3, TimeUnit.SECONDS )
                .build();

        Fitness.SensorsApi.add(mApiClient, request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Logger.i(TAG, "SensorApi successfully added");
                            listener.isReady();
                        }else{
                            Logger.e(TAG, "SensorApi couldn't be added");
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
            bundle.putInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY,
                    Constants.BROADCAST_GOOGLE_FIT_READ_PERMISSION);
            Intent intent = new Intent(Constants.LOCATION_SERVICE_BROADCAST_ACTION);
            bundle.putParcelable(Constants.KEY_GOOGLE_FIT_RESOLUTION_PARCELABLE,
                    connectionResult);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        Logger.d(TAG, "onDataPoint");
        for( Field field : dataPoint.getDataType().getFields() ) {
            Value value = dataPoint.getValue( field );
            Logger.d(TAG, "Field: " + field.getName() + " Value: " + value);
            listener.onStepCount(Integer.parseInt(value.toString()));
        }
    }
}
