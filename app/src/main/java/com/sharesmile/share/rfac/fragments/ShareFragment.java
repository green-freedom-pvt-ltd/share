package com.sharesmile.share.rfac.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.models.CauseData;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shine on 8/5/2016.
 */
public class ShareFragment extends BaseFragment implements View.OnClickListener {

    public static final String WORKOUT_DATA = "workout_data";
    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private static final String TAG = "ShareFragment";
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

    private WorkoutData mWorkoutData;

    public enum SHARE_MEDIUM {
        FB,
        GOOGLE,
        WHATS_APP,
        TWITTER,
    }

    private SHARE_MEDIUM mSelectedShareMedium = SHARE_MEDIUM.FB;

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
        mCauseData = (CauseData) arg.getSerializable(BUNDLE_CAUSE_DATA);
        mWorkoutData = (WorkoutData) arg.getParcelable(WORKOUT_DATA);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share, null);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        String distanceCovered = String.format("%1$,.2f", (mWorkoutData.getDistance() / 1000));
        mDistance.setText(distanceCovered + "km");
        int rupees = (int) (mCauseData.getConversionRate() * mWorkoutData.getDistance());
        mContributionAmount.setText(rupees + " Rs");
        mTime.setText(getTimeInHHMMFormat((int) (mWorkoutData.getElapsedTime() * 1000)));

        initShareLayout();
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

                getFragmentController().performOperation(IFragmentController.SAY_THANK_YOU, mCauseData.getCauseThankYouImage());
                break;
            case R.id.btn_share_screen:
                if (mSelectedShareMedium == SHARE_MEDIUM.WHATS_APP) {
                    shareOnWhatsApp();
                } else if (mSelectedShareMedium == SHARE_MEDIUM.GOOGLE) {

                    shareOnGooglePlus();
                } else if (mSelectedShareMedium == SHARE_MEDIUM.TWITTER) {

                } else {

                }
                break;
            default:

        }
    }

    private void shareOnGooglePlus() {
        /*Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText("Welcome to the Google+ platform.")
                .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                .getIntent();

        startActivityForResult(shareIntent, 0);*/
    }

    private void shareOnWhatsApp() {

        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mCauseData.getCauseShareMessageTemplate());
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Whats app not installed ", Toast.LENGTH_LONG).show();
        }

    }

    private String getTimeInHHMMFormat(long millis) {
        return String.format("%02dhr %02dmins", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)/*,
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)*/);
    }
}



