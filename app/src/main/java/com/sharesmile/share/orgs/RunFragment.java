package com.sharesmile.share.orgs;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.MainActivity;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;

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

    MainActivity myActivity;
    View baseView;
    LinearLayout runDataContainer;
    LinearLayout liveDataContainer;

    TextView totalDistanceView, avgSpeedView, totalTimeView;
    TextView liveDistanceView, liveSpeedView, liveTimeView;

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
        totalTimeView = (TextView) baseView.findViewById(R.id.tv_total_time);

        liveDataContainer = (LinearLayout) baseView.findViewById(R.id.live_data_container);
        liveDistanceView = (TextView) baseView.findViewById(R.id.tv_live_distance);
        liveSpeedView = (TextView) baseView.findViewById(R.id.tv_live_speed);
        liveTimeView = (TextView) baseView.findViewById(R.id.tv_live_time);

        return baseView;
    }

    public void showRunData(WorkoutData workoutData){
        Logger.d(TAG, "showRunData:\n " + workoutData);
        liveDataContainer.setVisibility(View.GONE);
        runDataContainer.setVisibility(View.VISIBLE);

        totalDistanceView.setText(workoutData.getDistance() + " m");
        avgSpeedView.setText(workoutData.getAvgSpeed() * (18/5) + " km/hr");
        totalTimeView.setText((workoutData.getTime() / 60) + " mins");
    }

    CountDownTimer newtimer = new CountDownTimer(1000000000, 1000) {

        public void onTick(long millisUntilFinished) {
            long elapsed = System.currentTimeMillis() - runStartTime;
            int secs = (int)(elapsed / 1000);
            int mins = secs / 60;
            int remainSecs = secs % 60;
            String time = mins+":"+remainSecs;
            liveTimeView.setText(time);
        }
        public void onFinish() {

        }
    };


    public void showUpdate(float speed, float distaneCovered){
        Logger.d(TAG, "showUpdate: speed = " + speed + ", distanceCovered = " + distaneCovered);
        if (isRunActive){
            liveDistanceView.setText(distaneCovered + " m");
            liveSpeedView.setText(speed * (18/5) + " km/hr");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start_run:
                myActivity.beginLocationTracking();
                runDataContainer.setVisibility(View.GONE);
                liveDataContainer.setVisibility(View.VISIBLE);
                logsFile = null;
                isRunActive = true;
                runStartTime = System.currentTimeMillis();
                newtimer.start();
                liveDistanceView.setText("0 m");
                liveSpeedView.setText("0 km/hr");
                break;

            case R.id.bt_end_run:
                myActivity.endLocationTracking();
                isRunActive = false;
                runStartTime = 0;
                newtimer.cancel();
                // Wait for total data to show up
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
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "PFA the logs for my Workout Session");
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logsFile));
                    startActivity(Intent.createChooser(emailIntent , "Send email..."));
                }else{
                    Toast.makeText(getContext(), "No Logs", Toast.LENGTH_SHORT);
                }
                break;
        }

    }
}
