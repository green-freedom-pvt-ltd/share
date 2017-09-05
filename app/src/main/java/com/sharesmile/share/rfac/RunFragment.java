package com.sharesmile.share.rfac;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.Events.GpsStateChangeEvent;
import com.sharesmile.share.Events.UpdateUiOnMockLocation;
import com.sharesmile.share.Events.UpdateUiOnWorkoutPauseEvent;
import com.sharesmile.share.Events.UpdateUiOnWorkoutResumeEvent;
import com.sharesmile.share.Events.UsainBoltForceExit;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.sharesmile.share.gps.WorkoutSingleton.GPS_STATE_BAD;
import static com.sharesmile.share.gps.WorkoutSingleton.GPS_STATE_INACTIVE;
import static com.sharesmile.share.gps.WorkoutSingleton.GPS_STATE_OK;

public abstract class RunFragment extends BaseFragment implements View.OnClickListener {

    private static final String PARAM_TITLE = "param_title";

    private static final String TAG = "RunFragment";
    public static final long TIMER_TICK = 1000; // in millis
    public static final long NOTIFICATION_TIMER_TICK = 60000; // in millis


    TrackerActivity myActivity;
    private View baseView;

    Handler handler = new Handler();

    private boolean isTimerRunning = false;

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
        Logger.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        baseView = inflater.inflate(getLayoutResId(), container, false);
        populateViews(baseView);
        EventBus.getDefault().register(this);
        return baseView;
    }

    protected abstract void populateViews(View baseView);

    protected abstract int getLayoutResId();

    public abstract void updateTimeView(String newTime);

    public abstract void onWorkoutResult(WorkoutData data);

    public void showUpdate(float speed, float distanceCovered, int elapsedTimeInSecs){
        Logger.d(TAG, "showUpdate: distanceCovered = " + distanceCovered
                + ", elapsedTimeInSecs " + elapsedTimeInSecs);
        if (!isTimerRunning && isRunning()){
            startTimer(elapsedTimeInSecs);
        }else {
            updateTimeView(Utils.secondsToHHMMSS(elapsedTimeInSecs));
        }
    }

    public void showSteps(int stepsSoFar, int elapsedTimeInSecs ){
        if (!isTimerRunning && isRunning()){
            startTimer(elapsedTimeInSecs);
        }else {
            updateTimeView(Utils.secondsToHHMMSS(elapsedTimeInSecs));
        }
    }

    private void startTimer(int initialSecs){
        Logger.d(TAG, "startTimer");
        handler.removeCallbacks(timer);
        handler.postDelayed(timer, TIMER_TICK);
        isTimerRunning = true;
        updateTimeView(Utils.secondsToHHMMSS(initialSecs));
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
        SharedPrefsManager.getInstance().removeKey(Constants.KEY_WORKOUT_TEST_MODE_ON);
        stopTimer();
        onEndRun();
    }


    public void pauseRun(boolean userPaused) {
        Logger.d(TAG, "pauseRun");
        stopTimer();
        if (userPaused) {
            myActivity.pauseWorkout();
        }
        onPauseRun();
    }

    public void resumeRun() {
        Logger.d(TAG, "resumeRun");
        if (myActivity.resumeWorkout()){
            startTimer((int) myActivity.getElapsedTimeInSecs());
            onResumeRun();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateUiOnWorkoutPauseEvent updateUiOnWorkoutPauseEvent) {
        Logger.d(TAG, "onEvent: UpdateUiOnWorkoutPauseEvent");

        onPauseRun();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateUiOnWorkoutResumeEvent updateUiOnWorkoutResumeEvent) {
        Logger.d(TAG, "onEvent: UpdateUiOnWorkoutResumeEvent");
        startTimer((int) myActivity.getElapsedTimeInSecs());
        onResumeRun();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateUiOnMockLocation updateUiOnMockLocation) {
        Logger.d(TAG, "onEvent: UpdateUiOnMockLocation");
        showForceExitDialogAfterStopRun(MainApplication.getContext().getString(R.string.mock_location_detected),
                MainApplication.getContext().getString(R.string.mock_location_detected_content));
        AnalyticsEvent.create(Event.ON_LOAD_DISBALE_MOCK_LOCATION)
                .buildAndDispatch();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UsainBoltForceExit usainBoltForceExit) {
        Logger.d(TAG, "onEvent: UsainBoltForceExit");
        showForceExitDialogAfterStopRun(MainApplication.getContext().getString(R.string.you_are_in_vehicle),
                MainApplication.getContext().getString(R.string.usain_bolt_force_exit_popup_content));
        AnalyticsEvent.create(Event.ON_LOAD_USAIN_BOLT_FORCE_EXIT)
                .buildAndDispatch();
    }

    @Override
    public void onStart() {
        super.onStart();
        isOnDisplay = true;
        if (WorkoutSingleton.getInstance().toShowWeakGpsPopup()){
            showGpsWeakDialog();
        }else {
            hideGpsWeakDialog();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isOnDisplay = false;
    }

    boolean isOnDisplay = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GpsStateChangeEvent gpsStateChangeEvent) {
        Logger.d(TAG, "onEvent: GpsStateChangeEvent");
        if (isOnDisplay){
            if (WorkoutSingleton.getInstance().toShowWeakGpsPopup()){
                showGpsWeakDialog();
            }else {
                hideGpsWeakDialog();
            }
        }
    }

    AlertDialog gpsWeakDialog;

    private void showGpsWeakDialog(){
        if (gpsWeakDialog == null){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    WorkoutSingleton.getInstance().setGpsState(GPS_STATE_OK);

                }
            });
            alertDialog.setCancelable(false);
            gpsWeakDialog = alertDialog.create();
        }else {
            gpsWeakDialog.dismiss();
        }
        switch (WorkoutSingleton.getInstance().getGpsState()){
            case GPS_STATE_BAD:
                gpsWeakDialog.setTitle(getString(R.string.notification_bad_gps_title));
                gpsWeakDialog.setMessage(getString(R.string.notification_bad_gps_description));
                gpsWeakDialog.show();
                AnalyticsEvent.create(Event.ON_LOAD_GPS_WEAK_POPUP)
                        .addBundle(getWorkoutBundle())
                        .put("num_spikes", WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes())
                        .put("num_update_events", WorkoutSingleton.getInstance().getDataStore().getNumUpdateEvents())
                        .buildAndDispatch();
                break;
            case GPS_STATE_INACTIVE:
                gpsWeakDialog.setTitle(getString(R.string.notification_gps_inactivity_title));
                gpsWeakDialog.setMessage(getString(R.string.notification_gps_inactivity_description));
                gpsWeakDialog.show();
                AnalyticsEvent.create(Event.ON_LOAD_GPS_INACTIVE_POPUP)
                        .addBundle(getWorkoutBundle())
                        .put("num_spikes", WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes())
                        .put("num_update_events", WorkoutSingleton.getInstance().getDataStore().getNumUpdateEvents())
                        .buildAndDispatch();

                break;
            default:
                throw new IllegalStateException("Inconsistent GPS state");
        }
    }

    private void hideGpsWeakDialog(){
        if (gpsWeakDialog != null && gpsWeakDialog.isShowing()){
            gpsWeakDialog.dismiss();
        }
    }

    public void stopTimer(){
        Logger.d(TAG, "stopTimer");
        handler.removeCallbacks(timer);
        isTimerRunning = false;
    }

    private void showForceExitDialogAfterStopRun(String title, String content) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(content);
        alertDialog.setCancelable(false);
        alertDialog.setNegativeButton(MainApplication.getContext().getString(R.string.ok_caps),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (isAttachedToActivity()) {
                            myActivity.exit();
                        }
            }
        });
        alertDialog.show();
    }

    public boolean isRunActive() {
        synchronized (RunFragment.class) {
            return WorkoutSingleton.getInstance().isWorkoutActive();
        }
    }

    public abstract void refreshWorkoutData();

    public Properties getWorkoutBundle(){
        return WorkoutSingleton.getInstance().getWorkoutBundle();
    }

    protected void continuedRun(){
        Logger.d(TAG, "continuedRun");
        int elapsedTImeInSecs = (int) WorkoutSingleton.getInstance().getDataStore().getElapsedTime();
        Logger.d(TAG, "elapsedTimeInSecs = " + elapsedTImeInSecs);
        myActivity.continuedRun();
        if (!isRunning()){
            // If the run is in paused state then don't start timer
            updateTimeView(Utils.secondsToHHMMSS(elapsedTImeInSecs));
        }else {
            // Workout is in running state
            startTimer(elapsedTImeInSecs);
        }
        onContinuedRun(!isRunning());
    }


    protected void beginRun() {
        Logger.d(TAG, "beginRun");
        myActivity.beginRun();
        startTimer(0);
        onBeginRun();
    }

    public boolean isRunning() {
        return WorkoutSingleton.getInstance().isRunning();
    }

    @Override
    public void onDestroyView() {
        Logger.d(TAG, "onDestroyView");
        super.onDestroyView();
        stopTimer();
        EventBus.getDefault().unregister(this);
        hideGpsWeakDialog();
    }

    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            if (isAttachedToActivity()){
                int elapsedTimeInSecs = (int) myActivity.getElapsedTimeInSecs();
                updateTimeView(Utils.secondsToHHMMSS(elapsedTimeInSecs));
            }
            handler.postDelayed(this, TIMER_TICK);
        }
    };

    public abstract void showStopDialog();

}
