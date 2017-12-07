package com.sharesmile.share.googleapis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.sharesmile.share.googleapis.event.GoogleApiResultEvent;
import com.sharesmile.share.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by ankitmaheshwari on 12/2/17.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "GoogleApiHelper";

    public static final String KEY_RESOLUTION_PARCELABLE = "key_resolution_parcelable";
    public static final String KEY_API_CODE = "key_api_code";

    public static final int API_SESSION_RECORDING = 1;
    public static final int API_LIVE_DISTANCE_TRACKING = 2;
    public static final int API_LIVE_STEP_COUNTING = 3;

    GoogleApiClient googleApiClient;
    private int apiCode;
    private Context context;
    private Listener listener;

    public GoogleApiHelper(int apiCode, Context context) {
        this.apiCode = apiCode;
        this.context = context;
        this.googleApiClient = buildApiClient();
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    private GoogleApiClient buildApiClient(){
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
        switch (apiCode){
            case API_LIVE_STEP_COUNTING:
                builder.addApi(Fitness.SENSORS_API)
                        .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ));
                break;
            case API_SESSION_RECORDING:
                builder
                        .addApi(Fitness.SESSIONS_API)
                        .addApi(Fitness.HISTORY_API)
                        .addApi(Fitness.RECORDING_API)
                        .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                        .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                        .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                        .build();
                break;
            case API_LIVE_DISTANCE_TRACKING:

                break;
            default:
                throw new IllegalArgumentException("Api not supported for code: " + apiCode);
        }
        return builder.addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

    }

    public void connect(){
        EventBus.getDefault().register(this);
        googleApiClient.connect();
    }

    public boolean isConnected(){
        return googleApiClient != null && googleApiClient.isConnected();
    }

    public void disconnect(){
        if (googleApiClient != null && googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        EventBus.getDefault().unregister(this);
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GoogleApiResultEvent event) {
        if( event.getResultCode() == Activity.RESULT_OK) {
            if( !googleApiClient.isConnecting() && !googleApiClient.isConnected() ) {
                googleApiClient.connect();
            }
            return;
        } else if( event.getResultCode() == Activity.RESULT_CANCELED ) {
            Logger.e(TAG, "RESULT_CANCELED for requestCode " + event.getRequestCode());
            if (listener != null){
                listener.connectionFailed();
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (listener != null){
            listener.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.e(TAG, "onConnectionFailed, hasResolution = " + connectionResult.hasResolution());
        if (connectionResult.hasResolution()){
            Intent intent = new Intent(context, GoogleApiHelperActivity.class);
            intent.putExtra(KEY_RESOLUTION_PARCELABLE, connectionResult);
            intent.putExtra(KEY_API_CODE, apiCode);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public interface Listener{
        void onConnected();
        void connectionFailed();
    }
}
