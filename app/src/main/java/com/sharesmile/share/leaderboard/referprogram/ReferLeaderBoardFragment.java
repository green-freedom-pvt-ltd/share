package com.sharesmile.share.leaderboard.referprogram;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.leaderboard.common.BaseLeaderBoardFragment;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;
import com.sharesmile.share.leaderboard.global.GlobalLeaderBoardDataUpdated;
import com.sharesmile.share.leaderboard.global.model.LeaderBoardData;
import com.sharesmile.share.leaderboard.global.model.LeaderBoardList;
import com.sharesmile.share.leaderboard.referprogram.model.ReferProgramBoard;
import com.sharesmile.share.refer_program.model.ReferProgramList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.sharesmile.share.leaderboard.LeaderBoardDataStore.ALL_TIME_INTERVAL;
import static com.sharesmile.share.leaderboard.LeaderBoardDataStore.LAST_MONTH_INTERVAL;
import static com.sharesmile.share.leaderboard.LeaderBoardDataStore.LAST_WEEK_INTERVAL;

/**
 * Created by parth on 17/6/18.
 */

public class ReferLeaderBoardFragment extends BaseLeaderBoardFragment{


    private ReferProgramList origData;

    public static ReferLeaderBoardFragment getInstance() {
        ReferLeaderBoardFragment fragment = new ReferLeaderBoardFragment();
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
    protected void init(boolean b) {
        super.init(true);
    }

    @Override
    protected void refreshItems() {
        super.refreshItems();
        LeaderBoardDataStore.getInstance().updateReferLeaderBoardData();
    }

    @Override
    protected void setupToolbar() {
//        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.share_a_meal_challenge));
        getFragmentController().setToolbarElevation(0);
    }

    @Override
    public void onDestroyView() {
        getFragmentController().setToolbarElevation(2);
        super.onDestroyView();
    }

    @Override
    protected void fetchData() {
        // GlobalLeaderBoard
        origData = LeaderBoardDataStore.getInstance().getReferProgramList();
        if (origData != null){
            prepareDatasetAndRender();
        }else {
            LeaderBoardDataStore.getInstance().updateReferLeaderBoardData();
            showProgressDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReferLeaderBoardDataUpdated event){
        if (isAttachedToActivity()){
            hideProgressDialog();
            origData = LeaderBoardDataStore.getInstance().getReferProgramList();
            if (event.isSuccess()){
                prepareDatasetAndRender();
            }else {
                if (origData != null){
                    prepareDatasetAndRender();
                    MainApplication.showToast("Network Error, Couldn't refresh");
                }else {
                    MainApplication.showToast("Network Error, Please try again");
                }
                hideProgressDialog();
            }
        }
    }

    public void prepareDatasetAndRender() {
        List<BaseLeaderBoardItem> itemList = new ArrayList<>();
        int myUserId = MainApplication.getInstance().getUserID();
        int myPos = -1;
        List<ReferProgramBoard> origList = origData.getReferProgramBoardList();
        for (int i=0; i < origList.size(); i++) {
            ReferProgramBoard data = origList.get(i);
            if (myUserId == data.getUserId()){
                myPos = i;
            }
            itemList.add(data.getLeaderBoardDbObject());
        }
        render(itemList, myPos,0);
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.REFER_LEADERBOARD;
    }

    @Override
    public void onItemClick(long id) {
        // No action on click
    }

    @Override
    public int getMyId() {
        return MainApplication.getInstance().getUserID();
    }

    @Override
    public boolean toShowLogo() {
        return true;
    }
}
