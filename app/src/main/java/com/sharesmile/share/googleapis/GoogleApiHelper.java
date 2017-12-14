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
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.googleapis.event.GoogleApiResultEvent;
import com.sharesmile.share.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ankitmaheshwari on 12/2/17.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "GoogleApiHelper";

    public static final String KEY_RESOLUTION_PARCELABLE = "key_resolution_parcelable";
    public static final String KEY_API_CODE = "key_api_code";

    public static final int GOOGLE_FIT_CONNECTION_RESOLUTION_CODE = 100;

    private static GoogleApiHelper uniqueInstance;
    Set<WeakReference<Listener>> listeners;

    private GoogleApiClient googleApiClient;
    private Context context;
    private State state;

    private GoogleApiHelper(Context context) {
        this.context = context;
        this.googleApiClient = buildApiClient();
        state = State.IDLE;
        listeners = new HashSet<>();
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique GoogleApiHelper instance
     */
    public static GoogleApiHelper getInstance() {
        if (uniqueInstance == null) {
            synchronized (GoogleApiHelper.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new GoogleApiHelper(MainApplication.getContext());
                }
            }
        }
        return uniqueInstance;
    }

    private GoogleApiClient buildApiClient(){
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
        builder.addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ));

        return builder.addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
    }

    public void register(Listener listener){
        Logger.i(TAG, "register");
        if (listener == null){
            return;
        }

        if (state == State.IDLE){
            // Very first listener to be registered, will connect the googleAPI client
            EventBus.getDefault().register(this);
            connect();
        }

        boolean toAdd = true;
        for (WeakReference<Listener> reference : listeners) {
            if (listener.equals(reference.get())){
                toAdd = false;
            }
        }
        if (toAdd){
            Logger.d(TAG, "Will register this listener for location updates");
            WeakReference<Listener> reference = new WeakReference<>(listener);
            listeners.add(reference);
            if (state == State.CONNECTED){
                listener.onConnected();
            }
        }
    }

    public void unregister(Listener listener){
        Logger.d(TAG, "unregister");
        if (listener == null){
            return;
        }

        Iterator<WeakReference<Listener>> iterator = listeners.iterator();
        while (iterator.hasNext()){
            WeakReference<Listener> reference = iterator.next();
            if (reference.get() != null){
                if (listener.equals(reference.get())){
                    iterator.remove();
                }
            }else {
                iterator.remove();
            }
        }

        if (listeners.isEmpty() && state != State.IDLE){
            // All subscribers have been unregistered, so disconnect client and change state
            Logger.d(TAG, "unregister: disconnecting googleApiClient and changing state to IDLE");
            googleApiClient.disconnect();
            state = State.IDLE;
            EventBus.getDefault().unregister(this);
        }

    }

    private void connect(){
        googleApiClient.connect();
        state = State.CONNECTING;
    }

    public boolean isConnected(){
        return state == State.CONNECTED;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GoogleApiResultEvent event) {
        if( event.getResultCode() == Activity.RESULT_OK) {
            connect();
            return;
        } else if( event.getResultCode() == Activity.RESULT_CANCELED ) {
            Logger.e(TAG, "RESULT_CANCELED for requestCode " + event.getRequestCode());
            // Notify Listeners about the failure
            state = State.IDLE;
            EventBus.getDefault().unregister(this);
            notifyFailure();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Notify Listeners about success
        state = State.CONNECTED;
        notifySucces();
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
            intent.putExtra(KEY_API_CODE, GOOGLE_FIT_CONNECTION_RESOLUTION_CODE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }else {
            String error = connectionResult.getErrorMessage();
            MainApplication.showToast(error);
            // Notify Listeners about the failure
            state = State.IDLE;
            EventBus.getDefault().unregister(this);
            notifyFailure();
        }
    }

    private void notifyFailure(){
        Iterator<WeakReference<Listener>> iterator = listeners.iterator();
        while (iterator.hasNext()){
            WeakReference<Listener> reference = iterator.next();
            if (reference.get() != null){
                reference.get().connectionFailed();
            }
        }
    }

    private void notifySucces(){
        Iterator<WeakReference<Listener>> iterator = listeners.iterator();
        while (iterator.hasNext()){
            WeakReference<Listener> reference = iterator.next();
            if (reference.get() != null){
                reference.get().onConnected();
            }
        }
    }

    public Field getFieldFor(DataType dataType){
        if (DataType.TYPE_DISTANCE_DELTA.equals(dataType)){
            return Field.FIELD_DISTANCE;
        }else if (DataType.TYPE_STEP_COUNT_DELTA.equals(dataType)){
            return Field.FIELD_STEPS;
        }else if (DataType.TYPE_CALORIES_EXPENDED.equals(dataType)){
            return Field.FIELD_CALORIES;
        }
        return null;
    }

    public interface Listener{
        void onConnected();
        void connectionFailed();
    }

    public enum State{
        IDLE,
        CONNECTING,
        CONNECTED;
    }
}
