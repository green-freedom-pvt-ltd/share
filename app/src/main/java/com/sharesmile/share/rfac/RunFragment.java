package com.sharesmile.share.rfac;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.gps.models.WorkoutData;;
import com.sharesmile.share.utils.Utils;


public abstract  class RunFragment extends BaseFragment implements View.OnClickListener {

    private static final String PARAM_TITLE = "param_title";

    private static final String TAG = "RunFragment";
    public static final long TIMER_TICK = 1000; // in millis


    TrackerActivity myActivity;
    private View baseView;

    WorkoutData workoutData;

    boolean isRunActive;
    boolean isRunnning;
    private long runStartTime;
    Handler handler = new Handler();

    private int secsSinceRunBegan = 0;

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

    public abstract void showUpdate(float speed, float distanceCovered);

    public abstract void showSteps(int stepsSoFar);

    protected abstract void onEndRun();

    protected abstract void onPauseRun();

    protected abstract void onResumeRun();

    protected abstract void onBeginRun();

    public abstract void showErrorMessage(String text);


    public void endRun(boolean userEnded){
        if (userEnded){
            myActivity.endLocationTracking();
        }
        setIsRunActive(false);
        isRunnning = false;
        runStartTime = 0;
        handler.removeCallbacks(timer);
        onEndRun();
    }


    public void pauseRun(boolean userPaused){
        isRunnning = false;
        handler.removeCallbacks(timer);
        if (userPaused){
            myActivity.pauseWorkout();
        }
        onPauseRun();
    }

    public void resumeRun(){
        // Resume will always be done by the user
        isRunnning = true;
        myActivity.resumeWorkout();
        handler.postDelayed(timer, TIMER_TICK);
        onResumeRun();
    }

    private void setIsRunActive(boolean b){
        synchronized (RunFragment.class){
            isRunActive = b;
        }
    }

    public boolean isRunActive(){
        synchronized (RunFragment.class){
            return isRunActive;
        }
    }

    protected void beginRun(){
        myActivity.beginLocationTracking();
        setIsRunActive(true);
        isRunnning = true;
        workoutData = null;
        runStartTime = System.currentTimeMillis();
        secsSinceRunBegan = 0;
        handler.postDelayed(timer, TIMER_TICK);
        onBeginRun();
    }

    public boolean isRunning(){
        return isRunnning;
    }

    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            secsSinceRunBegan++;
            updateTimeView(Utils.secondsToString(secsSinceRunBegan));
            handler.postDelayed(this, TIMER_TICK);
        }
    };

}
