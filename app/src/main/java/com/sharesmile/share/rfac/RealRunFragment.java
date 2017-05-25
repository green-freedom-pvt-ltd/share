package com.sharesmile.share.rfac;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.models.Calorie;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.fragments.ShareFragment;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by ankitm on 22/04/16.
 */
public class RealRunFragment extends RunFragment {

    private static final String TAG = "RealRunFragment";
    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";

    TextView time;
    TextView distance;
    TextView impact;
    ProgressBar runProgressBar;
    Button pauseButton;
    Button stopButton;

    @BindView(R.id.img_sponsor_logo)
    ImageView mSponsorLogo;

    @BindView(R.id.timer_indicator)
    TextView mTimerIndicator;

    @BindView(R.id.tv_calories_progress)
    TextView tvCalorieMets;

    @BindView(R.id.live_calories_container)
    View caloriesContainer;

    private CauseData mCauseData;


    public static RealRunFragment newInstance(CauseData causeData) {
        RealRunFragment fragment = new RealRunFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_CAUSE_DATA, causeData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        mCauseData = (CauseData) arg.getSerializable(BUNDLE_CAUSE_DATA);
        loadThankYouImage();
    }

    @Override
    protected void populateViews(View baseView) {
        ButterKnife.bind(this, baseView);
        time = (TextView) baseView.findViewById(R.id.tv_run_progress_timer);
        distance = (TextView) baseView.findViewById(R.id.tv_run_progress_distance);
        impact = (TextView) baseView.findViewById(R.id.tv_run_progress_impact);
        runProgressBar = (ProgressBar) baseView.findViewById(R.id.run_progress_bar);
        pauseButton = (Button) baseView.findViewById(R.id.btn_pause);
        stopButton = (Button) baseView.findViewById(R.id.btn_stop);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        if (MainApplication.getInstance().getUserDetails().getBodyWeight() > 0){
            caloriesContainer.setVisibility(View.VISIBLE);
        }
        Picasso.with(getContext()).load(mCauseData.getSponsor().getLogoUrl()).into(mSponsorLogo);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Will begin workout if not already active
        if (!myActivity.isWorkoutActive()) {
            beginRun();
        } else {
            continuedRun();
        }
        AnalyticsEvent.create(Event.ON_LOAD_TRACKER_SCREEN)
                .addBundle(mCauseData.getCauseBundle())
                .buildAndDispatch();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.run_progress;
    }

    @Override
    public void updateTimeView(String newTime) {
        time.setText(newTime);
        if (newTime.length() > 5) {
            mTimerIndicator.setText("HR:MIN:SEC");
        }
    }

