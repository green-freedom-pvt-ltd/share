package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;

import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.R;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class TeamLeaderBoardFragment extends BaseLeaderBoardFragment {

    private static final String BUNDLE_TEAM_ID = "bundle_team_id";

    private int mTeamId;

    public static TeamLeaderBoardFragment getInstance(int teamId) {
        TeamLeaderBoardFragment fragment = new TeamLeaderBoardFragment();
        Bundle bundle = fragment.getArguments();
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

    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getResources().getString(R.string.team_leader_board));
    }

    @Override
    protected void fetchData() {
        LeaderBoardDataStore.getInstance().updateTeamLeaderBoardData(mTeamId);
        showProgressDialog();
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.TEAM_LEADERBAORD;
    }

    @Override
    public void onItemClick(long id) {

    }
}
