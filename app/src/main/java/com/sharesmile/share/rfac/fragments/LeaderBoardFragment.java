package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import Models.TeamBoard;
import Models.TeamLeaderBoard;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by piyush on 8/30/16.
 */
public class LeaderBoardFragment extends BaseFragment implements LeaderBoardAdapter.ItemClickListener {

    private static final String BUNDLE_LEAGUE_BOARD = "bundle_league_board";
    RecyclerView mRecyclerView;
    private List<LeaderBoard> mleaderBoardList = new ArrayList<>();
    LeaderBoardAdapter mLeaderBoardAdapter;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mswipeRefresh;

    @BindView(R.id.tv_leaderboard_data)
    TextView mInfoView;

    @BindView(R.id.banner)
    ImageView mBanner;

    LeaderBoardDao mleaderBoardDao = MainApplication.getInstance().getDbWrapper().getLeaderBoardDao();
    private boolean mShowLeagueBoard = false;
    private BOARD_TYPE mBoard;
    private String mBannerUrl;

    public enum BOARD_TYPE {
        LEADERBOARD,
        TEAMBAORD,
        TEAMLEADERBAORD;

        public static BOARD_TYPE getBoardType(int board) {

            switch (board) {
                case 1:
                    return LEADERBOARD;
                case 2:
                    return TEAMBAORD;
                case 3:
                    return TEAMLEADERBAORD;
                default:
                    return LEADERBOARD;
            }
        }

        public static int getBoardID(BOARD_TYPE board) {

            switch (board) {
                case LEADERBOARD:
                    return 1;
                case TEAMBAORD:
                    return 2;
                case TEAMLEADERBAORD:
                    return 3;
                default:
                    return 1;
            }
        }
    }

    public static LeaderBoardFragment getInstance(BOARD_TYPE board) {

        LeaderBoardFragment fragment = new LeaderBoardFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_LEAGUE_BOARD, BOARD_TYPE.getBoardID(board));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            mBoard = BOARD_TYPE.getBoardType(arg.getInt(BUNDLE_LEAGUE_BOARD));
            mShowLeagueBoard = mBoard == BOARD_TYPE.TEAMBAORD ? true : false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View l = inflater.inflate(R.layout.fragment_drawer_leaderboard, null);
        ButterKnife.bind(this, l);
        mRecyclerView = (RecyclerView) l.findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar) l.findViewById(R.id.progress_bar);
        mswipeRefresh = (SwipeRefreshLayout) l.findViewById(R.id.swipeRefreshLayout);
        init();
        return l;
    }

    private void init() {
        if (mLeaderBoardAdapter == null) {
            mLeaderBoardAdapter = new LeaderBoardAdapter(getContext(), mleaderBoardList, mShowLeagueBoard, this);
            mLeaderBoardAdapter.setData(mleaderBoardList);
            fetchData();
        } else {
            if (mBoard == BOARD_TYPE.TEAMBAORD) {
                setBannerImage();
            }
        }
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupToolbar();

        mswipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();

            }
        });
        mswipeRefresh.setColorSchemeResources(R.color.sky_blue);

    }

    private void fetchData() {
        if (mBoard == BOARD_TYPE.LEADERBOARD) {
            EventBus.getDefault().register(this);
            SyncHelper.syncLeaderBoardData(getContext());
        } else {
            mInfoView.setVisibility(View.GONE);
            if (mBoard == BOARD_TYPE.TEAMBAORD) {
                fetchTeamBoardData();
            } else {
                fetchTeamLeaderBoardData();
            }
        }
        showProgressDialog();
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        if (mBoard == BOARD_TYPE.LEADERBOARD) {
            getFragmentController().updateToolBar(getResources().getString(R.string.leaderboard), true);
        } else {
            mInfoView.setVisibility(View.GONE);
            if (mBoard == BOARD_TYPE.TEAMBAORD) {
                getFragmentController().updateToolBar(getResources().getString(R.string.impact_league), true);
            } else {
                getFragmentController().updateToolBar(getResources().getString(R.string.team_leader_board), true);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mBoard == BOARD_TYPE.LEADERBOARD) {
            inflater.inflate(R.menu.menu_leaderboard, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_league:
                int teamId = SharedPrefsManager.getInstance().getInt(Constants.PREF_LEAGUE_TEAM_ID, 0);
                if (teamId > 0) {
                    getFragmentController().replaceFragment(getInstance(BOARD_TYPE.TEAMBAORD), true);
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

        if (mBoard == BOARD_TYPE.TEAMBAORD) {
            fetchTeamBoardData();
        } else if (mBoard == BOARD_TYPE.TEAMLEADERBAORD) {
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
        NetworkDataProvider.doGetCallAsync(Urls.getTeamLeaderBoardUrl(), new NetworkAsyncCallback<TeamLeaderBoard>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                ne.printStackTrace();
            }

            @Override
            public void onNetworkSuccess(TeamLeaderBoard board) {
                mleaderBoardList.clear();
                for (TeamLeaderBoard.UserDetails team : board.getTeamList()) {
                    Float distance = 0f;
                    if (team.getLeagueTotalDistance() != null && team.getLeagueTotalDistance().getTotalDistance() != null) {
                        distance = team.getLeagueTotalDistance().getTotalDistance();
                    }
                    mleaderBoardList.add(team.getUser().convertToLeaderBoard(distance));
                }

                hideProgressDialog();
                mLeaderBoardAdapter.setData(mleaderBoardList);
            }
        });
    }

    private void fetchTeamBoardData() {
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
                mBannerUrl = null;
                for (TeamBoard.Team team : board.getTeamList()) {
                    mleaderBoardList.add(team.convertToLeaderBoard());
                    mBannerUrl = team.getBanner();
                }
                setBannerImage();

                hideProgressDialog();
                mLeaderBoardAdapter.setData(mleaderBoardList);

            }
        });
    }

    private void setBannerImage() {
        if (!TextUtils.isEmpty(mBannerUrl)) {
            Picasso.with(getContext()).load(mBannerUrl).placeholder(R.drawable.cause_image_placeholder).into(mBanner);
            mBanner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(int id) {
        getFragmentController().replaceFragment(getInstance(BOARD_TYPE.TEAMLEADERBAORD), true);
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
