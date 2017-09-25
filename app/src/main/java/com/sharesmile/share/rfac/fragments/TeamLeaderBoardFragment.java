package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sharesmile.share.Events.ExitLeague;
import com.sharesmile.share.Events.TeamLeaderBoardDataFetched;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import Models.TeamLeaderBoard;

import static com.sharesmile.share.core.IFragmentController.OPEN_HELP_CENTER;
import static com.sharesmile.share.core.IFragmentController.START_MAIN_ACTIVITY;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class TeamLeaderBoardFragment extends BaseLeaderBoardFragment {

    private static final String BUNDLE_TEAM_ID = "bundle_team_id";

    private int mTeamId;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TeamLeaderBoardDataFetched event){
        if (mTeamId == event.getTeamId() && isAttachedToActivity()){
            // Check if we received TeamLeaderBoard data for the correct teamId
            // &&
            // Check if fragment is still on display
            TeamLeaderBoard board = null;
            if (event.isSuccess()){
                board = event.getTeamLeaderBoard();
            }else if (mTeamId == LeaderBoardDataStore.getInstance().getMyTeamId()){
                board = LeaderBoardDataStore.getInstance().getMyTeamLeaderBoard();
            }
            mleaderBoardList.clear();
            hideProgressDialog();
            if (board != null){
                // We have something to display
                String teamName = board.getTeamName();
                if (!TextUtils.isEmpty(teamName)){
                    setToolbarTitle(teamName);
                }
                for (TeamLeaderBoard.MemberDetails memberDetails : board.getMembersList()) {
                    mleaderBoardList.add(memberDetails.convertToLeaderBoard());
                }
                mLeaderBoardAdapter.setData(mleaderBoardList);
            }else {
                MainApplication.showToast("Network Error, Please try again");
            }
        }
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.TEAM_LEADERBAORD;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ExitLeague event){
        // Successful exit from League, need to take the user back to home screen
        if (isAttachedToActivity()){
            getFragmentController().performOperation(START_MAIN_ACTIVITY, null);
            getFragmentController().exit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_league_board, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                getFragmentController().performOperation(OPEN_HELP_CENTER,false);
                return true;
            case R.id.menu_exit:
                LeaderBoardDataStore.getInstance().exitLeague();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
