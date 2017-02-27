package com.sharesmile.share.rfac;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.GoogleLocationTracker;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

;


public abstract class RunFragment extends BaseFragment implements View.OnClickListener {

    private static final String PARAM_TITLE = "param_title";

    private static final String TAG = "RunFragment";
    public static final long TIMER_TICK = 1000; // in millis

    public static final String SECS_ELAPSED_ON_PAUSE = "secs_elapsed_on_pause";


    TrackerActivity myActivity;
    private View baseView;

    boolean isRunActive;
    Handler handler = new Handler();

    private int secsSinceRunBegan = -1;

    public RunFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = (TrackerActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        baseView = inflater.inflate(getLayoutResId(), container, false);
        populateViews(baseView);
        return baseView;
    }

    protected abstract void populateViews(View baseView);

    protected abstract int getLayoutResId();

    public abstract void updateTimeView(String newTime);

    public abstract void onWorkoutResult(WorkoutData data);

    public void showUpdate(float speed, float distanceCovered, int elapsedTimeInSecs){
        Logger.d(TAG, "showUpdate: distanceCovered = " + distanceCovered
                + ", elapsedTimeInSecs " + elapsedTimeInSecs
                + ", secsSinceRunBegan = " + secsSinceRunBegan);
        if (secsSinceRunBegan == -1){
            startTimer(elapsedTimeInSecs);
        }
    }

    public void showSteps(int stepsSoFar, int elapsedTimeInSecs ){
        if (secsSinceRunBegan == -1){
            startTimer(elapsedTimeInSecs);
        }
    }

    private void startTimer(int initialSecs){
        secsSinceRunBegan = initialSecs;
        handler.postDelayed(timer, TIMER_TICK);
    }

    protected abstract void onEndRun();

    protected abstract void onPauseRun();

    protected abstract void onResumeRun();

    protected abstract void onBeginRun();

    protected abstract void onContinuedRun(boolean isPaused);

    public abstract void showErrorMessage(String text);


    public void endRun(boolean userEnded) {
        Logger.d(TAG, "endRun, userEnded = " + userEnded);
        if (userEnded) {
            myActivity.endRun();
        }
        setIsRunActive(false);
        SharedPrefsManager.getInstance().removeKey(SECS_ELAPSED_ON_PAUSE);
        SharedPrefsManager.getInstance().removeKey(Constants.KEY_WORKOUT_TEST_MODE_ON);
        handler.removeCallbacks(timer);
        onEndRun();
    }


    public void pauseRun(boolean userPaused) {
        Logger.d(TAG, "pauseRun");
        handler.removeCallbacks(timer);
        SharedPrefsManager.getInstance().setInt(SECS_ELAPSED_ON_PAUSE, secsSinceRunBegan);
        if (userPaused) {
            myActivity.pauseWorkout();
        }
        onPauseRun();
    }

    public void resumeRun() {
        Logger.d(TAG, "resumeRun");
        if (GoogleLocationTracker.getInstance().isFetchingLocation()){
            // Resume will always be done by the user
            myActivity.resumeWorkout();
            startTimer(SharedPrefsManager.getInstance().getInt(SECS_ELAPSED_ON_PAUSE));
            SharedPrefsManager.getInstance().removeKey(SECS_ELAPSED_ON_PAUSE);
            onResumeRun();
        }else {
            GoogleLocationTracker.getInstance().startLocationTracking(true);
        }
    }

    private void setIsRunActive(boolean b) {
        Logger.d(TAG, "setIsRunActive, b = " + b);
        synchronized (RunFragment.class) {
            isRunActive = b;
        }
    }

    public boolean isRunActive() {
        synchronized (RunFragment.class) {
            return isRunActive;
        }
    }

    public void refreshWorkoutData(){
        showUpdate(myActivity.getCurrentSpeed(), myActivity.getTotalDistanceInMeters(),
                (int) myActivity.getElapsedTimeInSecs());
    }

    public Properties getWorkoutBundle(){
        if (myActivity != null){
            Properties p = new Properties();
            p.put("distance", Utils.formatToKmsWithOneDecimal(myActivity.getTotalDistanceInMeters()));
            p.put("time_elapsed", myActivity.getElapsedTimeInSecs());
            p.put("avg_speed", myActivity.getAvgSpeed()*(3.6f));
            p.put("num_steps", myActivity.getTotalSteps());
            return p;
        }
        return null;
    }

    protected void continuedRun(){
        Logger.d(TAG, "continuedRun");
        myActivity.continuedRun();
        setIsRunActive(true);
        if (!isRunning()){
            updateTimeView(Utils.secondsToString(SharedPrefsManager
                    .getInstance().getInt(SECS_ELAPSED_ON_PAUSE)));
        }
        onContinuedRun(!isRunning());
    }

    protected void beginRun() {
        Logger.d(TAG, "beginRun");
        myActivity.beginRun();
        setIsRunActive(true);
        startTimer(0);
        SharedPrefsManager.getInstance().removeKey(SECS_ELAPSED_ON_PAUSE);
        onBeginRun();
    }

    public boolean isRunning() {
        return isRunActive() && !SharedPrefsManager.getInstance().containsKey(SECS_ELAPSED_ON_PAUSE);
    }

    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            secsSinceRunBegan++;
            updateTimeView(Utils.secondsToString(secsSinceRunBegan));
            handler.postDelayed(this, TIMER_TICK);
        }
    };

    public abstract void showStopDialog();
}
