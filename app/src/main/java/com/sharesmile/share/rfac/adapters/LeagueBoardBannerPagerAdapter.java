package com.sharesmile.share.rfac.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.R;
import com.sharesmile.share.rfac.ImageBannerContainer;
import com.sharesmile.share.rfac.StatsBannerContainer;

import Models.LeagueBoard;

/**
 * Created by ankitmaheshwari on 9/3/17.
 */

public class LeagueBoardBannerPagerAdapter extends BannerPagerAdapter{

    private LeagueBoard leagueBoard;

    private ImageBannerContainer imageBannerContainer;
    private StatsBannerContainer statsBannerContainer;

    public LeagueBoardBannerPagerAdapter() {
    }

    public void setData(LeagueBoard leagueBoard){
        this.leagueBoard = leagueBoard;
        notifyDataSetChanged();
    }

    @Override
    int getNumPages() {
        return 2;
    }

    @Override
    protected View getItemView(int position, ViewGroup container) {
        switch (position){
            case 0:
                View imageBanner = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.banner_image_container, container, false);
                imageBannerContainer = new ImageBannerContainer(imageBanner, leagueBoard.getLeagueBanner());
                return imageBanner;
            case 1:
                View statsBanner = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.banner_stats_container, container, false);
                statsBannerContainer = new StatsBannerContainer(statsBanner, leagueBoard);
                return statsBanner;
            default:
                throw new IndexOutOfBoundsException("Invalid index "+position+" for LeagueBoardBannerPagerAdapter");
        }
    }
}
