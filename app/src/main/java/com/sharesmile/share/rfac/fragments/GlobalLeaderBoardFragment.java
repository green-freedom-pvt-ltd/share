package com.sharesmile.share.rfac.fragments;

import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.IFragmentController;

import butterknife.BindView;

import static com.sharesmile.share.LeaderBoardDataStore.LAST_WEEK_INTERVAL;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class GlobalLeaderBoardFragment extends BaseLeaderBoardFragment {

    @BindView(R.id.tv_leaderboard_data)
    TextView mInfoView;

    @BindView(R.id.containerView)
    CardView myListItem;


    TextView myRank;

    TextView myProfileName;

    TextView mylastWeekDistance;


    private LeaderBoard myLeaderBoard;

    public static GlobalLeaderBoardFragment getInstance() {
        GlobalLeaderBoardFragment fragment = new GlobalLeaderBoardFragment();
        return fragment;
    }

    @Override
    protected void init() {
        super.init();
        mInfoView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void refreshItems() {

    }

    @Override
    protected void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.leaderboard));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_leaderboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_league:
                int teamId = LeaderBoardDataStore.getInstance().getMyTeamId();
                if (teamId > 0) {
                    getFragmentController().replaceFragment(LeagueBoardFragment.getInstance(), true);
                } else {
                    getFragmentController().performOperation(IFragmentController.SHOW_LEAGUE_ACTIVITY, null);
                }
                AnalyticsEvent.create(Event.ON_CLICK_CUP_ICON)
                        .put("team_id", LeaderBoardDataStore.getInstance().getMyTeamId())
                        .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                        .buildAndDispatch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void fetchData() {
        // GlobalLeaderBoard
        if (LeaderBoardDataStore.getInstance().getGlobalLastWeekLeaderBoard() != null){
            showGlobalLeaderBoardData(LeaderBoardDataStore.getInstance()
                    .getGlobalLastWeekLeaderBoard());
        }else {
            LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData(LAST_WEEK_INTERVAL);
            showProgressDialog();
        }
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.GLOBAL_LEADERBOARD;
    }

    @Override
    public void onItemClick(long id) {

    }
}
