package com.sharesmile.share.leaderboard.impactleague;

import android.text.TextUtils;

import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;
import com.sharesmile.share.leaderboard.impactleague.event.LeagueBoardDataUpdated;
import com.sharesmile.share.leaderboard.impactleague.event.LeagueDataEvent;

import java.util.ArrayList;
import java.util.List;

import Models.LeagueBoard;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class LeagueBoardFragment extends BaseLeagueFragment {

    private static final String TAG = "LeagueBoardFragment";

    LeagueBoard origData;

    public static LeagueBoardFragment getInstance() {
        LeagueBoardFragment fragment = new LeagueBoardFragment();
        return fragment;
    }

    @Override
    protected void init(boolean b) {
        super.init(false);
    }

    @Override
    protected void refreshItems() {
        Logger.d(TAG, "refreshItems");
        super.refreshItems();
        LeaderBoardDataStore.getInstance().updateLeagueBoardData();
    }

    @Override
    protected void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(LeaderBoardDataStore.getInstance().getLeagueName());
    }

    @Override
    protected void fetchData() {
        origData = LeaderBoardDataStore.getInstance().getLeagueBoard();
        if (origData != null){
            prepareDataSetAndRender();
        }else {
            LeaderBoardDataStore.getInstance().updateLeagueBoardData();
            showProgressDialog();
        }
    }

    @Override
    public void onDataLoadEvent(LeagueDataEvent dataEvent) {
        if (dataEvent instanceof LeagueBoardDataUpdated){
            Logger.d(TAG, "onEvent: LeagueBoardDataUpdated");
            LeagueBoardDataUpdated event = (LeagueBoardDataUpdated) dataEvent;

            if (isAttachedToActivity()){
                hideProgressDialog();
                origData = LeaderBoardDataStore.getInstance().getLeagueBoard();
                if (event.isSuccess()){
                    prepareDataSetAndRender();
                }else {
                    if (origData != null){
                        prepareDataSetAndRender();
                        MainApplication.showToast("Network Error, Couldn't refresh");
                    }else {
                        MainApplication.showToast("Network Error, Please try again");
                    }
                    hideProgressDialog();
                }
            }
        }
    }

    private void prepareDataSetAndRender(){
        Logger.d(TAG, "prepareDataSetAndRender");
        List<BaseLeaderBoardItem> itemList = new ArrayList<>();

        int myTeamId = LeaderBoardDataStore.getInstance().getMyTeamId();
        int myPos = -1;
        List<LeagueBoard.Team> origList = origData.getTeamList();
        for (int i=0; i < origList.size(); i++) {
            LeagueBoard.Team data = origList.get(i);
            if (myTeamId == data.getId()){
                myPos = i;
            }
            itemList.add(data.convertToLeaderBoard());
        }
        String leagueName = origData.getLeagueName();
        if (!TextUtils.isEmpty(leagueName)){
            setToolbarTitle(leagueName);
        }
        render(itemList, myPos,myTeamId);
    }

    @Override
    public void onItemClick(long id) {
        getFragmentController().replaceFragment(TeamLeaderBoardFragment.getInstance((int) id), true);
        int myTeamId = LeaderBoardDataStore.getInstance().getMyTeamId();
        if (myTeamId == id){
            AnalyticsEvent.create(Event.ON_CLICK_SELF_TEAM_LEAGUE_BOARD)
                    .put("team_id", id)
                    .put("team_name", LeaderBoardDataStore.getInstance().getMyTeamName())
                    .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                    .buildAndDispatch();
        } else {
            AnalyticsEvent.create(Event.ON_CLICK_OTHER_TEAM_LEAGUE_BOARD)
                    .put("team_id", id)
                    .put("team_name", LeaderBoardDataStore.getInstance().getTeamName((int) id))
                    .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                    .buildAndDispatch();
        }
    }

    @Override
    public int getMyId() {
        return LeaderBoardDataStore.getInstance().getMyTeamId();
    }

    @Override
    public boolean toShowLogo() {
        return origData != null && origData.getShowTeamLogos();
    }

    @Override
    public int toShowBanner() {
        return 1;
    }

    @Override
    public LeagueBoard getBannerData() {
        return origData;
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.LEAGUEBOARD;
    }

}
