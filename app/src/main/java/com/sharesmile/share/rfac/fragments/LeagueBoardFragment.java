package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import Models.LeagueBoard;
import butterknife.BindView;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class LeagueBoardFragment extends BaseLeaderBoardFragment implements LeaderBoardAdapter.ItemClickListener {

    @BindView(R.id.banner_container)
    View bannerContainer;

    public static LeagueBoardFragment getInstance() {
        LeagueBoardFragment fragment = new LeagueBoardFragment();
        return fragment;
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
    protected void init() {
        super.init();
        mLeaderBoardAdapter.setItemClickListener(this);
    }

    @Override
    protected void refreshItems() {
        super.refreshItems();
        LeaderBoardDataStore.getInstance().updateLeagueBoardData();
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(LeaderBoardDataStore.getInstance().getLeagueName());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LeagueBoardDataUpdated event){
        if (isAttachedToActivity()){
            hideProgressDialog();
            LeagueBoard board = LeaderBoardDataStore.getInstance().getLeagueBoard();
            if (event.isSuccess()){
                showLeagueBoardData(board);
            }else {
                if (board != null){
                    showLeagueBoardData(board);
                    MainApplication.showToast("Network Error, Couldn't refresh");
                }else {
                    MainApplication.showToast("Network Error, Please try again");
                }
            }
        }
    }

    private void showLeagueBoardData(LeagueBoard board){
        mleaderBoardList.clear();
        String leagueName = board.getLeagueName();
        for (LeagueBoard.Team team : board.getTeamList()) {
            mleaderBoardList.add(team.convertToLeaderBoard());
        }
        setBannerContainer(board);
        hideProgressDialog();
        if (!TextUtils.isEmpty(leagueName)){
            setToolbarTitle(leagueName);
        }
        mLeaderBoardAdapter.setData(mleaderBoardList);
    }

    private void setBannerContainer(LeagueBoard board) {
        if (!TextUtils.isEmpty(board.getLeagueLogo())) {
            ShareImageLoader.getInstance().loadImage(board.getLeagueLogo(), bannerLogo);
            bannerLogo.setVisibility(View.VISIBLE);
        }
        bannerTotalImpact.setText("\u20B9 " + Utils.formatEnglishCommaSeparated(board.getTotalImpact()));
        bannerNumRuns.setText(Utils.formatEnglishCommaSeparated(board.getTotalRuns()));
        bannerNumMembers.setText(String.valueOf(board.getTotalMembers()));
        bannerContainer.setVisibility(View.VISIBLE);
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
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.LEAGUEBOARD;
    }

}
