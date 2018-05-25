package com.sharesmile.share.profile.badges;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.home.homescreen.OnboardingOverlay;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.profile.EditProfileFragment;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;
import com.sharesmile.share.profile.history.ProfileHistoryFragment;
import com.sharesmile.share.profile.stats.BarChartDataSet;
import com.sharesmile.share.profile.stats.BarChartEntry;
import com.sharesmile.share.profile.streak.StreakFragment;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import Models.Level;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_RUN;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;
import static com.sharesmile.share.core.Constants.PROFILE_SCREEN;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class AchieviedBadgeFragment extends BaseFragment {

    @BindView(R.id.continue_tv)
    TextView continueTv;
    @BindView(R.id.badge_earned_tv)
    TextView badgeEarnedTv;
    @BindView(R.id.badge_title_tv)
    TextView badgeTitle;
    @BindView(R.id.badge_amount_raised_tv)
    TextView badgeAmountRaised;
    @BindView(R.id.badge_upgrade_tv)
    TextView badgeUpgrade;

    private String TAG = "AchieviedBadgeFragment";
    AchievedBadgesData achievedBadgesData;


    public static AchieviedBadgeFragment newInstance(AchievedBadgesData achievedBadgesData,String tag) {
        AchieviedBadgeFragment fragment = new AchieviedBadgeFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ACHIEVED_BADGE_DATA, achievedBadgesData);
        args.putString("TAG", tag);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_badge, null);
        ButterKnife.bind(this, v);
//        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void onDestroyView() {
//        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        achievedBadgesData = bundle.getParcelable(Constants.ACHIEVED_BADGE_DATA);
        TAG = bundle.getString("TAG");
        initUi();
    }

    private void initUi() {
        int badgeId = 0;
        switch (TAG)
        {
            case Constants.BADGE_TYPE_CHANGEMAKER:
                badgeId = achievedBadgesData.getChangeMakerBadgeAchieved();
                break;
            case Constants.BADGE_TYPE_STREAK:
                badgeId = achievedBadgesData.getStreakBadgeAchieved();
                break;
            case Constants.BADGE_TYPE_CAUSE:
                badgeId = achievedBadgesData.getCauseBadgeAchieved();
                break;
            case Constants.BADGE_TYPE_MARATHON:
                badgeId = achievedBadgesData.getMarathonBadgeAchieved();
                break;
        }
        BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
        List<Badge> badges = badgeDao.queryBuilder().where(BadgeDao.Properties.BadgeId.eq(badgeId)).list();
        if(badges!=null && badges.size()>0)
        {
            Badge badge = badges.get(0);
            badgeTitle.setText(badge.getName());
            badgeAmountRaised.setText(badge.getDescription1());
            badgeUpgrade.setText(badge.getDescription2());
        }
    }

    @OnClick(R.id.continue_tv)
    public void continueClick() {
        switch (TAG) {
            case Constants.BADGE_TYPE_CHANGEMAKER:
                if(achievedBadgesData.getStreakBadgeAchieved()>0)
                {
                    getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.BADGE_TYPE_STREAK), true,Constants.BADGE_TYPE_STREAK);
                }else if(achievedBadgesData.getCauseBadgeAchieved()>0)
                {
                    getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.BADGE_TYPE_CAUSE), true,Constants.BADGE_TYPE_CAUSE);
                }else if(achievedBadgesData.getMarathonBadgeAchieved()>0)
                {
                    getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.BADGE_TYPE_MARATHON), true,Constants.BADGE_TYPE_MARATHON);
                }else
                {
                    openHomeActivityAndFinish();
                }
                break;
            case Constants.BADGE_TYPE_STREAK:
                if(achievedBadgesData.getCauseBadgeAchieved()>0)
                {
                    getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.BADGE_TYPE_CAUSE), true,Constants.BADGE_TYPE_CAUSE);
                }else if(achievedBadgesData.getMarathonBadgeAchieved()>0)
                {
                    getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.BADGE_TYPE_MARATHON), true,Constants.BADGE_TYPE_MARATHON);
                }else
                {
                    openHomeActivityAndFinish();
                }
                break;
            case Constants.BADGE_TYPE_CAUSE:
                if(achievedBadgesData.getMarathonBadgeAchieved()>0)
                {
                    getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.BADGE_TYPE_MARATHON), true,Constants.BADGE_TYPE_MARATHON);
                }else
                {
                    openHomeActivityAndFinish();
                }
                break;
            case Constants.BADGE_TYPE_MARATHON:
                openHomeActivityAndFinish();
                break;
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
