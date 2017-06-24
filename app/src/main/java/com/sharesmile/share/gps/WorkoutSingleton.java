package com.sharesmile.share.gps;

import android.app.NotificationManager;
import android.content.Context;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.SharedPrefsManager;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_ID;

/**
 * Created by ankitmaheshwari on 4/3/17.
 */

public class WorkoutSingleton {

    private static final String TAG = "WorkoutSingleton";

    private static WorkoutSingleton uniqueInstance;

    private WorkoutDataStore dataStore;

    private boolean toShowEndRunDialog;

    private int gpsState = 0;

    public static final int GPS_STATE_BAD = 1;
    public static final int GPS_STATE_INACTIVE = 2;
    public static final int GPS_STATE_OK = 0;

    private WorkoutSingleton(Context appContext){
        if (isWorkoutActive()){
            dataStore = new WorkoutDataStoreImpl();
        }
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique WorkoutSingleton instance
     */
    public static WorkoutSingleton getInstance() {
        if (uniqueInstance == null) {
            throw new IllegalStateException(
                    "WorkoutSingleton is not initialized, call initialize(applicationContext) " +
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
            synchronized (WorkoutSingleton.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new WorkoutSingleton(appContext);
                }
            }
        }
    }

    public WorkoutDataStore beginWorkout(){
        setState(State.RUNNING);
        NotificationManager manager = (NotificationManager) MainApplication.getContext().getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(WORKOUT_NOTIFICATION_ID);
        dataStore = new WorkoutDataStoreImpl(DateUtil.getServerTimeInMillis());
        return dataStore;
    }

    public void endWorkout(){
        setState(State.IDLE);
        dataStore = null;
    }

    public void pauseWorkout(){
        setState(State.PAUSED);
        dataStore.workoutPause();
    }

    public void resumeWorkout(){
        setState(State.RUNNING);
        dataStore.workoutResume();
    }

    public State getState() {
        return State.valueOf(SharedPrefsManager.getInstance().getString(Constants.PREF_WORKOUT_STATE, State.IDLE.name()));
    }

    public void setToShowEndRunDialog(boolean value){
        this.toShowEndRunDialog = value;
    }

    public boolean toShowEndRunDialog(){
        return toShowEndRunDialog;
    }

    public boolean toShowWeakGpsPopup() {
        return gpsState > 0;
    }

    public void setGpsState(int state) {
        this.gpsState = state;
    }

    public int getGpsState(){
        return gpsState;
    }

    public void mockLocationDetected(){
        dataStore.setMockLocationDetected(true);
    }

    public boolean isMockLocationEnabled(){
        return dataStore.isMockLocationDetected();
    }

    public boolean hasConsecutiveUsainBolts(){
        return dataStore.hasConsecutiveUsainBolts();
    }

    public void incrementUsainBoltsCounter(){
        dataStore.incrementUsainBoltCounter();
    }

    private void setState(State state){
        SharedPrefsManager.getInstance().setString(Constants.PREF_WORKOUT_STATE, state.name());
    }

    public boolean isWorkoutActive(){
        return (getState() != State.IDLE);
    }

    public boolean isRunning(){
        return (getState() == State.RUNNING);
    }

    public WorkoutDataStore getDataStore(){
        return this.dataStore;
    }

    enum State{
        IDLE,
        RUNNING,
        PAUSED;
    }
}
