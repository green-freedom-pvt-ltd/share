package com.sharesmile.share.leaderboard.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.leaderboard.common.adapter.LeaderBoardAdapter;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import Models.LeagueBoard;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public abstract class BaseLeaderBoardFragment extends BaseFragment implements LeaderBoardAdapter.Parent {

    private static final String TAG = "BaseLeaderBoardFragment";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mswipeRefresh;

    @BindView(R.id.list_container)
    View listContainer;

    @BindView(R.id.container_list_item)
    CardView selfRankItem;

    LeaderBoardAdapter.LeaderBoardViewHolder selfRankHolder;

    private int myLeaderBoardItemPosition;

    LinearLayoutManager mLayoutManager;

    private List<BaseLeaderBoardItem> mleaderBoardList = new ArrayList<>();
    protected LeaderBoardAdapter mLeaderBoardAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View l = inflater.inflate(R.layout.fragment_drawer_leaderboard, null);
        ButterKnife.bind(this, l);
        init(false);
        return l;
    }

    protected void init(boolean b) {
        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        List<Workout> mWorkoutList = mWorkoutDao.queryBuilder().where(WorkoutDao.Properties.CauseId.eq(false)).list();
        mLeaderBoardAdapter = new LeaderBoardAdapter(getContext(), this, mWorkoutList, b, getActivity());
        mLayoutManager = new LinearLayoutManager(getContext());
        fetchData();
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int oldScrollY;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                    boolean scrollUp = false;
                if (dy > 0){
                    // Scroll Up (finger move up), Move down
                    scrollUp = true;
                }else {
                    // Scroll down (finger move down), Move up
                    scrollUp = false;
                }

                if (mleaderBoardList != null && !mleaderBoardList.isEmpty()){
                    renderSelfRank(scrollUp, false);
                }
            }
        });

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.leaderboard_list_item_divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        setupToolbar();
        mswipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtils.isNetworkConnected(getContext())){
                    refreshItems();
                }else {
                    MainApplication.showToast(getResources().getString(R.string.connect_to_internet));
                    hideProgressDialog();
                }
            }
        });
        mswipeRefresh.setColorSchemeResources(new int[]{R.color.sky_blue,R.color.sky_blue_dark});
        selfRankHolder =  mLeaderBoardAdapter.createMyViewHolder(selfRankItem);
    }

    @Override
    public void enableDisableSwipeRefresh(boolean enable) {
        if (mswipeRefresh != null) {
            mswipeRefresh.setEnabled(enable);
        }
    }

    protected void refreshItems(){
        Logger.d(TAG, "refreshItems");
        if (mleaderBoardList != null) {
            mleaderBoardList.clear();
            Logger.i(TAG, "LeaderBoard list cleared");
        }
        mLeaderBoardAdapter.notifyDataSetChanged();
        listContainer.setVisibility(View.GONE);
        selfRankItem.setVisibility(View.GONE);
    }

    protected abstract void setupToolbar();

    protected abstract void fetchData();

    public void render(List<BaseLeaderBoardItem> list, final int myPosition,int teamId){
        Logger.d(TAG, "render");
        int myTeamId = LeaderBoardDataStore.getInstance().getMyTeamId();
        if (list == null || list.isEmpty()){
            MainApplication.showToast(R.string.nothing_to_display);
            return;
        }

        // Step: Set the list obtained from child
        this.mleaderBoardList = list;
        this.myLeaderBoardItemPosition = myPosition;

        if(myTeamId == teamId)
        mLeaderBoardAdapter.setShowUnsync(true);
        else
            mLeaderBoardAdapter.setShowUnsync(false);
        mLeaderBoardAdapter.setData(list);

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                renderSelfRank(true, true);
            }
        });
        hideProgressDialog();
    }

    private void renderSelfRank(boolean scrollUp, boolean force){
        if (myLeaderBoardItemPosition >= 0){
            int lastItemPos = mLayoutManager.findLastVisibleItemPosition();
            Logger.d(TAG, "renderSelfRank, lastItemPos = " + lastItemPos + ", myLeaderBoardItemPos = "
                    + myLeaderBoardItemPosition + ", scrollUp = " + scrollUp + ", force = " + force);
            int limit = scrollUp ? myLeaderBoardItemPosition : myLeaderBoardItemPosition + 1;
            limit = limit + mLeaderBoardAdapter.getHeaderOffSet();
            if (lastItemPos < limit){
                showSelfRankAtBottom(force);
            }else {
                hideSelfRankFromBottom();
            }
        }
    }

    protected void showSelfRankAtBottom(boolean force){
        Logger.d(TAG, "showSelfRankAtBottom");
        if (force || !selfRankHolder.isVisible()){
            // Need to show rank at the bottom
            Logger.d(TAG, "showSelfRankAtBottom force");
            BaseLeaderBoardItem myLeaderBoardItem = mleaderBoardList.get(myLeaderBoardItemPosition);
            selfRankHolder.bindData(myLeaderBoardItem, myLeaderBoardItem.getRanking());
            selfRankHolder.show();
            mRecyclerView.setPadding(0,0,0, (int) Utils.convertDpToPixel(getContext(), 68));
        }
    }

    protected void hideSelfRankFromBottom(){
        Logger.d(TAG, "hideSelfRankFromBottom");
        if (selfRankHolder.isVisible()){
            selfRankHolder.hide();
            mRecyclerView.setPadding(0,0,0,0);
        }
    }

    protected void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
        listContainer.setVisibility(View.GONE);
    }

    protected void hideProgressDialog() {
        mProgressBar.setVisibility(View.GONE);
        listContainer.setVisibility(View.VISIBLE);
        mswipeRefresh.setRefreshing(false);
    }

    @Override
    public int toShowBanner() {
        return 0;
    }

    @Override
    public LeagueBoard getBannerData() {
        return null;
    }

    public boolean isLeagueBoard(){
        return BOARD_TYPE.LEAGUEBOARD.equals(getBoardType()) ? true : false;
    }

    public abstract BOARD_TYPE getBoardType();

    public enum BOARD_TYPE {
        REFER_LEADERBOARD,
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
