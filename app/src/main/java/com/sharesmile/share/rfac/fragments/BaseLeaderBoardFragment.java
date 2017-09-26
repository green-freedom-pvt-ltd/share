package com.sharesmile.share.rfac.fragments;

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

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.rfac.models.BaseLeaderBoardItem;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.List;

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

    private int myLeaderBoardItemPosition;

    LinearLayoutManager mLayoutManager;

    private List<BaseLeaderBoardItem> mleaderBoardList = new ArrayList<>();
    protected LeaderBoardAdapter mLeaderBoardAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View l = inflater.inflate(R.layout.fragment_drawer_leaderboard, null);
        ButterKnife.bind(this, l);
        init();
        return l;
    }

    protected void init() {
        mLeaderBoardAdapter = new LeaderBoardAdapter(getContext(), this);
        mLayoutManager = new LinearLayoutManager(getContext());
        fetchData();
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setNestedScrollingEnabled(false);
//        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Logger.d(TAG, "onScrollStateChanged, newState = " + newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Logger.d(TAG, "onScrolled");
                if (mleaderBoardList != null && !mleaderBoardList.isEmpty()){
                    renderSelfRank();
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
                    MainApplication.showToast("Please connect to Internet");
                }
            }
        });
        mswipeRefresh.setColorSchemeResources(R.color.sky_blue);

    }

    protected void enableDisableSwipeRefresh(boolean enable) {
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
    }

    protected abstract void setupToolbar();

    protected abstract void fetchData();

    public void render(List<BaseLeaderBoardItem> list, final int myPosition){
        Logger.d(TAG, "render");
        if (list == null || list.isEmpty()){
            MainApplication.showToast(R.string.nothing_to_display);
            return;
        }

        // Step: Set the list obtained from child
        this.mleaderBoardList = list;
        this.myLeaderBoardItemPosition = myPosition;

        mLeaderBoardAdapter.setData(list);

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                renderSelfRank();
            }
        });
        hideProgressDialog();
    }

    private void renderSelfRank(){
        int lastItemPos = mLayoutManager.findLastVisibleItemPosition();
        Logger.d(TAG, "renderSelfRank, lastItemPos = " + lastItemPos + ", myLeaderBoardItemPos = "
                + myLeaderBoardItemPosition);
        if (lastItemPos < myLeaderBoardItemPosition){
            showSelfRankAtBottom();
        }else {
            hideSelfRankFromBottom();
        }
    }

    protected void showSelfRankAtBottom(){
        Logger.d(TAG, "showSelfRankAtBottom");
        if (myLeaderBoardItemPosition >= 0){
            // Need to show rank at the bottom
            selfRankItem.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_gold));
            selfRankItem.setCardElevation(3f);
            mRecyclerView.setPadding(0,0,0, (int) Utils.convertDpToPixel(getContext(), 68));
            selfRankItem.setVisibility(View.VISIBLE);
            BaseLeaderBoardItem myLeaderBoardItem = mleaderBoardList.get(myLeaderBoardItemPosition);
            mLeaderBoardAdapter.createMyViewHolder(selfRankItem).bindData(myLeaderBoardItem, myLeaderBoardItem.getRanking());
        }
    }

    protected void hideSelfRankFromBottom(){
        Logger.d(TAG, "hideSelfRankFromBottom");
        selfRankItem.setVisibility(View.GONE);
        mRecyclerView.setPadding(0,0,0,0);
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
