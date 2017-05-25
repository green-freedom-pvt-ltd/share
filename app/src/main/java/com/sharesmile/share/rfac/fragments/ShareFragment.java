package com.sharesmile.share.rfac.fragments;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.plus.PlusShare;
import com.sharesmile.share.Events.BodyWeightChangedEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.core.LoginImpl;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shine on 8/5/2016.
 */
public class ShareFragment extends BaseFragment implements View.OnClickListener, LoginImpl.LoginListener {

    public static final String WORKOUT_DATA = "workout_data";
    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private static final String TAG = "ShareFragment";
    private static final String BUNDLE_SHOW_LOGIN = "bundle_show_login";
    private static final int REQUEST_SHARE = 101;
    private CauseData mCauseData;


    @BindView(R.id.skip_layout)
    LinearLayout mSkipLayout;

    @BindView(R.id.btn_share_screen)
    Button mShareButton;

    @BindView(R.id.tv_share_screen_rupee)
    TextView mContributionAmount;

    @BindView(R.id.tv_share_screen_distance)
    TextView mDistance;

    @BindView(R.id.tv_share_screen_time)
    TextView mTime;

    @BindView(R.id.share_layout)
    RadioGroup mShareGroup;

    @BindView(R.id.content)
    RelativeLayout mContentView;

    @BindView(R.id.share_container)
    RelativeLayout mShare_container;

    @BindView(R.id.login_container)
    LinearLayout mLoginContainer;

    private WorkoutData mWorkoutData;
    private boolean mShowLogin;

    @BindView(R.id.btn_login_fb)
    LinearLayout mFbLoginButton;

    @BindView(R.id.btn_login_google)
    LinearLayout mGoogleLoginButton;

    @BindView(R.id.tv_welcome_skip)
    LinearLayout tv_skip;

    @BindView(R.id.progress_container)
    LinearLayout mProgressContainer;

    @BindView(R.id.share_cal_not_available_container)
    View caloriesNotAvailableContainer;

    @BindView(R.id.img_share_calories)
    View caloriesIcon;

    @BindView(R.id.tv_share_screen_calories)
    TextView caloriesText;

    private LoginImpl mLoginHandler;

    public enum SHARE_MEDIUM {
        FB,
        GOOGLE,
        WHATS_APP,
        TWITTER,
    }

    private SHARE_MEDIUM mSelectedShareMedium = SHARE_MEDIUM.FB;

    public static ShareFragment newInstance(WorkoutData data, CauseData causeData, boolean showLoginScreen) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putParcelable(WORKOUT_DATA, data);
        args.putSerializable(BUNDLE_CAUSE_DATA, causeData);
        args.putBoolean(BUNDLE_SHOW_LOGIN, showLoginScreen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        mCauseData = (CauseData) arg.getSerializable(BUNDLE_CAUSE_DATA);
        mWorkoutData = arg.getParcelable(WORKOUT_DATA);
        mShowLogin = arg.getBoolean(BUNDLE_SHOW_LOGIN);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_share, null);
        ButterKnife.bind(this, view);
        init();
        Picasso.with(getContext()).load(R.drawable.share_background).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mContentView.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void init() {
        if (mShowLogin) {
            mShare_container.setVisibility(View.GONE);
            mLoginContainer.setVisibility(View.VISIBLE);
            initLogin();
        } else {
            mShare_container.setVisibility(View.VISIBLE);
            mLoginContainer.setVisibility(View.GONE);
        }
    }

    private void initLogin() {
        mLoginHandler = new LoginImpl(this, this);
        tv_skip.setOnClickListener(this);
        mFbLoginButton.setOnClickListener(this);
        mGoogleLoginButton.setOnClickListener(this);

        //init fb login
        TextView mFbText = (TextView) mFbLoginButton.findViewById(R.id.title);
        mFbText.setText(getString(R.string.logn_with_fb));
        mFbText.setTextColor(getResources().getColor(R.color.denim_blue));

        ImageView mFbImage = (ImageView) mFbLoginButton.findViewById(R.id.login_image);
        mFbImage.setImageResource(R.drawable.logo_fb);


        //init Google login
        TextView mGText = (TextView) mGoogleLoginButton.findViewById(R.id.title);
        mGText.setText(getString(R.string.logn_with_google));
        mGText.setTextColor(getResources().getColor(R.color.pale_red));

        ImageView mGImage = (ImageView) mGoogleLoginButton.findViewById(R.id.login_image);
        mGImage.setImageResource(R.drawable.login_google);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        float distanceInMeters = mWorkoutData.getDistance();
        float elapsedTimeInSecs = mWorkoutData.getElapsedTime();

        String distanceCovered = Utils.formatToKmsWithTwoDecimal(distanceInMeters);
        mDistance.setText(distanceCovered + " km");

        int rupees = Math.round(mCauseData.getConversionRate() * Float.valueOf(distanceCovered));
        mContributionAmount.setText(getString(R.string.rs_symbol) +" "+ String.valueOf(rupees));
        initCaloriesContainer();
        mTime.setText(Utils.secondsToHoursAndMins(Math.round(elapsedTimeInSecs)));

        initShareLayout();
        AnalyticsEvent.create(Event.ON_LOAD_SHARE_SCREEN)
                .addBundle(mWorkoutData.getWorkoutBundle())
                .buildAndDispatch();
    }

