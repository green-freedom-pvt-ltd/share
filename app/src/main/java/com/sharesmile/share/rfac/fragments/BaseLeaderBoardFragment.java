package com.sharesmile.share.rfac.fragments;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;

import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public abstract class BaseLeaderBoardFragment extends BaseFragment
        implements LeaderBoardAdapter.ItemClickListener {

    private static final String TAG = "BaseLeaderBoardFragment";

    RecyclerView mRecyclerView;
    private List<LeaderBoard> mleaderBoardList = new ArrayList<>();
    LeaderBoardAdapter mLeaderBoardAdapter;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mswipeRefresh;


}
