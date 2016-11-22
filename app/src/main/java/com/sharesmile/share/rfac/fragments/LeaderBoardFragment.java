package com.sharesmile.share.rfac.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.LeaderBoardDao;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import activities.ImpactLeagueActivity;

/**
 * Created by piyush on 8/30/16.
 */
public class LeaderBoardFragment extends BaseFragment {

    RecyclerView mRecyclerView;
    private List<LeaderBoard> mleaderBoardList;
    LeaderBoardAdapter mLeaderBoardAdapter;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mswipeRefresh;

    LeaderBoardDao mleaderBoardDao = MainApplication.getInstance().getDbWrapper().getLeaderBoardDao();
    private View badgeIndictor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View l = inflater.inflate(R.layout.fragment_drawer_leaderboard, null);
        mRecyclerView = (RecyclerView) l.findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar) l.findViewById(R.id.progress_bar);
        mswipeRefresh = (SwipeRefreshLayout) l.findViewById(R.id.swipeRefreshLayout);
        getFragmentController().updateToolBar(getResources().getString(R.string.leaderboard), true);
        init();
        EventBus.getDefault().register(this);
//        SyncTaskManger.fetchLeaderBoardData(getContext());
        SyncHelper.syncLeaderBoardData(getContext());
        showProgressDialog();
        mswipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();

            }
        });

        mswipeRefresh.setColorSchemeResources(R.color.sky_blue);

        setupToolbar();
        return l;
    }



    private void init() {
        mLeaderBoardAdapter = new LeaderBoardAdapter(getContext(), mleaderBoardList);
        mLeaderBoardAdapter.setData(mleaderBoardList);
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        MenuItem messageItem = menu.findItem(R.id.item_message);

        RelativeLayout badge = (RelativeLayout) messageItem.getActionView();
        badgeIndictor = badge.findViewById(R.id.badge_indicator);
        boolean hasUnreadMessage = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_UNREAD_MESSAGE, false);
        badgeIndictor.setVisibility(hasUnreadMessage ? View.VISIBLE : View.GONE);

        badge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentController().performOperation(IFragmentController.SHOW_LEAGUE_ACTIVITY, null);
            }
        });
    }

    public void refreshItems(){
        if(mleaderBoardList != null) {
            mleaderBoardList.clear();
            Logger.i("LeaderBoard", mleaderBoardList.toString());

        }
        mleaderBoardDao.deleteAll();
        mLeaderBoardAdapter.notifyDataSetChanged();
        SyncHelper.syncLeaderBoardData(getContext());

    }

    private void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DBEvent.LeaderBoardDataUpdated leaderBoardDataUpdated) {
        fetchLeaderBoardDataFromDb();
    }

    public void fetchLeaderBoardDataFromDb() {
        mleaderBoardList = mleaderBoardDao.queryBuilder().orderDesc(LeaderBoardDao.Properties.Last_week_distance).limit(30).list();
        mLeaderBoardAdapter.setData(mleaderBoardList);
        hideProgressDialog();
    }


    private void hideProgressDialog() {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mswipeRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

}
