package com.sharesmile.share.tracking.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.IFragmentController;
import com.sharesmile.share.core.cause.model.CauseData;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;
import com.sharesmile.share.tracking.models.Calorie;
import com.sharesmile.share.tracking.models.WorkoutData;
import com.sharesmile.share.tracking.share.ShareFragment;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by ankitm on 22/04/16.
 */
public class RealRunFragment extends RunFragment {

    private static final String TAG = "RealRunFragment";
    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";

    TextView time;
    TextView distanceTextView;
    TextView distanceUnitTextView;
    TextView impact;
    View pauseButton;
    Button stopButton;

    @BindView(R.id.img_sponsor_logo)
    ImageView mSponsorLogo;

    @BindView(R.id.timer_indicator)
    TextView mTimerIndicator;

    @BindView(R.id.tv_calories_progress)
    TextView tvCalorieMets;

    @BindView(R.id.live_calories_container)
    RelativeLayout caloriesContainer;

    @BindView(R.id.live_distance_container)
    RelativeLayout distanceContainer;

    @BindView(R.id.btn_music_hook)
    View musicHookButton;

    @BindView(R.id.live_timer_container)
    View timerContainer;

    @BindView(R.id.iv_pause_resume)
    ImageView pauseResumeIcon;

    @BindView(R.id.tv_pause_resume)
    TextView pauseResumeTextView;

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
    }

    @Override
    protected void populateViews(View baseView) {
        ButterKnife.bind(this, baseView);
        time = baseView.findViewById(R.id.tv_run_progress_timer);
        distanceTextView = baseView.findViewById(R.id.tv_run_progress_distance);
        distanceUnitTextView = baseView.findViewById(R.id.tv_run_progress_distance_unit);
        impact = baseView.findViewById(R.id.tv_run_progress_impact);
        pauseButton = baseView.findViewById(R.id.btn_pause);
        stopButton = baseView.findViewById(R.id.btn_stop);
        pauseButton.setOnClickListener(this);

        pauseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setAlpha(0.5f);
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        v.setAlpha(1f);
                        break;
                    }
                }
                return false;
            }
        });

        stopButton.setOnClickListener(this);
        musicHookButton.setOnClickListener(this);

        if (MainApplication.getInstance().getBodyWeight() <= 0){
            // Need to hide caloriesContainer and reset distanceContainer LayoutParams
            caloriesContainer.setVisibility(View.GONE);
            distanceContainer.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) distanceContainer.getLayoutParams();
            LinearLayout.LayoutParams timerParams = (LinearLayout.LayoutParams) timerContainer.getLayoutParams();
            params.topMargin = timerParams.topMargin;
            params.weight = 4;
            distanceContainer.setLayoutParams(params);
        }else {
            // Need to show caloriesContainer and set distanceContainer LayoutParams
            caloriesContainer.setVisibility(View.VISIBLE);
            distanceContainer.setGravity(Gravity.LEFT);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) distanceContainer.getLayoutParams();
            params.topMargin = 0;
            params.weight = 3.2f;
            distanceContainer.setLayoutParams(params);
        }

        String distanceLabel = getString(R.string.distance_km, UnitsManager.getDistanceLabel().toUpperCase());
        distanceUnitTextView.setText(distanceLabel);

        ShareImageLoader.getInstance().loadImage(mCauseData.getSponsor().getLogoUrl(), mSponsorLogo);

