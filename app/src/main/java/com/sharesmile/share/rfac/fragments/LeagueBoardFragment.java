package com.sharesmile.share.rfac.fragments;

import android.widget.ImageView;

import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.R;

import butterknife.BindView;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class LeagueBoardFragment extends BaseLeaderBoardFragment {

    @BindView(R.id.banner)
    ImageView mBanner;

    private String mBannerUrl;

    public static LeagueBoardFragment getInstance() {
        LeagueBoardFragment fragment = new LeagueBoardFragment();
        return fragment;
    }

    @Override
    protected void refreshItems() {

    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getResources().getString(R.string.impact_league));
    }

    @Override
    protected void fetchData() {
        if (LeaderBoardDataStore.getInstance().getLeagueBoard() != null){
            showLeagueBoardData(LeaderBoardDataStore.getInstance().getLeagueBoard());
        }else {
            LeaderBoardDataStore.getInstance().updateLeagueBoardData();
            showProgressDialog();
        }
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.LEAGUEBOARD;
    }

    @Override
    public void onItemClick(long id) {

    }
}
