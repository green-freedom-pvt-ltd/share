package com.sharesmile.share.tracking.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.base.IFragmentController;
import com.sharesmile.share.core.cause.model.CauseData;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/31/2016.
 */
public class RunCountdownFragment extends BaseFragment {

    private static final String TAG = "RunCountdownFragment";

    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";

    TextView countdown;
    FragmentManager mFragmentManager;
    CountDownTimer Count;

    @BindView(R.id.img_sponsor_logo)
    ImageView mSponsorImage;
    private CauseData mCauseData;

    public static RunCountdownFragment newInstance(CauseData causeData) {
        RunCountdownFragment fragment = new RunCountdownFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_CAUSE_DATA, causeData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg=getArguments();
        mCauseData=(CauseData)arg.getSerializable(BUNDLE_CAUSE_DATA);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_run_count_down, null);
        ButterKnife.bind(this,v);
        countdown = v.findViewById(R.id.tv_countdown);
        RelativeLayout layout_countdown = v.findViewById(R.id.start_countdown_layout);
        mFragmentManager = getFragmentManager();
        Count = new CountDownTimer(3150, 1000) {

            public void onTick(long millisUntilFinished) {
                Logger.d(TAG, "onTick: " + millisUntilFinished) ;
                int seconds = Math.round ((millisUntilFinished / 1000));
                countdown.setText(seconds + "");
            }

            public void onFinish() {
                Logger.d(TAG, "onFinish") ;
                proceedToRunProgress();
                cancel();
            }
        };

        Count.start();
        ShareImageLoader.getInstance().loadImage(mCauseData.getSponsor().getLogoUrl(), mSponsorImage);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Count.cancel();
    }

    private void proceedToRunProgress() {
        if (isAttachedToActivity()) {
            getFragmentController().performOperation(IFragmentController.END_RUN_START_COUNTDOWN, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}