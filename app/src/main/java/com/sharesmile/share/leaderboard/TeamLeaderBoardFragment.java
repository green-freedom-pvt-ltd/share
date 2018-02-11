package com.sharesmile.share.leaderboard;

import android.os.Bundle;
import android.text.TextUtils;

import com.sharesmile.share.leaderboard.impactleague.event.LeagueDataEvent;
import com.sharesmile.share.leaderboard.impactleague.event.TeamLeaderBoardDataFetched;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.rfac.models.BaseLeaderBoardItem;

import java.util.ArrayList;
import java.util.List;

import Models.TeamLeaderBoard;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class TeamLeaderBoardFragment extends BaseLeagueFragment {

    private static final String BUNDLE_TEAM_ID = "bundle_team_id";

    private int mTeamId;

    private TeamLeaderBoard origData;

    public static TeamLeaderBoardFragment getInstance(int teamId) {
        TeamLeaderBoardFragment fragment = new TeamLeaderBoardFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_TEAM_ID, teamId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            mTeamId = arg.getInt(BUNDLE_TEAM_ID);
        }
    }

    @Override
    protected void refreshItems() {
        super.refreshItems();
        LeaderBoardDataStore.getInstance().updateTeamLeaderBoardData(mTeamId);
    }

    @Override
    protected void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.team_leader_board));
    }

    @Override
    protected void fetchData() {
        LeaderBoardDataStore.getInstance().updateTeamLeaderBoardData(mTeamId);
        showProgressDialog();
    }

    @Override
    void onDataLoadEvent(LeagueDataEvent dataEvent) {
        if (dataEvent instanceof TeamLeaderBoardDataFetched){
            TeamLeaderBoardDataFetched event = (TeamLeaderBoardDataFetched) dataEvent;
            if (mTeamId == event.getTeamId() && isAttachedToActivity()){
                // Check if we received TeamLeaderBoard data for the correct teamId
                // &&
                // Check if fragment is still on display
                if (event.isSuccess()){
                    origData = event.getTeamLeaderBoard();
                }else if (mTeamId == LeaderBoardDataStore.getInstance().getMyTeamId()){
                    origData = LeaderBoardDataStore.getInstance().getMyTeamLeaderBoard();
                }
                if (origData != null){
                    // We have something to display
                    String teamName = origData.getTeamName();
                    if (!TextUtils.isEmpty(teamName)){
                        setToolbarTitle(teamName);
                    }
                    prepareDatasetAndRender();
                }else {
                    MainApplication.showToast("Network Error, Please try again");
                }
                hideProgressDialog();
            }
        }
    }

    public void prepareDatasetAndRender() {
        List<BaseLeaderBoardItem> itemList = new ArrayList<>();
        int myUserId = MainApplication.getInstance().getUserID();
        int myPos = -1;
        List<TeamLeaderBoard.MemberDetails> origList = origData.getMembersList();
        for (int i=0; i < origList.size(); i++) {
            TeamLeaderBoard.MemberDetails data = origList.get(i);
            if (myUserId == data.getId()){
                myPos = i;
            }
            itemList.add(data.convertToLeaderBoard());
        }
        render(itemList, myPos);
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.TEAM_LEADERBAORD;
    }

    @Override
    public void onItemClick(long id) {
        // Nothing to do here
    }

    @Override
    public int getMyId() {
        return MainApplication.getInstance().getUserID();
    }

    @Override
    public boolean toShowLogo() {
        return true;
    }

}
