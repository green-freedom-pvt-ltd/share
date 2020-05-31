package com.sharesmile.share.profile.badges;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.profile.badges.adapter.InProgressBadgeAdapter;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class InProgressBadgeFragment extends BaseFragment implements SeeAchievedBadge {

    private static final String TAG = "InProgressBadgeFragment";

    @BindView(R.id.rv_achievement_badges)
    RecyclerView achievementBadgesRecyclerView;

    InProgressBadgeAdapter inProgressBadgeAdapter;

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
                        AchievedBadgeDao.Properties.BadgeIdAchieved.notEq(-1),
                        AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID())).list();

        Collections.sort(achievedBadges, new Comparator<AchievedBadge>() {
            @Override
            public int compare(AchievedBadge o1, AchievedBadge o2) {
                BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
                List<Badge> badges1 = badgeDao.queryBuilder()
                        .where(BadgeDao.Properties.BadgeId.eq(o1.getBadgeIdInProgress())).limit(1).list();
                List<Badge> badges2 = badgeDao.queryBuilder()
                        .where(BadgeDao.Properties.BadgeId.eq(o2.getBadgeIdInProgress())).limit(1).list();
                if (badges1 != null && badges1.size() > 0 && badges2 != null && badges2.size() > 0) {
                    if (badges1.get(0).getType().equals(Constants.BADGE_TYPE_STREAK)) {
                        return 1;
                    } else if (badges2.get(0).getType().equals(Constants.BADGE_TYPE_STREAK)) {
                        return -1;
                    } else {
                        double badge1Total = badges1.get(0).getBadgeParameter();
                        double badge2Total = badges2.get(0).getBadgeParameter();
                        double badge1Diff = badge1Total - o1.getParamDone();
                        double badge2Diff = badge2Total - o2.getParamDone();
                        return badge1Diff < badge2Diff ? -1 : 1;
                    }
                } else {
                    return 0;
                }

            }
        });
        inProgressBadgeAdapter = new InProgressBadgeAdapter(achievedBadges,getContext());
        achievementBadgesRecyclerView.setAdapter(inProgressBadgeAdapter);

    }

    private void setupToolbar() {
        setHasOptionsMenu(false);
        setToolbarTitle(getResources().getString(R.string.in_progress));
    }
    @Override
    public void showBadgeDetails(long id, String badgeType) {
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