    private void initCaloriesContainer(){
        if (MainApplication.getInstance().getUserDetails().getBodyWeight() > 0){
            double caloriesBurned = mWorkoutData.getCalories().getCalories();
            caloriesNotAvailableContainer.setVisibility(View.GONE);
            caloriesIcon.setVisibility(View.VISIBLE);
            String caloriesString = Utils.formatWithOneDecimal(caloriesBurned);
            if ("0.0".equals(caloriesString)){
                caloriesText.setText("--");
            }else {
                caloriesText.setText(caloriesString + " Cal");
            }
        }else {
            caloriesNotAvailableContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.showWeightInputDialog(getContext());
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BodyWeightChangedEvent event) {
        Logger.d(TAG, "onEvent: BodyWeightChangedEvent");
        initCaloriesContainer();
        MainApplication.showToast("Will track calories for future walks/runs.");
    }

    private void initShareLayout() {
        mShareButton.setOnClickListener(this);
        mSkipLayout.setOnClickListener(this);

        mShareGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.fb:
                        mSelectedShareMedium = SHARE_MEDIUM.FB;
                        break;
                    case R.id.google:
                        mSelectedShareMedium = SHARE_MEDIUM.GOOGLE;
                        break;
                    case R.id.whatsapp:
                        mSelectedShareMedium = SHARE_MEDIUM.WHATS_APP;
                        break;
                    case R.id.twitter:
                        mSelectedShareMedium = SHARE_MEDIUM.TWITTER;
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.skip_layout:
                showThankYouFragment();
                break;
            case R.id.btn_share_screen:
                if (mSelectedShareMedium == SHARE_MEDIUM.WHATS_APP) {
                    shareOnWhatsApp();
                } else if (mSelectedShareMedium == SHARE_MEDIUM.GOOGLE) {
                    shareOnGooglePlus();
                } else if (mSelectedShareMedium == SHARE_MEDIUM.TWITTER) {
                    shareOnTwitter();
                } else {
                    shareOnFb();
                }
                break;
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

    private void showThankYouFragment() {
        getFragmentController().performOperation(IFragmentController.SAY_THANK_YOU, mCauseData.getCauseThankYouImage());
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

    private void shareOnGooglePlus() {
        Intent shareIntent = new PlusShare.Builder(getActivity())
                .setType("text/plain")
                .setText(getShareMsg())
                .getIntent();
        startActivityForResult(shareIntent, REQUEST_SHARE);
    }

    private void shareOnWhatsApp() {

        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getShareMsg());
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivityForResult(sendIntent, REQUEST_SHARE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Whats app not installed ", Toast.LENGTH_LONG).show();
        }

    }

    private void shareOnTwitter() {

        TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                .text(getShareMsg());
        startActivityForResult(builder.createIntent(), REQUEST_SHARE);
        //  builder.show();
    }

    private void shareOnFb() {

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(getString(R.string.url_play_store_with_utm)))
                .setContentTitle(getString(R.string.app_name))
                .setContentDescription(getShareMsg())
                .setImageUrl(Uri.parse(mCauseData.getCauseThankYouImage()))
                .build();


        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(new CallbackManagerImpl(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                //Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                //  Toast.makeText(getActivity(),"Failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {

            }
        }, REQUEST_SHARE);
        shareDialog.show(content);
    }

    private String SHARE_PLACEHOLDER_DISTANCE = "<distance>";
    private String SHARE_PLACEHOLDER_AMOUNT = "<amount>";
    private String SHARE_PLACEHOLDER_SPONSOR = "<sponsor_company>";
    private String SHARE_PLACEHOLDER_PARTNER = "<partner_ngo>";

    /*
    *
    * "Ran <distance> km. and raised Rs. <amount> for <partner_ngo> on ImpactRun. Kudos <sponsor_company> for sponsoring my run. #impactrun #nowisthetime"*/
    private String getShareMsg() {
        String msg = mCauseData.getCauseShareMessageTemplate();

        if (msg.contains(SHARE_PLACEHOLDER_DISTANCE)) {
            msg = msg.replaceAll(SHARE_PLACEHOLDER_DISTANCE,
                    Utils.formatToKmsWithTwoDecimal(mWorkoutData.getDistance()));
        }

        if (msg.contains(SHARE_PLACEHOLDER_AMOUNT)) {
            String rDistance = Utils.formatToKmsWithTwoDecimal(mWorkoutData.getDistance());
            Float fDistance = Float.parseFloat(rDistance);
            int rs = Math.round(fDistance * mCauseData.getConversionRate());
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

    private String getTimeInHHMMFormat(long millis) {

        int secs = (int) (millis / 1000);
        if (secs >= 3600) {
            return String.format("%02dhr %02dmin", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1));
        } else {
            return String.format("%02dmin",
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1));

        }
    }

    @Override
    public void onLoginSuccess() {
        getFragmentController().replaceFragment(ShareFragment.newInstance(mWorkoutData, mCauseData, false), false);
    }

    @Override
    public void showHideProgress(boolean show, String title) {
        if (show) {
            mLoginContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.VISIBLE);
        } else {
            mLoginContainer.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SHARE) {
            showThankYouFragment();
        } else {
            mLoginHandler.onActivityResult(requestCode, resultCode, data);
        }
    }
}



