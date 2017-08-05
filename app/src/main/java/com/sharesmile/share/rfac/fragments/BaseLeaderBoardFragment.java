package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.LeaderBoardDataStore.LAST_WEEK_INTERVAL;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public abstract class BaseLeaderBoardFragment extends BaseFragment
        implements LeaderBoardAdapter.ItemClickListener {

    private static final String TAG = "BaseLeaderBoardFragment";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mswipeRefresh;

    @BindView(R.id.list_container)
    View listContainer;

    private List<LeaderBoard> mleaderBoardList = new ArrayList<>();
    LeaderBoardAdapter mLeaderBoardAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View l = inflater.inflate(R.layout.fragment_drawer_leaderboard, null);
        ButterKnife.bind(this, l);
        EventBus.getDefault().register(this);
        init();
        return l;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    protected void init() {
        mLeaderBoardAdapter = new LeaderBoardAdapter(getContext(), isLeagueBoard(), this);
        fetchData();
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupToolbar();
        mswipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtils.isNetworkConnected(getContext())){
                    refreshItems();
                }else {
                    MainApplication.showToast("Please connect to Internet");
                }

            }
        });
        mswipeRefresh.setColorSchemeResources(R.color.sky_blue);

    }

    protected void refreshItems(){
        if (mleaderBoardList != null) {
            mleaderBoardList.clear();
            Logger.i(TAG, "LeaderBoard list cleared");
        }
        mLeaderBoardAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.GONE);
        myListItem.setVisibility(View.GONE);
        if (mBoard == BOARD_TYPE.LEAGUEBOARD) {
            LeaderBoardDataStore.getInstance().updateLeagueBoardData();
        } else if (mBoard == BOARD_TYPE.TEAM_LEADERBAORD) {
            LeaderBoardDataStore.getInstance().updateTeamLeaderBoardData(mTeamId);
        } else {
            LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData(LAST_WEEK_INTERVAL);
        }
    }

    protected abstract void setupToolbar();

    protected abstract void fetchData();




    protected void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        myListItem.setVisibility(View.GONE);
    }

    protected void hideProgressDialog() {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mswipeRefresh.setRefreshing(false);
    }

    public boolean isLeagueBoard(){
        return BOARD_TYPE.LEAGUEBOARD.equals(getBoardType()) ? true : false;
    }

    public abstract BOARD_TYPE getBoardType();


    public enum BOARD_TYPE {
        GLOBAL_LEADERBOARD,
        LEAGUEBOARD,
        TEAM_LEADERBAORD;

        public static BOARD_TYPE getBoardType(int board) {

            switch (board) {
                case 1:
                    return GLOBAL_LEADERBOARD;
                case 2:
                    return LEAGUEBOARD;
                case 3:
                    return TEAM_LEADERBAORD;
                default:
                    return GLOBAL_LEADERBOARD;
            }
        }

        public static int getBoardID(BOARD_TYPE board) {

            switch (board) {
                case GLOBAL_LEADERBOARD:
                    return 1;
                case LEAGUEBOARD:
                    return 2;
                case TEAM_LEADERBAORD:
                    return 3;
                default:
                    return 1;
            }
        }
    }


}
