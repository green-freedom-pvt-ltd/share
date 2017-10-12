package com.sharesmile.share.rfac.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.Events.BodyWeightChangedEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.LoginImpl;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.activities.MainActivity;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.CauseImageData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sharesmile.share.core.Constants.SHARE_PLACEHOLDER_AMOUNT;
import static com.sharesmile.share.core.Constants.SHARE_PLACEHOLDER_DISTANCE;
import static com.sharesmile.share.core.Constants.SHARE_PLACEHOLDER_FIRST_NAME;
import static com.sharesmile.share.core.Constants.SHARE_PLACEHOLDER_PARTNER;
import static com.sharesmile.share.core.Constants.SHARE_PLACEHOLDER_SPONSOR;

/**
 * Created by Shine on 8/5/2016.
 */
public class ShareFragment extends FeedbackDialogHolderFragment implements View.OnClickListener, LoginImpl.LoginListener {

    public static final String WORKOUT_DATA = "workout_data";
    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private static final String TAG = "ShareFragment";
    private CauseData mCauseData;


    // Stats

    @BindView(R.id.tv_share_screen_title)
    TextView shareScreenTitle;

    @BindView(R.id.tv_impact_rupees)
    TextView impactInRupees;

    // Calories container

    @BindView(R.id.tv_calories_burned)
    TextView caloriesBurned;

    @BindView(R.id.share_cal_not_available_container)
    View caloriesNotAvailableContainer;

    @BindView(R.id.share_cal_available_container)
    View caloriesAvailableContainer;

    @BindView(R.id.tv_share_screen_calories_label)
    TextView caloriesLabel;

    // Distance
    @BindView(R.id.tv_share_distance_kms)
    TextView distance;

    @BindView(R.id.tv_share_screen_distance_label)
    TextView distanceLabel;

    // Duration
    @BindView(R.id.tv_share_duration)
    TextView durationInHHMMSS;

    @BindView(R.id.tv_duration_label)
    TextView durationLabel;


    // ThankYouImage
    @BindView(R.id.img_thank_you)
    ImageView thankYouImage;


    // Login Container

    @BindView(R.id.login_container)
    LinearLayout mLoginContainer;

    @BindView(R.id.btn_login_fb)
    LinearLayout mFbLoginButton;

    @BindView(R.id.btn_login_google)
    LinearLayout mGoogleLoginButton;

    @BindView(R.id.tv_welcome_skip)
    LinearLayout tv_skip;


    // Progress Container

    @BindView(R.id.progress_container)
    LinearLayout mProgressContainer;


    // Control Panel

    @BindView(R.id.control_panel)
    LinearLayout controlPanel;

    @BindView(R.id.btn_give_feedback)
    View giveFeedbackButton;

    @BindView(R.id.btn_share)
    View shareButton;

    @BindView(R.id.tv_share_skip)
    View shareSkipButton;

    // Sharable Content

    @BindView(R.id.sharable_content)
    LinearLayout sharableContent;


    private WorkoutData mWorkoutData;
    private boolean mShowLogin;

    private LoginImpl mLoginHandler;

