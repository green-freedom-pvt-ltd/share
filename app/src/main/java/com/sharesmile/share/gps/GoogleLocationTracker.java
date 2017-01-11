package com.sharesmile.share.gps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by ankitm on 22/12/16.
 */
public class GoogleLocationTracker implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "GoogleLocationTracker";

    private static GoogleLocationTracker uniqueInstance;

    private Context appContext;
    private GoogleApiClient googleApiClient;
    private boolean isActive = false;
    private boolean workoutInProgess;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private State state;
    private Handler handler;
    Set<WeakReference<Listener>> listeners;
    private boolean permissionAskedOnce;

    private GoogleLocationTracker(Context appContext) {
        this.appContext = appContext;
        this.handler = new Handler();
        this.listeners = new HashSet<>();
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // All required permissions available
            state = State.PERMISSIONS_GRANTED;
            connectToLocationServices();
        } else {
            //Need to get permissions
            state = State.NEEDS_PERMISSION;
        }
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique GoogleLocationTracker instance
     */
    public static GoogleLocationTracker getInstance() {
        if (uniqueInstance == null) {
            throw new IllegalStateException(
                    "GoogleLocationTracker is not initialized, call initialize(applicationContext) " +
                            "static method first");
        }
        return uniqueInstance;
    }

    /**
     Initialize this class using application Context,
     should be called once in the beginning by any application Component

     @param appContext application context
     */
    public static void initialize(Context appContext) {
        if (appContext == null) {
            throw new NullPointerException("Provided application context is null");
        }
        if (uniqueInstance == null) {
            synchronized (GoogleLocationTracker.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new GoogleLocationTracker(appContext);
                }
            }
        }
    }

    public void register(Listener listener){
        if (listener != null){
            boolean toAdd = true;
            for (WeakReference<Listener> reference : listeners){
                if (listener.equals(reference.get())){
                    toAdd = false;
                }
            }
            if (toAdd){
                WeakReference<Listener> reference = new WeakReference<>(listener);
                listeners.add(reference);
                workoutInProgess = true;
                if (state != State.FETCHING_LOCATION){
                    startLocationTracking();
                }else {
                    listener.onLocationTrackerReady();
                }
            }
        }
    }

    public void unregister(Listener listener){
        if (listener != null){
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
        }
        if (listeners.size() == 0){
            // No more active listeners
            workoutInProgess = false;
        }
    }

    public void stopLocationTracking(){
        if (workoutInProgess == false){
            // Will stop location updates only if Workout is not in progress
            if (state == State.FETCHING_LOCATION){
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                state = State.LOCATION_ENABLED;
            }
            isActive = false;
        }
    }

    public void startLocationTracking() {
        Logger.d(TAG, "startLocationTracking");
        isActive = true;
        if (state == State.NEEDS_PERMISSION){
            if (workoutInProgess){
                // First of all need to fetch permissions from user
                sendPermissionBroadcast();
            }else if (permissionAskedOnce == false){
                sendPermissionBroadcast();
            }
        }else if (state == State.PERMISSIONS_GRANTED){
            // Permissions granted, Google Api client not connected
            connectToLocationServices();
        }else if (state == State.API_CLIENT_CONNECTED){
            // Permissions granted, Google Api client connected, but Location not enabled
            checkForLocationSettings();
        }else if (state == State.LOCATION_ENABLED){
            // Permissions granted, Google Api client connected, but Location enabled but updates didn't start to flow in yet
            handler.post(new Runnable() {
                @Override
                public void run() {
                    requestLocationUpdates();
                }
            });
        }
        // In all other cases, do nothing
    }

    private void requestLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        state = State.FETCHING_LOCATION;
        for (WeakReference<Listener> reference : listeners){
            if (reference.get() != null){
                reference.get().onLocationTrackerReady();
            }
        }
    }

    public void onPermissionsGranted(){
        state = State.PERMISSIONS_GRANTED;
        permissionAskedOnce = true;
        connectToLocationServices();
    }

    public void onPermissionsRejected(){
        state = State.NEEDS_PERMISSION;
        permissionAskedOnce = true;
        // Permission was denied or request was cancelled
        Logger.i(TAG, "Location Permission denied, couldn't update the UI");
        Toast.makeText(appContext, "Please give Location access", Toast.LENGTH_LONG).show();
        for (WeakReference<Listener> reference : listeners){
            if (reference.get() != null){
                reference.get().onPermissionDenied();
            }
        }
    }

    private void connectToLocationServices(){
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS) {
            if (googleApiClient == null){
                googleApiClient = new GoogleApiClient.Builder(appContext)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Logger.e(TAG, "unable to connect to google play services.");
            Toast.makeText(appContext, "Unable to connect to google play services", Toast.LENGTH_SHORT);
        }
    }


    private void fetchInitialLocation() {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            currentLocation =
                    LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (currentLocation == null) {
                Logger.i(TAG, "Last Known Location couldn't be fetched");
            }
        } else {
            //No need to worry about permission unavailability, as it was already granted in constructor
        }
    }

    private void sendPermissionBroadcast(){
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.LOCATION_TRACKER_BROADCAST_CATEGORY,
                Constants.BROADCAST_REQUEST_PERMISSION_CODE);
        Intent intent = new Intent(Constants.LOCATION_TRACKER_BROADCAST_ACTION);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CODE_LOCATION_SETTINGS_RESOLUTION){
            if (resultCode == Activity.RESULT_OK) {
                // Can startWorkout with location requests
                state = State.LOCATION_ENABLED;
                requestLocationUpdates();
            } else {
                // Can't do nothing, retry for enabling Location Settings
                checkForLocationSettings();
            }
        }
    }

    private void checkForLocationSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        state = State.LOCATION_ENABLED;
                        requestLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        if (workoutInProgess){
                            // Show location enable dialog only when a workout is in progress
                            sendLocationEnableBroadcast(status);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Logger.e(TAG, "Error: Can't enable location updates SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }

    private void sendLocationEnableBroadcast(Status status){
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.LOCATION_TRACKER_BROADCAST_CATEGORY,
                Constants.BROADCAST_FIX_LOCATION_SETTINGS_CODE);
        Intent intent = new Intent(Constants.LOCATION_TRACKER_BROADCAST_ACTION);
        bundle.putParcelable(Constants.KEY_LOCATION_SETTINGS_PARCELABLE,
                status);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or startWorkout periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Logger.d(TAG, "onConnected");
        state = State.API_CLIENT_CONNECTED;
        retryAttempt = 0;

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(Config.LOCATION_UPDATE_INTERVAL); // milliseconds
        locationRequest.setSmallestDisplacement(Config.SMALLEST_DISPLACEMENT);
        locationRequest.setFastestInterval(Config.LOCATION_UPDATE_INTERVAL); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (isActive){
            checkForLocationSettings();
        }

        fetchInitialLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Connection temporarily suspended, onConnected will be called once connection is back
        Logger.e(TAG, "GoogleApiClient connection has been suspend");
        state = State.API_CLIENT_CONNECTION_SUSPENDED;
    }

    int retryAttempt = 0;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.e(TAG, "onConnectionFailed");
        if (retryAttempt < 3){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectToLocationServices();
                }
            }, 500);
            retryAttempt++;
        }
        else {
            for (WeakReference<Listener> reference : listeners){
                if (reference.get() != null){
                    reference.get().onConnectionFailure();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        for (WeakReference<Listener> reference : listeners){
            if (reference.get() != null){
                reference.get().onLocationChanged(location);
            }
        }

        // TODO: Handle the case when GPS location is turned off during run
    }

    public boolean isGpsEnabled(){
        try {
            LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (SecurityException se){
            Logger.e(TAG, "Location permission not granted: " + se.getMessage());
            se.printStackTrace();
            return false;
        }
    }

    public interface Listener{
        void onLocationTrackerReady();
        void onLocationChanged(Location location);
        void onPermissionDenied();
        void onConnectionFailure();
    }

    public enum State{
        NEEDS_PERMISSION, // Needs location access permission from the user
        PERMISSIONS_GRANTED, // Location Access permissions granted by the user
        API_CLIENT_CONNECTED, // Location permissions present and GoogleApiClient connected
        API_CLIENT_CONNECTION_SUSPENDED, // Location permissions present and GoogleApiClient connection was suspended temporarily
        LOCATION_ENABLED, // Location permissions present, GoogleApiClient connected and Location enabled by the user and location updates have not started to flow in yet
        FETCHING_LOCATION, // Location permissions present, GoogleApiClient connected and Location enabled by the user and location updates flowing in already
    }

}
