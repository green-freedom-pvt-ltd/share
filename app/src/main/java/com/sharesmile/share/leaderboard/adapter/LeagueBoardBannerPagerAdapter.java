package com.sharesmile.share.leaderboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.leaderboard.ImageBannerContainer;
import com.sharesmile.share.leaderboard.StatsBannerContainer;

import Models.LeagueBoard;

/**
 * Created by ankitmaheshwari on 9/3/17.
 */

public class LeagueBoardBannerPagerAdapter extends BannerPagerAdapter {

    private static final String TAG = "LeagueBoardBannerPagerAdapter";

    private LeagueBoard leagueBoard;

    private ImageBannerContainer imageBannerContainer;
    private StatsBannerContainer statsBannerContainer;

    public LeagueBoardBannerPagerAdapter() {
    }

    public void setData(LeagueBoard leagueBoard){
        Logger.d(TAG, "setData");
        this.leagueBoard = leagueBoard;
        notifyDataSetChanged();
    }

    @Override
    int getNumPages() {
        return 2;
    }

    @Override
    protected View getItemView(final int position, ViewGroup container) {
        Logger.d(TAG, "getItemView: " + position);
        switch (position){
            case 0:
                View imageBanner = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.banner_image_container, container, false);
                imageBannerContainer = new ImageBannerContainer(imageBanner, leagueBoard.getLeagueBanner());
                imageBanner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnalyticsEvent.create(Event.ON_CLICK_LEAGUE_BOARD_BANNER)
                                .put("type", "image")
                                .buildAndDispatch();
                    }
                });
                return imageBanner;
            case 1:
                View statsBanner = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.banner_stats_container, container, false);
                Logger.d(TAG, "getItemView, stats banner, total raised = " + leagueBoard.getTotalImpact());
                statsBannerContainer = new StatsBannerContainer(statsBanner, leagueBoard);
                statsBanner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnalyticsEvent.create(Event.ON_CLICK_LEAGUE_BOARD_BANNER)
                                .put("type", "stats")
                                .buildAndDispatch();
                    }
                });
                return statsBanner;
            default:
                throw new IndexOutOfBoundsException("Invalid index "+position+" for LeagueBoardBannerPagerAdapter");
        }
    }
}