    public static ShareFragment newInstance(WorkoutData data, CauseData causeData) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putParcelable(WORKOUT_DATA, data);
        args.putSerializable(BUNDLE_CAUSE_DATA, causeData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();

//        mCauseData = MainApplication.getInstance().getCausesToShow().get(2);
//        mWorkoutData = WorkoutDataImpl.getDummyWorkoutData();

        mCauseData = (CauseData) arg.getSerializable(BUNDLE_CAUSE_DATA);
        mWorkoutData = arg.getParcelable(WORKOUT_DATA);

        mShowLogin = !MainApplication.isLogin();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share, null);
        ButterKnife.bind(this, view);
        init();
        EventBus.getDefault().register(this);
        return view;
    }

    private void init() {
        if (mShowLogin) {
            thankYouImage.setVisibility(View.GONE);
            controlPanel.setVisibility(View.GONE);
            mLoginContainer.setVisibility(View.VISIBLE);
            initLogin();
        } else {
            thankYouImage.setVisibility(View.VISIBLE);
            controlPanel.setVisibility(View.VISIBLE);
            mLoginContainer.setVisibility(View.GONE);
        }
    }

    private void initLogin() {
        mLoginHandler = new LoginImpl(this, this);
        tv_skip.setOnClickListener(this);
        mFbLoginButton.setOnClickListener(this);
        mGoogleLoginButton.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        float distanceInMeters = mWorkoutData.getDistance();
        float elapsedTimeInSecs = mWorkoutData.getElapsedTime();

        Logger.d(TAG, "Elapsed Time in secs is " + elapsedTimeInSecs);

        int rupees = Utils.convertDistanceToRupees(mCauseData.getConversionRate(), distanceInMeters);
        impactInRupees.setText(getString(R.string.rs_symbol) + String.valueOf(rupees));
        initCaloriesContainer();
        String distanceCovered = Utils.formatToKmsWithTwoDecimal(distanceInMeters);
        distance.setText(distanceCovered);
        distanceLabel.setText(getString(R.string.distance));
        durationInHHMMSS.setText(Utils.secondsToHHMMSS(Math.round(elapsedTimeInSecs)));
        durationLabel.setText(getString(R.string.duration));
        initImageData();

        getFragmentController().hideToolbar();

        AnalyticsEvent.create(Event.ON_LOAD_SHARE_SCREEN)
                .addBundle(mWorkoutData.getWorkoutBundle())
                .put("num_spikes", mWorkoutData.getNumGpsSpikes())
                .put("bolt_count", mWorkoutData.getUsainBoltCount())
                .put("num_update_events", mWorkoutData.getNumUpdateEvents())
                .buildAndDispatch();

        if (WorkoutSingleton.getInstance().toShowFeedbackDialog()){
            // Need to show post run feedback dialog before
            showPostRunFeedbackDialog(mWorkoutData);
        }

    }

    private void initCaloriesContainer(){
        if (MainApplication.getInstance().getBodyWeight() > 0){
            double calories = mWorkoutData.getCalories().getCalories();
            caloriesNotAvailableContainer.setVisibility(View.GONE);
            caloriesAvailableContainer.setVisibility(View.VISIBLE);
            caloriesBurned.setText(String.valueOf(Math.round(calories)));
            caloriesLabel.setText(getString(R.string.calories_burned));
        }else {
            caloriesAvailableContainer.setVisibility(View.GONE);
            caloriesNotAvailableContainer.setVisibility(View.VISIBLE);
            caloriesLabel.setText(getString(R.string.for_calories));
            caloriesNotAvailableContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainApplication.isLogin()){
                        weightInputDialog = Utils.showWeightInputDialog(getActivity());
                    }else {
                        MainApplication.showToast("Please login to track Calories.");
                    }
                }
            });
        }
    }

    AlertDialog weightInputDialog;

    @Override
    protected void exitFeedback(WorkoutData workoutData) {
        // Do nothing
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (weightInputDialog != null && weightInputDialog.isShowing()){
            weightInputDialog.dismiss();
        }
        if (mLoginHandler != null){
            mLoginHandler.disconnect();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BodyWeightChangedEvent event) {
        Logger.d(TAG, "onEvent: BodyWeightChangedEvent");
        initCaloriesContainer();
    }

    private void initImageData(){

        CauseImageData causeImageData = mCauseData.getRandomCauseImageData();
        String titleTemplate = getString(R.string.thank_you);
        String imageUrl = null;

        if (causeImageData != null){
            titleTemplate = causeImageData.getTitleTemplate();

            imageUrl = causeImageData.getImage();
        }

        if (!MainApplication.isLogin()){
            shareScreenTitle.setText(getString(R.string.thank_you));
        }else {
            shareScreenTitle.setText(replacePlaceHolders(titleTemplate));
        }

        ShareImageLoader.getInstance().loadImage(imageUrl, thankYouImage,
                ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_welcome_skip:
                showLoginSkipDialog();
                break;
            case R.id.btn_login_fb:
                // performFbLogin();
                mLoginHandler.performFbLogin();
                break;
            case R.id.btn_login_google:
                mLoginHandler.performGoogleLogin();
                break;
            default:
        }
    }

    @OnClick(R.id.btn_give_feedback)
    public void onGiveFeedbackClick(){
        AnalyticsEvent.create(Event.ON_CLICK_GIVE_FEEDBACK_BTN)
                .addBundle(mWorkoutData.getWorkoutBundle())
                .put("num_spikes", mWorkoutData.getNumGpsSpikes())
                .put("bolt_count", mWorkoutData.getUsainBoltCount())
                .put("num_update_events", mWorkoutData.getNumUpdateEvents())
                .buildAndDispatch();
        showPostRunFeedbackDialog(mWorkoutData);
    }

    @OnClick(R.id.btn_share)
    public void onShareButtonClick(){
        // Open Share picker with sharable image and custom message
        Bitmap toShare = Utils.getBitmapFromLiveView(sharableContent);
        Utils.share(getContext(), Utils.getLocalBitmapUri(toShare, getContext()), getShareMsg());
        AnalyticsEvent.create(Event.ON_CLICK_WORKOUT_SHARE)
                .addBundle(mWorkoutData.getWorkoutBundle())
                .put("num_spikes", mWorkoutData.getNumGpsSpikes())
                .put("bolt_count", mWorkoutData.getUsainBoltCount())
                .put("num_update_events", mWorkoutData.getNumUpdateEvents())
                .buildAndDispatch();
        // Not exiting ShareActivity
//        if (getActivity() != null){
//            getActivity().finish();
//        }
    }

    @OnClick(R.id.tv_share_skip)
    public void onSkipClick(){
        openHomeActivityAndFinish();
    }

    private void openHomeActivityAndFinish(){
        if (getActivity() != null){

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Constants.BUNDLE_SHOW_RUN_STATS, true);
            startActivity(intent);

            getActivity().finish();
        }
    }

    private void showLoginSkipDialog() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getString(R.string.skip_login_title));
        alertDialog.setMessage(getString(R.string.login_skip_msg));
        alertDialog.setPositiveButton(getString(R.string.yes_sure), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.login), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }

    /*
    *
    * "Ran <distance> km. and raised Rs. <amount> for <partner_ngo> on ImpactRun. Kudos <sponsor_company> for sponsoring my run. #impactrun"*/
    private String getShareMsg() {
        String msg = mCauseData.getCauseShareMessageTemplate();
        return replacePlaceHolders(msg);
    }

    private String replacePlaceHolders(String msg){
        if (msg.contains(SHARE_PLACEHOLDER_FIRST_NAME)){
            if (MainApplication.isLogin() && !TextUtils.isEmpty(MainApplication.getInstance().getUserDetails().getFirstName())){
                msg = msg.replaceAll(SHARE_PLACEHOLDER_FIRST_NAME,
                        MainApplication.getInstance().getUserDetails().getFirstName());
            }else {
                msg = msg.replaceAll(SHARE_PLACEHOLDER_FIRST_NAME, "");
            }
        }
        if (msg.contains(SHARE_PLACEHOLDER_DISTANCE)) {
            msg = msg.replaceAll(SHARE_PLACEHOLDER_DISTANCE,
                    Utils.formatToKmsWithTwoDecimal(mWorkoutData.getDistance()));
        }
        if (msg.contains(SHARE_PLACEHOLDER_AMOUNT)) {
            int rs = Utils.convertDistanceToRupees(mCauseData.getConversionRate(), mWorkoutData.getDistance());
            msg = msg.replaceAll(SHARE_PLACEHOLDER_AMOUNT, String.valueOf(rs));
        }
        if (msg.contains(SHARE_PLACEHOLDER_SPONSOR)) {
            msg = msg.replaceAll(SHARE_PLACEHOLDER_SPONSOR, mCauseData.getSponsor().getName());
        }
        if (msg.contains(SHARE_PLACEHOLDER_PARTNER)) {
            msg = msg.replaceAll(SHARE_PLACEHOLDER_PARTNER, mCauseData.getExecutor().getPartnerNgo());
        }
        return msg;
    }

    @Override
    public void onLoginSuccess() {
        if (isAttachedToActivity()){
            getFragmentController().replaceFragment(ShareFragment.newInstance(mWorkoutData, mCauseData), false);
        }
    }

    @Override
    public void showHideProgress(boolean show, String title) {
        if (isAttachedToActivity()){
            if (show) {
                mLoginContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.VISIBLE);
            } else {
                mLoginContainer.setVisibility(View.VISIBLE);
                mProgressContainer.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mLoginHandler != null){
            mLoginHandler.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}



