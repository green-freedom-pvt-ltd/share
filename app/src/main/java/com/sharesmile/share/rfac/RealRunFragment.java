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
            getFragmentController().replaceFragment(ShareFragment.newInstance(data));
        }
    }

    @Override
    public void showUpdate(float speed, float distanceCovered) {
        String distDecimal = String.format("%1$,.2f" , (distanceCovered / 1000) );
        distance.setText(distDecimal);
    }

    @Override
    public void showSteps(int stepsSoFar) {
        // Nothing to do here
    }

    @Override
    protected void onEndRun() {

    }

    @Override
    protected void onPauseRun() {
        pauseButton.setText(R.string.resume);
    }

    @Override
    protected void onResumeRun() {
        pauseButton.setText(R.string.pause);
    }

    @Override
    protected void onBeginRun() {

    }

    @Override
    public void showErrorMessage(String text) {
        MainApplication.showToast(text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pause:
                pauseRun(true);
                break;

            case R.id.btn_stop:
                showEndConfirmationDialog();
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
