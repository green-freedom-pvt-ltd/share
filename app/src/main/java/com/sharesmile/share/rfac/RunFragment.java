package com.sharesmile.share.rfac;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.MainActivity;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.gps.RunPathFragment;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import java.io.File;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunFragment extends BaseFragment implements View.OnClickListener {

    private static final String PARAM_TITLE = "param_title";

    private static final String TAG = "RunFragment";
    private static final String WORKOUT_DATA = "workout_data";
    private static final String ERROR_MESSAGE = "error_message";

    MainActivity myActivity;
    View baseView;
    LinearLayout runDataContainer;
    LinearLayout liveDataContainer;

    TextView totalDistanceView, avgSpeedView, totalTimeView, totalStepsView;
    TextView liveDistanceView, liveSpeedView, liveTimeView, liveStepsView;
    TextView errorMessageView;
    ImageView staticGoogleMapView;
    WorkoutData workoutData;

    File logsFile;
    boolean isRunActive;
    private long runStartTime;

    public RunFragment() {
        // Required empty public constructor
    }

    public static RunFragment newInstance() {
        RunFragment fragment = new RunFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = (MainActivity) getActivity();
    }

    public boolean isFragmentAttachedToActivity(){
        return (myActivity != null);
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
        baseView = inflater.inflate(R.layout.fragment_run, container, false);

        baseView.findViewById(R.id.bt_start_run).setOnClickListener(this);
        baseView.findViewById(R.id.bt_end_run).setOnClickListener(this);
        baseView.findViewById(R.id.bt_capture_logs).setOnClickListener(this);
        baseView.findViewById(R.id.bt_email_logs).setOnClickListener(this);

        runDataContainer = (LinearLayout) baseView.findViewById(R.id.run_data_container);
        totalDistanceView = (TextView) baseView.findViewById(R.id.tv_total_distance);
        avgSpeedView = (TextView) baseView.findViewById(R.id.tv_avg_speed);
        totalStepsView = (TextView) baseView.findViewById(R.id.tv_total_steps);
        totalTimeView = (TextView) baseView.findViewById(R.id.tv_total_time);
        errorMessageView = (TextView) baseView.findViewById(R.id.tv_error_message);

        liveDataContainer = (LinearLayout) baseView.findViewById(R.id.live_data_container);
        liveDistanceView = (TextView) baseView.findViewById(R.id.tv_live_distance);
        liveStepsView = (TextView) baseView.findViewById(R.id.tv_live_steps);
        liveSpeedView = (TextView) baseView.findViewById(R.id.tv_live_speed);
        liveTimeView = (TextView) baseView.findViewById(R.id.tv_live_time);

        staticGoogleMapView = (ImageView) baseView.findViewById(R.id.iv_static_google_map);
        staticGoogleMapView.setOnClickListener(this);

        return baseView;
    }

    CountDownTimer newtimer = new CountDownTimer(1000000000, 1000) {

        public void onTick(long millisUntilFinished) {
            long elapsed = System.currentTimeMillis() - runStartTime;
            int secs = (int)(elapsed / 1000);
            String time = Utils.secondsToString(secs);
            liveTimeView.setText(time);
        }
        public void onFinish() {

        }
    };

    public void showRunData(WorkoutData data){
        workoutData = data;
        Logger.d(TAG, "showRunData:\n " + workoutData);
        liveDataContainer.setVisibility(View.GONE);
        runDataContainer.setVisibility(View.VISIBLE);

        String distance = String.format("%1$,.2f", (workoutData.getDistance() / 1000)) + " km";
        String avgSpeed = String.format("%1$,.2f" , workoutData.getAvgSpeed() * 3.6) + " km/hr";
        String time = Utils.secondsToString((int) workoutData.getElapsedTime());
        totalDistanceView.setText(distance);
        avgSpeedView.setText(avgSpeed);
        totalTimeView.setText(time);
        totalStepsView.setText(workoutData.getTotalSteps() + "");
        int size = (int) Utils.convertDpToPixel(getContext(), 300);
        Utils.setStaticGoogleMap(size, size, staticGoogleMapView, workoutData.getPoints());
    }


    public void showUpdate(float speed, float distaneCovered){
        Logger.d(TAG, "showUpdate: speed = " + speed + ", distanceCovered = " + distaneCovered);
        if (isRunActive()){
            String distance = Math.round(distaneCovered)+ " m";
            String speedDecimal = String.format("%1$,.2f" , speed * 3.6) + " km/hr";
            liveDistanceView.setText(distance);
            liveSpeedView.setText(speedDecimal);
        }
    }

    public void showSteps(int stepsSoFar){
        if (isRunActive()){
            liveStepsView.setText(stepsSoFar + "");
        }
    }

    public void endRun(boolean userEnded){
        if (userEnded){
            myActivity.endLocationTracking();
        }
        setIsRunActive(false);
        runStartTime = 0;
        newtimer.cancel();
        liveDataContainer.setVisibility(View.GONE);
        // Wait for total data to show up
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

    private void beginRun(){
        myActivity.beginLocationTracking();
        runDataContainer.setVisibility(View.GONE);
        liveDataContainer.setVisibility(View.VISIBLE);
        logsFile = null;
        setIsRunActive(true);
        workoutData = null;
        runStartTime = System.currentTimeMillis();
        newtimer.start();
        liveDistanceView.setText("0 m");
        liveSpeedView.setText("0 km/hr");
        liveStepsView.setText("0");
    }

    public void setErrorMessageView(String text){
        if (errorMessageView != null){
            errorMessageView.setText(text);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start_run:
                beginRun();
                break;

            case R.id.bt_end_run:
                endRun(true);
                break;

            case R.id.bt_capture_logs:
                logsFile = myActivity.writeLogsToFile(getContext());
                break;

            case R.id.bt_email_logs:
                if (logsFile != null){
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    // The intent does not have a URI, so declare the "text/plain" MIME type
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"maheshwari.ankit.iitd@gmail.com"}); // recipients
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RFAC Logs " + new Date().toString());
                    emailIntent.putExtra(Intent.EXTRA_TEXT, textForMail());
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logsFile));
                    startActivity(Intent.createChooser(emailIntent , "Send email..."));
                }else{
                    Toast.makeText(getContext(), "No Logs", Toast.LENGTH_SHORT);
                }
                break;

            case R.id.iv_static_google_map:
                Logger.d(TAG, "onClick: iv_static_google_map");
                if (workoutData != null){
                    myActivity.replaceFragment(RunPathFragment.newInstance(workoutData));
                }
                break;
        }
    }

    private String textForMail(){
        StringBuilder sb = new StringBuilder();
        sb.append("\nSPEED_TRACKING : " + Config.SPEED_TRACKING);
        sb.append("\nTHRESHOLD_INTERVAL : " + Config.THRESHOLD_INTEVAL + " secs");
        sb.append("\nTHRESHOLD_ACCURACY : " + Config.THRESHOLD_ACCURACY);
        sb.append("\nTHRESHOLD_FACTOR : " + Config.THRESHOLD_FACTOR);
        sb.append("\nVIGILANCE_START_THRESHOLD : " + (Config.VIGILANCE_START_THRESHOLD / 1000) + " secs");
        sb.append("\nUPPER_SPEED_LIMIT : " + Config.UPPER_SPEED_LIMIT*3.6 + " km/hr");
        sb.append("\nLOWER_SPEED_LIMIT : " + Config.LOWER_SPEED_LIMIT*3.6 + " km/hr");
        sb.append("\nSTEPS_PER_SECOND_FACTOR : " + Config.STEPS_PER_SECOND_FACTOR);
        sb.append("\nSMALLEST_DISPLACEMENT : " + Config.SMALLEST_DISPLACEMENT + " m");
        if (workoutData != null){
            sb.append("\nWorkoutData:\n" + workoutData);
        }

        return sb.toString();
    }

    @Override
    protected void onSaveState(Bundle outState) {
        Logger.d(TAG,"onSaveState");
        super.onSaveState(outState);
        if (workoutData != null){
            Logger.d(TAG,"onSaveState: workoutData is present");
            outState.putParcelable(WORKOUT_DATA, workoutData);
            if (errorMessageView != null && !TextUtils.isEmpty(errorMessageView.getText())){
                outState.putString(ERROR_MESSAGE, String.valueOf(errorMessageView.getText()));
            }
        }else{
            outState.remove(WORKOUT_DATA);
            outState.remove(ERROR_MESSAGE);
        }
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        Logger.d(TAG,"onRestoreState");
        super.onRestoreState(savedInstanceState);
        WorkoutData data = savedInstanceState.getParcelable(WORKOUT_DATA);
        if (data != null){
            showRunData(data);
            String errorMessage = savedInstanceState.getString(ERROR_MESSAGE);
            if (!TextUtils.isEmpty(errorMessage)){
                setErrorMessageView(errorMessage);
            }
        }
    }

}
