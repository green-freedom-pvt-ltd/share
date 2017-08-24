package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.utils.ShareImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import Models.LeagueBoard;
import butterknife.BindView;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class LeagueBoardFragment extends BaseLeaderBoardFragment implements LeaderBoardAdapter.ItemClickListener {

    @BindView(R.id.banner)
    ImageView mBanner;

    private String mBannerUrl;

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
        mBannerUrl = board.getLeagueBanner();
        String leagueName = board.getLeagueName();
        for (LeagueBoard.Team team : board.getTeamList()) {
            mleaderBoardList.add(team.convertToLeaderBoard());
        }
        setBannerImage();
        hideProgressDialog();
        if (!TextUtils.isEmpty(leagueName)){
            setToolbarTitle(leagueName);
        }
        mLeaderBoardAdapter.setData(mleaderBoardList);
    }

    private void setBannerImage() {
        if (!TextUtils.isEmpty(mBannerUrl)) {
            ShareImageLoader.getInstance().loadImage(mBannerUrl, mBanner,
                    ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));
            mBanner.setVisibility(View.VISIBLE);
        }
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
