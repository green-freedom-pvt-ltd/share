package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sharesmile.share.Events.GlobalLeaderBoardDataUpdated;
import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.Events.TeamLeaderBoardDataFetched;
import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.rfac.models.LeaderBoardData;
import com.sharesmile.share.rfac.models.LeaderBoardList;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import Models.TeamBoard;
import Models.TeamLeaderBoard;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.LeaderBoardDataStore.LAST_WEEK_INTERVAL;

/**
 * Created by piyush on 8/30/16.
 */
public class LeaderBoardFragment extends BaseFragment implements LeaderBoardAdapter.ItemClickListener {

    private static final String TAG = "LeaderBoardFragment";




    public void showGlobalLeaderBoardData(LeaderBoardList list) {
        mleaderBoardList.clear();
        int userId = SharedPrefsManager.getInstance().getInt(Constants.PREF_USER_ID);
        boolean isShowingMyRank = false;
        for (LeaderBoardData data : list.getLeaderBoardList()){
            if (data.getRank() > 50 && userId == data.getUserid()){
                myLeaderBoard = data.getLeaderBoardDbObject();
                isShowingMyRank = true;
            }else if (data.getRank() > 0 && data.getRank() <= 50){
                mleaderBoardList.add(data.getLeaderBoardDbObject());
            }
        }

        if (isShowingMyRank){
            showMyRank(myLeaderBoard);
        }else {
            myListItem.setVisibility(View.GONE);
            mRecyclerView.setPadding(0,0,0,0);
        }

        mLeaderBoardAdapter.setData(mleaderBoardList);
        hideProgressDialog();
    }

    private void showLeagueBoardData(TeamBoard board){
        mleaderBoardList.clear();
        mBannerUrl = null;
        String leagueName = "";
        for (TeamBoard.Team team : board.getTeamList()) {
            if (TextUtils.isEmpty(leagueName)){
                leagueName = team.getLeagueName();
            }
            mleaderBoardList.add(team.convertToLeaderBoard());
            mBannerUrl = team.getBanner();
        }
        setBannerImage();
        hideProgressDialog();
        if (!TextUtils.isEmpty(leagueName)){
            setToolbarTitle(leagueName);
        }
        mLeaderBoardAdapter.setData(mleaderBoardList);
    }

    private void showMyRank(LeaderBoard myLeaderBoard){
        // Need to show rank at the bottom
        myListItem.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_gold));
        myListItem.setCardElevation(3f);
        myProfileName = (TextView) myListItem.findViewById(R.id.tv_profile_name);
        mylastWeekDistance = (TextView) myListItem.findViewById(R.id.last_week_distance);
        myRank = (TextView) myListItem.findViewById(R.id.id_leaderboard);

        myRank.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        myProfileName.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        mylastWeekDistance.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        mRecyclerView.setPadding(0,0,0, (int) Utils.convertDpToPixel(getContext(), 68));
        myListItem.setVisibility(View.VISIBLE);
        mLeaderBoardAdapter.createMyViewHolder(myListItem).bindData(myLeaderBoard, myLeaderBoard.getRank());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlobalLeaderBoardDataUpdated event){
        if (isAttachedToActivity() && BOARD_TYPE.GLOBAL_LEADERBOARD.equals(mBoard)){
            hideProgressDialog();
            LeaderBoardList globalLeaderBoardData = LeaderBoardDataStore.getInstance().getGlobalLastWeekLeaderBoard();
            if (event.isSuccess()){
                showGlobalLeaderBoardData(globalLeaderBoardData);
            }else {
                if (globalLeaderBoardData != null){
                    showGlobalLeaderBoardData(globalLeaderBoardData);
                    MainApplication.showToast("Network Error, Couldn't refresh");
                }else {
                    MainApplication.showToast("Network Error, Please try again");
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LeagueBoardDataUpdated event){
        if (isAttachedToActivity() && BOARD_TYPE.LEAGUEBOARD.equals(mBoard)){
            hideProgressDialog();
            TeamBoard board = LeaderBoardDataStore.getInstance().getLeagueBoard();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TeamLeaderBoardDataFetched event){
        if (mTeamId == event.getTeamId() && isAttachedToActivity() && BOARD_TYPE.TEAM_LEADERBAORD.equals(mBoard)){
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
                String teamName = "";
                for (TeamLeaderBoard.UserDetails team : board.getTeamList()) {
                    Float distance = 0f;
                    if (team.getLeagueTotalDistance() != null && team.getLeagueTotalDistance().getTotalDistance() != null) {
                        distance = team.getLeagueTotalDistance().getTotalDistance();
                    }
                    if (TextUtils.isEmpty(teamName)){
                        teamName = team.getTeamName();
                    }
                    mleaderBoardList.add(team.getUser().convertToLeaderBoard(distance));
                }
                if (!TextUtils.isEmpty(teamName)){
                    setToolbarTitle(teamName);
                }
                mLeaderBoardAdapter.setData(mleaderBoardList);
            }else {
                MainApplication.showToast("Network Error, Please try again");
            }
        }
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
        getFragmentController().replaceFragment(getInstance(BOARD_TYPE.TEAM_LEADERBAORD,(int)id), true);
        int myTeamId = LeaderBoardDataStore.getInstance().getMyTeamId();
        if (myTeamId == id){
            AnalyticsEvent.create(Event.ON_CLICK_SELF_TEAM_LEAGUE_BOARD)
                    .put("team_id", id)
                    .put("team_name", LeaderBoardDataStore.getInstance().getMyTeamName())
                    .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                    .buildAndDispatch();
        }else {
            AnalyticsEvent.create(Event.ON_CLICK_OTHER_TEAM_LEAGUE_BOARD)
                    .put("team_id", id)
                    .put("team_name", LeaderBoardDataStore.getInstance().getTeamName((int) id))
                    .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                    .buildAndDispatch();
        }

    }



}