//        int height = (int) getResources().getDimension(R.dimen.super_duper_large_text);
//        Shader textShader=new LinearGradient(0, 0, 0, height, new int[]{0xff00bef5,0xff00ff55},
//                new float[]{0, 1}, Shader.TileMode.CLAMP);
//        impact.getPaint().setShader(textShader);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Logger.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        // Will begin workout if not already active
        if (!myActivity.isWorkoutActive()) {
            beginRun();
        } else {
            continuedRun();
        }
        AnalyticsEvent.create(Event.ON_LOAD_TRACKER_SCREEN)
                .buildAndDispatch();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.run_progress;
    }

    @Override
    public void updateTimeView(String newTime) {
        if (isVisible()) {
            time.setText(newTime);
            if (newTime.length() > 5) {
                mTimerIndicator.setText("HR:MIN:SEC");
            }
        }
    }

    @Override
    public void onWorkoutResult(WorkoutData data, AchievedBadgesData achievedBadgesData) {
        //Workout completed and results obtained, time to show the next Fragment
        Logger.d(TAG, "onWorkoutResult");
        if (isAttachedToActivity()) {
            if (data.isMockLocationDetected()){
                // Do nothing, DisableMock blocking popup is on display on the screen
                return;
            }

            if (data.hasConsecutiveUsainBolts()){
                // Do nothing, ConsecutiveUsainBoltsForceExit blocking popup is on display
                return;
            }

            if (data.isAutoFlagged()){
                // Do nothing, Auto flag popup is on display
                return;
            }

            exitRun(data,achievedBadgesData);

        }
    }

    protected void exitRun(WorkoutData data, AchievedBadgesData achievedBadgesData){
        Logger.d(TAG, "exit");
        if (mCauseData.getMinDistance() > (data.getDistance())
                && !data.isMockLocationDetected()) {
            myActivity.exit();
            stopTimer();
            return;
        }
        Loader statsLoader= getActivity().getLoaderManager().getLoader(Constants.LOADER_MY_STATS_GRAPH);
        if(statsLoader!=null)
            statsLoader.onContentChanged();
        Loader  loader = getActivity().getLoaderManager().getLoader(Constants.LOADER_CHARITY_OVERVIEW);
        if(loader!=null)
            loader.onContentChanged();
        getFragmentController().replaceFragment(ShareFragment.newInstance(data, mCauseData,achievedBadgesData), false);
    }

    @Override
    public void showUpdate(float speed, float distanceCoveredMeters, int elapsedTimeInSecs) {
        super.showUpdate(speed, distanceCoveredMeters, elapsedTimeInSecs);
        if (isVisible()) {
            float distanceOnDisplayInMeters = 0f;
            try {
                float numberOnDisplay = Float.parseFloat(distanceTextView.getText().toString());
                distanceOnDisplayInMeters = UnitsManager.isImperial() ? 1609.34f * numberOnDisplay
                        : 1000 * numberOnDisplay;
            } catch (NumberFormatException nfe) {
                String message = "NumberFormatException while parsing distanceTextView on display: " + nfe.getMessage();
                Logger.e(TAG, message);
                Crashlytics.log(message);
                nfe.printStackTrace();
            }
            if (distanceCoveredMeters < distanceOnDisplayInMeters || distanceCoveredMeters - distanceOnDisplayInMeters >= 1) {
                // Only when the delta is greater than 0.001 km we show the update
                String distanceString = UnitsManager.formatToMyDistanceUnitWithTwoDecimal(distanceCoveredMeters);
                distanceTextView.setText(distanceString);
                int rupees = Utils.convertDistanceToRupees(getConversionFactor(), distanceCoveredMeters);
                impact.setText(UnitsManager.formatRupeeToMyCurrency(rupees));
                setCaloriesInTextView();
            }
        }
    }

    private void setCaloriesInTextView(){
        if (WorkoutSingleton.getInstance().getDataStore() != null){
            Calorie calorie = WorkoutSingleton.getInstance().getDataStore().getCalories();
            if (calorie != null){
                String caloriesString = "";
                if (calorie.getCalories() > 100){
                    caloriesString = String.valueOf(Math.round(calorie.getCalories()));
                }else {
                    caloriesString = Utils.formatWithOneDecimal(calorie.getCalories());
                }
                tvCalorieMets.setText(caloriesString);
            }
        }else {
            tvCalorieMets.setText("0");
        }
    }

    @Override
    public void refreshWorkoutData(){
        Logger.d(TAG, "refreshWorkoutData");
        if (isAttachedToActivity()){
            updateTimeView(Utils.secondsToHHMMSS((int) WorkoutSingleton.getInstance().getElapsedTimeInSecs(),false));
            float totalDistance = WorkoutSingleton.getInstance().getTotalDistanceInMeters();
            String distanceString = UnitsManager.formatToMyDistanceUnitWithTwoDecimal(totalDistance);
            distanceTextView.setText(distanceString);
            int rupees = Utils.convertDistanceToRupees(getConversionFactor(), totalDistance);
            impact.setText(UnitsManager.formatRupeeToMyCurrency(rupees));
            setCaloriesInTextView();
        }
    }

    @Override
    public void showSteps(int stepsSoFar, int elapsedTimeInSecs) {
        super.showSteps(stepsSoFar, elapsedTimeInSecs);
//        MainApplication.showToast("Steps: " + stepsSoFar);
    }

    @Override
    protected void onEndRun() {
        // Will wait for workout result broadcast
        Logger.d(TAG, "onEndRun");
    }

    @Override
    protected void onPauseRun() {
        setPauseResumeButton(true);
    }

    @Override
    protected void onResumeRun() {
        setPauseResumeButton(false);
    }

    private void setPauseResumeButton(boolean paused){
        if (isVisible()) {
            if (paused) {
                pauseResumeTextView.setText(R.string.resume);
                pauseResumeIcon.setImageResource(R.drawable.ic_play_arrow_black_50_24px);
                impact.setTextColor(ContextCompat.getColor(getContext(), R.color.black_38));
            } else {
                pauseResumeTextView.setText(R.string.pause);
                pauseResumeIcon.setImageResource(R.drawable.ic_pause_black_50_24px);
                impact.setTextColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue));
            }
        }
    }

    @Override
    protected void onBeginRun() {
        impact.setText(UnitsManager.formatRupeeToMyCurrency(0));
        distanceTextView.setText("0.00");
        tvCalorieMets.setText("0.0");
    }

    @Override
    protected void onContinuedRun(boolean isPaused) {
        Logger.d(TAG, "onContinuedRun , isPaused = " + isPaused);
        if (!isRunning()) {
            setPauseResumeButton(true);
        } else {
            setPauseResumeButton(false);
        }

        float distanceCovered = WorkoutSingleton.getInstance().getTotalDistanceInMeters(); // in meters
        int rupees = Utils.convertDistanceToRupees(getConversionFactor(), distanceCovered);
        impact.setText(UnitsManager.formatRupeeToMyCurrency(rupees));
        distanceTextView.setText(UnitsManager.formatToMyDistanceUnitWithTwoDecimal(distanceCovered));
        setCaloriesInTextView();

        if (WorkoutSingleton.getInstance().toShowEndRunDialog()){
            showStopDialog();
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
                                .addBundle(getWorkoutBundle())
                                .buildAndDispatch();
                    } else {
                        resumeRun();
                        AnalyticsEvent.create(Event.ON_CLICK_RESUME_RUN)
                                .addBundle(getWorkoutBundle())
                                .buildAndDispatch();
                    }
                }
                break;

            case R.id.btn_stop:
                AnalyticsEvent.create(Event.ON_CLICK_STOP_RUN)
                        .addBundle(getWorkoutBundle())
                        .buildAndDispatch();
                showStopDialog();
                break;
            case R.id.btn_music_hook:
                getFragmentController().performOperation(IFragmentController.OPEN_MUSIC_PLAYER, null);
                AnalyticsEvent.create(Event.ON_CLICK_MUSIC_BUTTON)
                        .buildAndDispatch();
                break;
        }
    }

    @Override
    public void showStopDialog() {
        if (mCauseData != null) {
            float totalDistance = WorkoutSingleton.getInstance().getTotalDistanceInMeters();
            if (mCauseData.getMinDistance() > (totalDistance)) {
                showMinDistanceDialog();
            } else {
                showRunEndDialog();
            }
        }
    }

    private void showRunEndDialog() {
        if (isAttachedToActivity() && !getActivity().isFinishing()){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(getString(R.string.finish_workout));
            alertDialog.setMessage(getString(R.string.finish_workout_message));
            alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isAttachedToActivity()){
                        endRun(true);
                    }
                }
            });

            alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            AnalyticsEvent.create(Event.ON_LOAD_FINISH_RUN_POPUP)
                    .addBundle(getWorkoutBundle())
                    .buildAndDispatch();
        }
    }

    private void showMinDistanceDialog() {
        if (isAttachedToActivity() && !getActivity().isFinishing()){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(getString(R.string.dialog_title_min_distance));
            int minDistance = (int) (UnitsManager.isImperial() ? 3.28f*mCauseData.getMinDistance() : mCauseData.getMinDistance());
            String unit = UnitsManager.isImperial() ? "ft" : "m";
            alertDialog.setMessage(getString(R.string.dialog_msg_min_distance, minDistance, unit));
            alertDialog.setNegativeButton(getString(R.string.dialog_positive_button_min_distance), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });

            alertDialog.setPositiveButton(getString(R.string.dialog_negative_button_min_distance), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isAttachedToActivity()) {
                        endRun(true);
                    }
                }
            });
            alertDialog.show();
            AnalyticsEvent.create(Event.ON_LOAD_TOO_SHORT_POPUP)
                    .addBundle(getWorkoutBundle())
                    .buildAndDispatch();
        }
    }

    /*  Rs per km*/
    public float getConversionFactor() {
        return mCauseData.getConversionRate();
    }

    private void showErrorDialog(String msg) {
        if (isAttachedToActivity() && !getActivity().isFinishing()){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            View view = ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.alert_dialog_title, null);
            view.setBackgroundColor(getResources().getColor(R.color.neon_red));
            TextView titleView = (TextView) view.findViewById(R.id.title);
            titleView.setText(getString(R.string.something_not_right));
            alertDialog.setCustomTitle(view);
            alertDialog.setMessage(msg);
            alertDialog.setNegativeButton(getString(R.string.resume), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isAttachedToActivity()){
                        resumeRun();
                    }
                }
            });
            alertDialog.setPositiveButton(getString(R.string.finish), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isAttachedToActivity()){
                        endRun(true);
                    }
                }
            });
            if(isVisible())
            alertDialog.show();
        }
    }

    @Override
    protected boolean handleBackPress() {
        Logger.d(TAG, "handleBackPress");
        AnalyticsEvent.create(Event.ON_CLICK_BACK_ON_TRACKER_SCREEN).buildAndDispatch();
        return super.handleBackPress();
    }
}
