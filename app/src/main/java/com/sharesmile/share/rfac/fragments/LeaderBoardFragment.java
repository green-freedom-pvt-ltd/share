package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.LeaderBoardDao;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import Models.TeamBoard;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by piyush on 8/30/16.
 */
public class LeaderBoardFragment extends BaseFragment {

    private static final String BUNDLE_LEAGUE_BOARD = "bundle_league_board";
    RecyclerView mRecyclerView;
    private List<LeaderBoard> mleaderBoardList;
    LeaderBoardAdapter mLeaderBoardAdapter;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mswipeRefresh;

    @BindView(R.id.tv_leaderboard_data)
    TextView mInfoView;

    LeaderBoardDao mleaderBoardDao = MainApplication.getInstance().getDbWrapper().getLeaderBoardDao();
    private View badgeIndictor;
    private boolean mShowLeagueBoard = false;


    public static LeaderBoardFragment getInstance(Boolean showLeagueLeaderBoard) {

        LeaderBoardFragment fragment = new LeaderBoardFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_LEAGUE_BOARD, showLeagueLeaderBoard);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            mShowLeagueBoard = arg.getBoolean(BUNDLE_LEAGUE_BOARD, false);
        }

        Log.i("Anshul", " mShowLeagueBoard : " + mShowLeagueBoard);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View l = inflater.inflate(R.layout.fragment_drawer_leaderboard, null);
        ButterKnife.bind(this, l);
        mRecyclerView = (RecyclerView) l.findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar) l.findViewById(R.id.progress_bar);
        mswipeRefresh = (SwipeRefreshLayout) l.findViewById(R.id.swipeRefreshLayout);
        getFragmentController().updateToolBar(getResources().getString(R.string.leaderboard), true);
        init();
        if (!mShowLeagueBoard) {
            EventBus.getDefault().register(this);
            SyncHelper.syncLeaderBoardData(getContext());
        } else {
            mInfoView.setVisibility(View.GONE);
            fetchTeamLeaderBoardData();
        }
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
        mleaderBoardList = new ArrayList<>();
        mLeaderBoardAdapter = new LeaderBoardAdapter(getContext(), mleaderBoardList, mShowLeagueBoard);
        mLeaderBoardAdapter.setData(mleaderBoardList);
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mShowLeagueBoard) {
            inflater.inflate(R.menu.menu_leaderboard, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_league:
                String teamCode = SharedPrefsManager.getInstance().getString(Constants.PREF_LEAGUE_TEAM_CODE, "");
                if (!TextUtils.isEmpty(teamCode)) {
                    getFragmentController().replaceFragment(getInstance(true), true);
                } else {
                    getFragmentController().performOperation(IFragmentController.SHOW_LEAGUE_ACTIVITY, null);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void refreshItems() {
        if (mleaderBoardList != null) {
            mleaderBoardList.clear();
            Logger.i("LeaderBoard", mleaderBoardList.toString());

        }
        if (mShowLeagueBoard) {
            fetchTeamLeaderBoardData();
        } else {
            mleaderBoardDao.deleteAll();
            mLeaderBoardAdapter.notifyDataSetChanged();
            SyncHelper.syncLeaderBoardData(getContext());
        }

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

    private void fetchTeamLeaderBoardData() {
        NetworkDataProvider.doGetCallAsync(Urls.getTeamBoardUrl(), new NetworkAsyncCallback<TeamBoard>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                ne.printStackTrace();
            }

            @Override
            public void onNetworkSuccess(TeamBoard board) {
                mleaderBoardList.clear();
                for (TeamBoard.Team team : board.getTeamList()) {
                    mleaderBoardList.add(team.convertToLeaderBoard());
                }

                hideProgressDialog();
                mLeaderBoardAdapter.setData(mleaderBoardList);

            }
        });
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
