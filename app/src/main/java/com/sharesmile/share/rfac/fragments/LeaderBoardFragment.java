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
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.rfac.models.LeaderBoardData;
import com.sharesmile.share.rfac.models.LeaderBoardList;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;
import com.squareup.picasso.Picasso;

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
    private static final String BUNDLE_TEAM_ID = "bundle_team_id";
    private static final String TAG = "LeaderBoardFragment";

    RecyclerView mRecyclerView;
    private List<LeaderBoard> mleaderBoardList = new ArrayList<>();
    LeaderBoardAdapter mLeaderBoardAdapter;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mswipeRefresh;

    @BindView(R.id.tv_leaderboard_data)
    TextView mInfoView;

    @BindView(R.id.banner)
    ImageView mBanner;

    @BindView(R.id.containerView)
    CardView myListItem;

    TextView myRank;

    TextView myProfileName;

    TextView mylastWeekDistance;

    private boolean mShowLeagueBoard = false;
    private BOARD_TYPE mBoard;
    private String mBannerUrl;
    private int mTeamId;
    private LeaderBoard myLeaderBoard;

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

    public static LeaderBoardFragment getInstance(BOARD_TYPE board) {

        LeaderBoardFragment fragment = new LeaderBoardFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_LEAGUE_BOARD, BOARD_TYPE.getBoardID(board));
        fragment.setArguments(bundle);
        return fragment;
    }

    public static LeaderBoardFragment getInstance(BOARD_TYPE board,int teamId) {
        LeaderBoardFragment fragment = getInstance(board);
        Bundle bundle = fragment.getArguments();
        if (board == BOARD_TYPE.TEAM_LEADERBAORD) {
            bundle.putInt(BUNDLE_TEAM_ID, teamId);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            mBoard = BOARD_TYPE.getBoardType(arg.getInt(BUNDLE_LEAGUE_BOARD));
            mShowLeagueBoard = mBoard == BOARD_TYPE.LEAGUEBOARD ? true : false;
            mTeamId = arg.getInt(BUNDLE_TEAM_ID);
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
        mLeaderBoardAdapter = new LeaderBoardAdapter(getContext(), mShowLeagueBoard, this);
        fetchData();
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
        if (mBoard == BOARD_TYPE.GLOBAL_LEADERBOARD) {
            // GlobalLeaderBoard
            if (LeaderBoardDataStore.getInstance().getGlobalLeaderBoard() != null){
                showGlobalLeaderBoardData(LeaderBoardDataStore.getInstance()
                        .getGlobalLeaderBoard());
                MainApplication.showToast("Swipe to Refresh");
            }else {
                LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData();
                showProgressDialog();
            }
        } else {
            // LeagueBoard
            mInfoView.setVisibility(View.GONE);
            if (mBoard == BOARD_TYPE.LEAGUEBOARD) {
                if (LeaderBoardDataStore.getInstance().getLeagueBoard() != null){
                    showLeagueBoardData(LeaderBoardDataStore.getInstance().getLeagueBoard());
                    MainApplication.showToast("Swipe to Refresh");
                }else {
                    LeaderBoardDataStore.getInstance().updateLeagueBoardData();
                    showProgressDialog();
                }
            } else {
                // League's Team LeaderBoard
                LeaderBoardDataStore.getInstance().updateTeamLeaderBoardData(mTeamId);
                showProgressDialog();
            }
        }
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        if (mBoard == BOARD_TYPE.GLOBAL_LEADERBOARD) {
            setToolbarTitle(getResources().getString(R.string.leaderboard));
        } else {
            mInfoView.setVisibility(View.GONE);
            if (mBoard == BOARD_TYPE.LEAGUEBOARD) {
                setToolbarTitle(getResources().getString(R.string.impact_league));
            } else {
                setToolbarTitle(getResources().getString(R.string.team_leader_board));
            }
        }
    }

    private void setToolbarTitle(String title){
        Logger.d(TAG, "setToolbarTitle: " + title);
        getFragmentController().updateToolBar(title, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mBoard == BOARD_TYPE.GLOBAL_LEADERBOARD) {
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
                    getFragmentController().replaceFragment(getInstance(BOARD_TYPE.LEAGUEBOARD), true);
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
            Logger.i(TAG, "LeaderBoard list cleared");
        }
        mLeaderBoardAdapter.notifyDataSetChanged();
        showProgressDialog();
        if (mBoard == BOARD_TYPE.LEAGUEBOARD) {
            LeaderBoardDataStore.getInstance().updateLeagueBoardData();
        } else if (mBoard == BOARD_TYPE.TEAM_LEADERBAORD) {
            LeaderBoardDataStore.getInstance().updateTeamLeaderBoardData(mTeamId);
        } else {
            LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData();
        }
    }

    private void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        myListItem.setVisibility(View.GONE);
    }

    public void showGlobalLeaderBoardData(LeaderBoardList list) {
        mleaderBoardList.clear();
        int userId = SharedPrefsManager.getInstance().getInt(Constants.PREF_USER_ID);
        boolean isShowingMyRank = false;
        for (LeaderBoardData data : list.getLeaderBoardList()){
            if (data.getRank() > 50 && userId == data.getUserid()){
                myLeaderBoard = data.getLeaderBoardDbObject();
                isShowingMyRank = true;
            }else if (data.getRank() > 0 && data.getRank() <= 50){
                mleaderBoardList.add(data.getRank() - 1, data.getLeaderBoardDbObject());
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
        if (isAttachedToActivity()){
            hideProgressDialog();
            LeaderBoardList globalLeaderBoardData = LeaderBoardDataStore.getInstance().getGlobalLeaderBoard();
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
        if (isAttachedToActivity()){
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
        if (mTeamId == event.getTeamId() && isAttachedToActivity()){
            // Check if we received TeamLeaderBoard data for the correct teamId
            // &&
            // Check if fragment is still on display
            TeamLeaderBoard board = null;
            if (event.isSuccess()){
                board = event.getTeamLeaderBoard();
            }else if (mTeamId == SharedPrefsManager.getInstance().getInt(Constants.PREF_LEAGUE_TEAM_ID)){
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
            Picasso.with(getContext()).load(mBannerUrl).placeholder(R.drawable.cause_image_placeholder).into(mBanner);
            mBanner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(long id) {
        getFragmentController().replaceFragment(getInstance(BOARD_TYPE.TEAM_LEADERBAORD,(int)id), true);
    }

    private void hideProgressDialog() {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mswipeRefresh.setRefreshing(false);
    }

}
