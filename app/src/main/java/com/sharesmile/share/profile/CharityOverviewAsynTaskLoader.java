package com.sharesmile.share.profile;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.profile.model.CategoryStats;
import com.sharesmile.share.profile.model.CauseStats;
import com.sharesmile.share.profile.model.CharityOverview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class CharityOverviewAsynTaskLoader extends AsyncTaskLoader<CharityOverview> {

    private CharityOverview charityOverview;

    public CharityOverviewAsynTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if(charityOverview!=null)
            deliverResult(charityOverview);
        else
            forceLoad();
    }

    @Override
    public CharityOverview loadInBackground() {
        CharityOverview charityOverview = null;
        try {
            JSONObject jsonObject = new JSONObject(SharedPrefsManager.getInstance().getString(Constants.PREF_CHARITY_OVERVIEW));
            charityOverview = new CharityOverview();
            charityOverview.setTotalRaised(jsonObject.getInt("total_raised"));
            charityOverview.setTotalWorkouts(jsonObject.getInt("total_workouts"));

            ArrayList<CategoryStats> categoryStatsArrayList = new ArrayList<>();
            JSONObject categoryWiseStats = jsonObject.getJSONObject("category_wise_stats");
            Iterator<String> categoryWiseStatsKeys = categoryWiseStats.keys();
            while (categoryWiseStatsKeys.hasNext())
            {
                String key = categoryWiseStatsKeys.next();
                JSONObject value = categoryWiseStats.getJSONObject(key);
                CategoryStats categoryStats = new CategoryStats();
                categoryStats.setCategoryName(key);
                categoryStats.setCategoryRaised(value.getInt("category_raised"));
                categoryStats.setCategoryWorkouts(value.getInt("category_workouts"));
                ArrayList<CauseStats> causeStatsArrayList = new ArrayList<>();
                JSONObject causeWiseStats = value.getJSONObject("cause_wise_stats");
                Iterator<String> causeWiseStatsKeys = causeWiseStats.keys();
                while (causeWiseStatsKeys.hasNext())
                {
                    String causeKey = causeWiseStatsKeys.next();
                    JSONObject causeValue = causeWiseStats.getJSONObject(causeKey);
                    CauseStats causeStats = new CauseStats();
                    causeStats.setCauseName(causeKey);
                    causeStats.setCause_raised(causeValue.getInt("cause_raised"));
                    causeStats.setCause_workouts(causeValue.getInt("cause_workouts"));
                    causeStatsArrayList.add(causeStats);
                }
                categoryStats.setCauseStats(causeStatsArrayList);
                categoryStatsArrayList.add(categoryStats);
            }
            charityOverview.setCategoryStats(categoryStatsArrayList);

        } catch (JSONException e) {
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