    @Override
    public void onWorkoutResult(WorkoutData data) {
        //Workout completed and results obtained, time to show the next Fragment
        Logger.d(TAG, "onWorkoutResult");
        if (isAttachedToActivity()) {
            String distanceString = Utils.formatToKmsWithTwoDecimal(data.getDistance());
            Float fDistance = Float.parseFloat(distanceString);

            boolean isLogin = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN);
            if (data.isMockLocationDetected()){
                // Do nothing, DisableMock blocking popup is on display on the screen
                return;
            }

            if (data.getUsainBoltCount() >= 3){
                // Do nothing, ConsecutiveUsainBoltsForceExit blocking popup is on display
                return;
            }

            if (mCauseData.getMinDistance() > (fDistance * 1000)
                    && !data.isMockLocationDetected()) {
                myActivity.exit();
                stopTimer();
                return;
            }

            Boolean hasPreviousRun =SharedPrefsManager.getInstance().getBoolean(Constants.PREF_HAS_RUN, false);
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_FIRST_RUN_FEEDBACK, !hasPreviousRun);
            getFragmentController().replaceFragment(ShareFragment.newInstance(data, mCauseData, !isLogin), false);
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_HAS_RUN, true);

        }
    }

    @Override
    public void showUpdate(float speed, float distanceCovered, int elapsedTimeInSecs) {
        super.showUpdate(speed, distanceCovered, elapsedTimeInSecs);
        String distanceString = Utils.formatToKmsWithTwoDecimal(distanceCovered);
        distance.setText(distanceString);
        int rupees = Math.round(getConversionFactor() * Float.parseFloat(distanceString));
        impact.setText(String.valueOf(rupees));
        Calorie calorie = WorkoutSingleton.getInstance().getDataStore().getCalories();
        if (calorie != null){
            tvCalorieMets.setText(String.valueOf(Math.round(calorie.getCalories())));
        }
    }


    private String getImpactInRupees(float distanceCovered){
        String distanceString = Utils.formatToKmsWithTwoDecimal(distanceCovered);
        int rupees = Math.round(getConversionFactor() * Float.parseFloat(distanceString));
        return String.valueOf(rupees);
    }

    @Override
    public void showSteps(int stepsSoFar, int elapsedTimeInSecs) {
        super.showSteps(stepsSoFar, elapsedTimeInSecs);
    }

    @Override
    protected void onEndRun() {
        // Will wait for workout result broadcast
        Logger.d(TAG, "onEndRun");
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
        distance.setText("0.0");
    }

    @Override
    protected void onContinuedRun(boolean isPaused) {
        if (!isRunning()) {
            pauseButton.setText(R.string.resume);
            runProgressBar.setVisibility(View.INVISIBLE);
        } else {
            pauseButton.setText(R.string.pause);
            runProgressBar.setVisibility(View.VISIBLE);
        }

        float distanceCovered = WorkoutSingleton.getInstance().getDataStore().getDistanceCoveredSinceLastResume(); // in meters
        impact.setText(getImpactInRupees(distanceCovered));
        distance.setText(Utils.formatToKmsWithTwoDecimal(distanceCovered));

        if (WorkoutSingleton.getInstance().toShowEndRunDialog()){
            showRunEndDialog();
            WorkoutSingleton.getInstance().setToShowEndRunDialog(false);
        }
    }

    @Override
    public void showErrorMessage(String msg) {
        if (isAttachedToActivity()){
            showErrorDialog(msg);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:
                if (isRunActive()) {
                    if (isRunning()) {
                        pauseRun(true);
                        AnalyticsEvent.create(Event.ON_CLICK_PAUSE_RUN)
                                .addBundle(mCauseData.getCauseBundle())
                                .addBundle(getWorkoutBundle())
                                .buildAndDispatch();
                    } else {
                        resumeRun();
                        AnalyticsEvent.create(Event.ON_CLICK_RESUME_RUN)
                                .addBundle(mCauseData.getCauseBundle())
                                .addBundle(getWorkoutBundle())
                                .buildAndDispatch();
                    }
                }
                break;

            case R.id.btn_stop:
                showStopDialog();
                AnalyticsEvent.create(Event.ON_CLICK_STOP_RUN)
                        .addBundle(mCauseData.getCauseBundle())
                        .addBundle(getWorkoutBundle())
                        .buildAndDispatch();
                break;
        }
    }

    @Override
    public void showStopDialog() {
        String rDistance = distance.getText().toString();
        Float fDistance = Float.parseFloat(rDistance);
        if (mCauseData.getMinDistance() > (fDistance * 1000)) {
            showMinDistanceDialog();
        } else {
            showRunEndDialog();
        }
    }

    private void showRunEndDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Finish Run");
        alertDialog.setMessage("Are you sure you want to end the run?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isAttachedToActivity()){
                    endRun(true);
                }
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
        AnalyticsEvent.create(Event.ON_LOAD_FINISH_RUN_POPUP)
                .addBundle(mCauseData.getCauseBundle())
                .addBundle(getWorkoutBundle())
                .buildAndDispatch();
    }

    private void showMinDistanceDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getString(R.string.dialog_title_min_distance));
        alertDialog.setMessage(getString(R.string.dialog_msg_min_distance, mCauseData.getMinDistance()));
        alertDialog.setPositiveButton(getString(R.string.dialog_positive_button_min_distance), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        alertDialog.setNegativeButton(getString(R.string.dialog_negative_button_min_distance), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isAttachedToActivity()) {
                    endRun(true);
                }
            }
        });
        alertDialog.show();
        AnalyticsEvent.create(Event.ON_LOAD_TOO_SHORT_POPUP)
                .addBundle(mCauseData.getCauseBundle())
                .addBundle(getWorkoutBundle())
                .buildAndDispatch();
    }

    /*  Rs per km*/
    public float getConversionFactor() {
        return mCauseData.getConversionRate();
    }

    private void loadThankYouImage() {
        if (mCauseData != null) {
            Picasso.with(getActivity()).load(mCauseData.getCauseThankYouImage()).fetch();
        }
    }

    private void showErrorDialog(String msg) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater(null).inflate(R.layout.alert_dialog_title, null);
        view.setBackgroundColor(getResources().getColor(R.color.neon_red));
        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(getString(R.string.error));
        alertDialog.setCustomTitle(view);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(getString(R.string.resume), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isAttachedToActivity()){
                    resumeRun();
                }

            }
        });
        alertDialog.setNegativeButton(getString(R.string.stop), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isAttachedToActivity()){
                    endRun(true);
                }
            }
        });

        alertDialog.show();
    }

}
