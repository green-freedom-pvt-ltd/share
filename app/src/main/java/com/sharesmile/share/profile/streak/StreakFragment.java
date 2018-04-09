package com.sharesmile.share.profile.streak;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class StreakFragment extends BaseFragment {

    private static final String TAG = "StreakFragment";


    @BindView(R.id.streak_progress)
    ProgressBar streakProgress;

    @BindView(R.id.tv_edit_goal)
    TextView editGoal;

    @BindView(R.id.btn_tell_your_friends)
    Button tellYourFriends;

    @BindView(R.id.tv_streak_best)
    TextView streakBest;

    @BindView(R.id.tv_streak_distance)
    TextView streakDistance;

    @BindView(R.id.lt_streak_goal_icons)
    LinearLayout streakGoalIconsLayout;

    @BindView(R.id.tv_streak_count)
    TextView streakCount;

    @BindView(R.id.iv_back)
    ImageView back;

    @BindView(R.id.iv_close)
    ImageView close;

    @BindView(R.id.sharable_container)
    View sharableContainer;

    int from;

    public static StreakFragment newInstance(int from) {
        StreakFragment fragment = new StreakFragment();
        Bundle args = new Bundle();
        args.putInt("FROM",from);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        from = bundle.getInt("FROM");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_run_streak, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getFragmentController().hideToolbar();
        initUi();
    }



    private void initUi() {
        if(from == Constants.FROM_THANK_YOU_SCREEN_FOR_STREAK) {
            back.setVisibility(View.GONE);
            close.setVisibility(View.GONE);
            editGoal.setText("CONTINUE");
        }else {
            back.setVisibility(View.VISIBLE);
            close.setVisibility(View.GONE);
            editGoal.setText("EDIT GOAL");
        }
        streakCount.setText(MainApplication.getInstance().getUserDetails().getStreakCount()+"");
        streakGoalIconsLayout.removeAllViews();
        double distanceDiff = (MainApplication.getInstance().getUserDetails().getStreakGoalDistance() - MainApplication.getInstance().getUserDetails().getStreakRunProgress());
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        if(distanceDiff<=0 || userDetails.isStreakAdded()) {
            streakDistance.setText("Congratulations!!! "+Utils.formatToKmsWithTwoDecimal((float)userDetails.getStreakRunProgress()*1000)+" kms done.");
        }else
        {
            streakDistance.setText(Utils.formatToKmsWithTwoDecimal((float) distanceDiff*1000)+" kms left, Let's Go.");
        }

        double progress;
        if(distanceDiff<=0 || userDetails.isStreakAdded())
            progress = 360;
        else
            progress = (MainApplication.getInstance().getUserDetails().getStreakRunProgress()*360.0)/MainApplication.getInstance().getUserDetails().getStreakGoalDistance();

        ObjectAnimator animation = ObjectAnimator.ofInt(streakProgress, "progress", (int) progress);
        animation.setDuration(1000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
        streakBest.setText("BEST "+MainApplication.getInstance().getUserDetails().getStreakMaxCount());
    }

    @OnClick(R.id.iv_back)
    void onBack()
    {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.btn_tell_your_friends)
    void tellYourFriends()
    {
        //TODO : change message
        Bitmap toShare = Utils.getBitmapFromLiveView(sharableContainer);
        Utils.share(getContext(), Utils.getLocalBitmapUri(toShare, getContext()),
                getString(R.string.share_stats));
        AnalyticsEvent.create(Event.ON_SELECT_SHARE_MENU)
                .buildAndDispatch();
    }

    @OnClick(R.id.tv_edit_goal)
    void editGoal()
    {
        if(from == Constants.FROM_THANK_YOU_SCREEN_FOR_STREAK)
        {
            openHomeActivityAndFinish();
        }else if(from == Constants.FROM_PROFILE_FOR_STREAK)
        {
            getFragmentController().replaceFragment(new StreakGoalFragment(), true);
        }
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

}
