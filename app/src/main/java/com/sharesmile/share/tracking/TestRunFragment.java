package com.sharesmile.share.tracking;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.R;
import com.sharesmile.share.core.config.ClientConfig;
import com.sharesmile.share.core.config.Config;
import com.sharesmile.share.tracking.ui.RunPathFragment;
import com.sharesmile.share.tracking.models.WorkoutData;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.utils.Utils;

import java.io.File;
import java.util.Date;

/**
 * Created by ankitm on 22/04/16.
 */
public class TestRunFragment extends RunFragment implements View.OnClickListener {

    private static final String TAG = "RawRunFragment";

    private static final String WORKOUT_DATA = "workout_data";
    private static final String ERROR_MESSAGE = "error_message";
    WorkoutData workoutData;

    LinearLayout runDataContainer;
    LinearLayout liveDataContainer;

    TextView totalDistanceView, avgSpeedView, totalTimeView, totalStepsView;
    TextView liveDistanceView, liveSpeedView, liveTimeView, liveStepsView;
    TextView errorMessageView;
    ImageView staticGoogleMapView;

    File logsFile;
    Button startPauseResume;

    public static TestRunFragment newInstance() {
        TestRunFragment fragment = new TestRunFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void populateViews(View baseView) {
        baseView.findViewById(R.id.bt_end_run).setOnClickListener(this);
        baseView.findViewById(R.id.bt_capture_logs).setOnClickListener(this);
        baseView.findViewById(R.id.bt_email_logs).setOnClickListener(this);
        baseView.findViewById(R.id.bt_edit_config).setOnClickListener(this);

        startPauseResume = (Button) baseView.findViewById(R.id.bt_start_run);
        startPauseResume.setOnClickListener(this);
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
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_test_run;
    }

    @Override
    public void updateTimeView(String newTime) {
        liveTimeView.setText(newTime);
    }

    @Override
    public void onWorkoutResult(WorkoutData data) {
        workoutData = data;
        Logger.d(TAG, "onWorkoutResult:\n " + workoutData);
        liveDataContainer.setVisibility(View.GONE);
        runDataContainer.setVisibility(View.VISIBLE);

        String distance = String.format("%1$,.2f", (workoutData.getDistance() / 1000)) + " km";
        String avgSpeed = String.format("%1$,.2f", workoutData.getAvgSpeed() * 3.6) + " km/hr";
        String time = Utils.secondsToHHMMSS((int) workoutData.getElapsedTime());
        totalDistanceView.setText(distance);
        avgSpeedView.setText(avgSpeed);
        totalTimeView.setText(time);
        totalStepsView.setText(workoutData.getTotalSteps() + "");
        int size = (int) Utils.convertDpToPixel(getContext(), 300);
        Utils.setStaticGoogleMap(size, size, staticGoogleMapView, workoutData.getPoints());
    }

    @Override
    public void showUpdate(float speed, float distaneCovered, int elapsedTimeInSecs) {
        super.showUpdate(speed, distaneCovered, elapsedTimeInSecs);
        Logger.d(TAG, "showUpdate: speed = " + speed + ", distanceCovered = " + distaneCovered);
        if (isRunActive()) {
            String distance = Math.round(distaneCovered) + " m";
            String speedDecimal = String.format("%1$,.2f", speed * 3.6) + " km/hr";
            liveDistanceView.setText(distance);
            liveSpeedView.setText(speedDecimal);
        }
    }

    @Override
    public void showSteps(int stepsSoFar, int elapsedTimeInSecs) {
        super.showSteps(stepsSoFar, elapsedTimeInSecs);
        if (isRunActive()) {
            liveStepsView.setText(stepsSoFar + "");
        }
    }

    @Override
    protected void onEndRun() {
        startPauseResume.setText("START");
        liveDataContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onPauseRun() {
        startPauseResume.setText("RESUME");
    }

    @Override
    protected void onResumeRun() {
        startPauseResume.setText("PAUSE");
    }

    @Override
    protected void onBeginRun() {
        runDataContainer.setVisibility(View.GONE);
        liveDataContainer.setVisibility(View.VISIBLE);
        startPauseResume.setText("PAUSE");
        logsFile = null;
        liveDistanceView.setText("0.00 m");
        liveSpeedView.setText("0.0 km/hr");
        liveStepsView.setText("0");
    }

    @Override
    protected void onContinuedRun(boolean isPaused) {
        runDataContainer.setVisibility(View.GONE);
        liveDataContainer.setVisibility(View.VISIBLE);
        if(isPaused){
            startPauseResume.setText("RESUME");
        }else{
            startPauseResume.setText("PAUSE");
        }
        logsFile = null;
        liveDistanceView.setText("0.00 m");
        liveSpeedView.setText("0.0 km/hr");
        liveStepsView.setText("0");
    }

    @Override
    public void showErrorMessage(String text) {
        if (errorMessageView != null) {
            errorMessageView.setText(text);
        }
    }

    @Override
    public void refreshWorkoutData() {

    }

    @Override
    public void showStopDialog() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start_run:
                if (!isRunActive()) {
                    beginRun();
                } else {
                    if (isRunning()) {
                        pauseRun(true);
                    } else {
                        resumeRun();
                    }
                }
                break;

            case R.id.bt_end_run:
                endRun(true);
                break;

            case R.id.bt_capture_logs:
                logsFile = myActivity.writeLogsToFile(getContext());
                break;

            case R.id.bt_email_logs:
                if (logsFile != null) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    // The intent does not have a URI, so declare the "text/plain" MIME type
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"maheshwari.ankit.iitd@gmail.com"}); // recipients
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RFAC Logs " + new Date().toString());
                    emailIntent.putExtra(Intent.EXTRA_TEXT, textForMail());
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logsFile));
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } else {
                    Toast.makeText(getContext(), "No Logs", Toast.LENGTH_SHORT);
                }
                break;

            case R.id.bt_edit_config:
