package com.sharesmile.share.profile;


import android.content.AsyncTaskLoader;
import android.content.Context;


import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.profile.model.CategoryStats;
import com.sharesmile.share.profile.model.CauseStats;
import com.sharesmile.share.profile.model.CharityOverview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class CharityOverviewAsyncTaskLoader extends AsyncTaskLoader<CharityOverview> {

    private CharityOverview charityOverview;

    public CharityOverviewAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if(charityOverview!=null)
            deliverResult(charityOverview);
        if(charityOverview == null || takeContentChanged())
            forceLoad();
    }

    @Override
    public CharityOverview loadInBackground() {
        CharityOverview charityOverview = null;
        try {
            JSONObject jsonObject = new JSONObject(SharedPrefsManager.getInstance().getString(Constants.PREF_CHARITY_OVERVIEW,null));
            if(jsonObject!=null) {
                charityOverview = new CharityOverview();
                charityOverview.setTotalRaised(jsonObject.getInt("total_raised"));
                charityOverview.setTotalWorkouts(jsonObject.getInt("total_workouts"));

                AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();
                BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
                ArrayList<CategoryStats> categoryStatsArrayList = new ArrayList<>();
                JSONObject categoryWiseStats = jsonObject.getJSONObject("category_wise_stats");
                Iterator<String> categoryWiseStatsKeys = categoryWiseStats.keys();
                while (categoryWiseStatsKeys.hasNext()) {
                    String key = categoryWiseStatsKeys.next();
                    JSONObject value = categoryWiseStats.getJSONObject(key);
                    CategoryStats categoryStats = new CategoryStats();
                    categoryStats.setCategoryName(key);
                    categoryStats.setCategoryRaised(value.getInt("category_raised"));
                    categoryStats.setCategoryWorkouts(value.getInt("category_workouts"));
                    ArrayList<CauseStats> causeStatsArrayList = new ArrayList<>();
                    JSONObject causeWiseStats = value.getJSONObject("cause_wise_stats");
                    Iterator<String> causeWiseStatsKeys = causeWiseStats.keys();

                    while (causeWiseStatsKeys.hasNext()) {
                        String causeKey = causeWiseStatsKeys.next();

                        JSONObject causeValue = causeWiseStats.getJSONObject(causeKey);
                        CauseStats causeStats = new CauseStats();
                        causeStats.setCauseName(causeKey);
                        causeStats.setCause_raised(causeValue.getInt("cause_raised"));
                        causeStats.setCause_workouts(causeValue.getInt("cause_workouts"));
                        List<AchievedBadge> achievedBadges = achievedBadgeDao.queryBuilder()
                                .where(AchievedBadgeDao.Properties.CauseName.eq(causeKey),
                                        AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID())).list();
                        if(achievedBadges.size()>0)
                        {
                            if(achievedBadges.get(0).getBadgeIdAchieved()>0) {
                                List<Badge> badges = badgeDao.queryBuilder()
                                        .where(BadgeDao.Properties.BadgeId.eq(achievedBadges.get(0).getBadgeIdAchieved())).list();
                                if (badges.size() > 0) {
                                    categoryStats.setCategoryNoOfStars(badges.get(0).getNoOfStars());
                                    causeStats.setCause_no_of_stars(badges.get(0).getNoOfStars());
                                }
                            }else {
                                categoryStats.setCategoryNoOfStars(0);
                                causeStats.setCause_no_of_stars(0);
                            }
                        }
                        causeStatsArrayList.add(causeStats);
                    }
                    categoryStats.setCauseStats(causeStatsArrayList);
                    categoryStatsArrayList.add(categoryStats);
                }
                charityOverview.setCategoryStats(categoryStatsArrayList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return charityOverview;
    }

    @Override
    public void deliverResult(CharityOverview data) {
        charityOverview = data;
        super.deliverResult(data);
    }
}
