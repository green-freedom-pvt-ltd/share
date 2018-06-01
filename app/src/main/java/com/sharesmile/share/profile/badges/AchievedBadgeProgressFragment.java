package com.sharesmile.share.profile.badges;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
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
import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
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
import com.sharesmile.share.profile.badges.adapter.AchievementBadgeProgressAdapter;
import com.sharesmile.share.profile.badges.adapter.AchievementsAdapter;
import com.sharesmile.share.profile.badges.adapter.HallOfFameAdapter;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;
import com.sharesmile.share.profile.badges.model.HallOfFameData;
import com.sharesmile.share.profile.history.ProfileHistoryFragment;
import com.sharesmile.share.profile.stats.BarChartDataSet;
import com.sharesmile.share.profile.stats.BarChartEntry;
import com.sharesmile.share.profile.streak.StreakFragment;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import Models.Level;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.dao.query.WhereCondition;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_RUN;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;
import static com.sharesmile.share.core.Constants.PROFILE_SCREEN;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class AchievedBadgeProgressFragment extends BaseFragment implements SeeAchivedBadge {

    private static final String TAG = "AchievedBadgeProgressFragment";

    @BindView(R.id.rv_achievement_badges)
    RecyclerView achievementBadgesRecyclerView;

    AchievementBadgeProgressAdapter achievementBadgeProgressAdapter;

    @BindView(R.id.rv_hall_of_fame)
    RecyclerView hallOfFameRecyclerView;
    HallOfFameAdapter hallOfFameAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_achievements, null);
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
        initUi();
    }


    private void initUi() {
        setupToolbar();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        achievementBadgesRecyclerView.setLayoutManager(linearLayoutManager);
        AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();
        List<AchievedBadge> achievedBadges = achievedBadgeDao.queryBuilder()
                .where(AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS),
                        AchievedBadgeDao.Properties.BadgeIdAchieved.notEq(-1)).list();
        achievementBadgeProgressAdapter = new AchievementBadgeProgressAdapter(achievedBadges);
        achievementBadgesRecyclerView.setAdapter(achievementBadgeProgressAdapter);

        LinearLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),3)
        {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        hallOfFameRecyclerView.setLayoutManager(gridLayoutManager);

        Cursor cursor = achievedBadgeDao.getDatabase()
                .rawQuery("SELECT "+AchievedBadgeDao.Properties.BadgeIdAchieved.columnName+
                        " , COUNT("+AchievedBadgeDao.Properties.Id.columnName+")" +
                        " , "+AchievedBadgeDao.Properties.BadgeType.columnName +
                "  FROM " + AchievedBadgeDao.TABLENAME
                        +" WHERE "+AchievedBadgeDao.Properties.UserId.columnName+"="+MainApplication.getInstance().getUserID()+" AND "+
                        AchievedBadgeDao.Properties.BadgeIdAchieved.columnName+">0"
                        +" GROUP BY "+AchievedBadgeDao.Properties.BadgeIdAchieved.columnName, new String []{});
        cursor.moveToFirst();
        ArrayList<HallOfFameData> hallOfFameBadges = new ArrayList<>();
        while (!cursor.isAfterLast())
        {
            HallOfFameData hallOfFameData = new HallOfFameData(cursor.getInt(0),cursor.getInt(1),cursor.getString(2));
            hallOfFameBadges.add(hallOfFameData);
            cursor.moveToNext();
        }

        hallOfFameAdapter = new HallOfFameAdapter(hallOfFameBadges,this);
        hallOfFameRecyclerView.setAdapter(hallOfFameAdapter);
    }

    private void setupToolbar() {
        setHasOptionsMenu(false);
        setToolbarTitle(getResources().getString(R.string.achievements));
    }
    @Override
    public void showBadgeDetails(int id, String badgeType) {
        AchievedBadgesData achievedBadgesData = new AchievedBadgesData();

        switch (badgeType)
        {
            case Constants.BADGE_TYPE_CAUSE :
                achievedBadgesData.setCauseBadgeAchieved(id);
                break;
            case Constants.BADGE_TYPE_CHANGEMAKER :
                achievedBadgesData.setChangeMakerBadgeAchieved(id);
                break;
            case Constants.BADGE_TYPE_MARATHON :
                achievedBadgesData.setMarathonBadgeAchieved(id);
                break;
            case Constants.BADGE_TYPE_STREAK :
                achievedBadgesData.setStreakBadgeAchieved(id);
                break;
        }
        AchieviedBadgeFragment achieviedBadgeFragment = AchieviedBadgeFragment.newInstance(achievedBadgesData,badgeType,0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            achieviedBadgeFragment.setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }
        getFragmentController().replaceFragment(achieviedBadgeFragment,true,badgeType);
    }
}