//                getFragmentController().replaceFragment(ConfigFragment.newInstance(), true);
                break;

            case R.id.iv_static_google_map:
                Logger.d(TAG, "onClick: iv_static_google_map");
                if (workoutData != null) {
                    getFragmentController().replaceFragment(RunPathFragment.newInstance(workoutData), true);
                }
                break;
        }
    }


    private String textForMail() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("\nApp Version : " + getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0).versionName);
        }catch (PackageManager.NameNotFoundException nnfe){
            nnfe.printStackTrace();
        }
        sb.append("\nTHRESHOLD_INTERVAL : " + Config.THRESHOLD_INTEVAL + " secs");
        sb.append("\nTHRESHOLD_ACCURACY : " + ClientConfig.getInstance().THRESHOLD_ACCURACY);
        sb.append("\nTHRESHOLD_FACTOR : " + ClientConfig.getInstance().THRESHOLD_FACTOR);
        sb.append("\nVIGILANCE_TIMER_INTERVAL : " + ClientConfig.getInstance().VIGILANCE_TIMER_INTERVAL + " milli secs");
        sb.append("\nVIGILANCE_START_THRESHOLD : " + (Config.VIGILANCE_START_THRESHOLD / 1000) + " secs");
        sb.append("\nUSAIN_BOLT_UPPER_SPEED_LIMIT : " + ClientConfig.getInstance().USAIN_BOLT_UPPER_SPEED_LIMIT * 3.6 + " km/hr");
        sb.append("\nLOWER_SPEED_LIMIT : " + Config.LOWER_SPEED_LIMIT * 3.6 + " km/hr");
        sb.append("\nMIN_STEPS_PER_SECOND_FACTOR : " + Config.MIN_STEPS_PER_SECOND_FACTOR);
        if (workoutData != null) {
            sb.append("\nWorkoutData:\n" + workoutData);
        }

        return sb.toString();
    }

}
