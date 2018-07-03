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
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.profile.badges.adapter.InProgressBadgeAdapter;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;

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
