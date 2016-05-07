package com.sharesmile.share.rfac;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.fragments.ShareFragment;

/**
 * Created by ankitm on 22/04/16.
 */
public class RealRunFragment extends RunFragment{

    private static final String TAG = "RealRunFragment";

    TextView time;
    TextView distance;
    TextView impact;
    ProgressBar runProgressBar;
    Button pauseButton;
    Button stopButton;

    public static final float IMPACT_CONVERSION_FACTOR = 10.0f; // Rs per km

    public static RealRunFragment newInstance() {
        RealRunFragment fragment = new RealRunFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    protected void populateViews(View baseView) {
        time = (TextView) baseView.findViewById(R.id.tv_run_progress_timer);
        distance = (TextView) baseView.findViewById(R.id.tv_run_progress_distance);
        impact = (TextView) baseView.findViewById(R.id.tv_run_progress_impact);
        runProgressBar = (ProgressBar) baseView.findViewById(R.id.run_progress_bar);
        pauseButton = (Button) baseView.findViewById(R.id.btn_pause);
        stopButton = (Button) baseView.findViewById(R.id.btn_stop);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Will begin workout if not already active
        if (!isRunActive()){
            beginRun();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.run_progress;
    }

    @Override
    public void updateTimeView(String newTime) {
        time.setText(newTime);
    }

    @Override
    public void onWorkoutResult(WorkoutData data) {
        //Workout completed and results obtained, time to show the next Fragment
        if (isAttachedToActivity()){
            getFragmentController().replaceFragment(ShareFragment.newInstance(data), false);
        }
    }

    @Override
    public void showUpdate(float speed, float distanceCovered) {
        String distDecimal = String.format("%1$,.2f" , (distanceCovered / 1000) );
        distance.setText(distDecimal);
        int rupees = (int) (IMPACT_CONVERSION_FACTOR*distanceCovered);
        impact.setText(rupees);
    }

    @Override
    public void showSteps(int stepsSoFar) {
        // Nothing to do here
    }

    @Override
    protected void onEndRun() {
        // Will wait for workout result broadcast
    }

    @Override
    protected void onPauseRun() {
        pauseButton.setText(R.string.resume);
        runProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResumeRun() {
        pauseButton.setText(R.string.pause);
        runProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onBeginRun() {
        impact.setText("0");
        distance.setText("0.00");
    }

    @Override
    public void showErrorMessage(String text) {
        MainApplication.showToast(text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:
                if (isRunActive()) {
                    if (isRunning()) {
                        pauseRun(true);
                    } else {
                        resumeRun();
                    }
                }
                break;

            case R.id.btn_stop:
                showEndConfirmationDialog();

                //Dummy data for testing
                WorkoutDao wd = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
                Workout workout = new Workout();
                workout.setAvgSpeed(10);
                workout.setDistance(100);
                workout.setElapsedTime(120);
                workout.setRecordedTime(110);
                workout.setSteps(10000);
                wd.insertOrReplace(workout);

                break;
        }
    }

    private void showEndConfirmationDialog(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Finish Run");


        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to end the run?");

        // Setting Icon to Dialog

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                endRun(true);

            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                dialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
