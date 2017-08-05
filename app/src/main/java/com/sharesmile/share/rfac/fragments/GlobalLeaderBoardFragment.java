package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.sharesmile.share.Events.GlobalLeaderBoardDataUpdated;
import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.rfac.models.LeaderBoardData;
import com.sharesmile.share.rfac.models.LeaderBoardList;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.sharesmile.share.LeaderBoardDataStore.ALL_TIME_INTERVAL;
import static com.sharesmile.share.LeaderBoardDataStore.LAST_MONTH_INTERVAL;
import static com.sharesmile.share.LeaderBoardDataStore.LAST_WEEK_INTERVAL;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class GlobalLeaderBoardFragment extends BaseLeaderBoardFragment implements AdapterView.OnItemSelectedListener{

    @BindView(R.id.interval_spinner)
    Spinner intervalSpinner;

    @BindView(R.id.containerView)
    CardView myListItem;

    TextView myRank;

    TextView myProfileName;

    TextView mylastWeekDistance;

    private String interval = LAST_WEEK_INTERVAL;

    private LeaderBoard myLeaderBoard;

    HighLightArrayAdapter spinnerAdapter;

    public static GlobalLeaderBoardFragment getInstance() {
        GlobalLeaderBoardFragment fragment = new GlobalLeaderBoardFragment();
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
    protected void init() {
        super.init();
        intervalSpinner.setOnItemSelectedListener(this);
        intervalSpinner.setVisibility(View.VISIBLE);

        List<String> list = new ArrayList<>();
        list.add(getString(R.string.most_kms_last_7_days));
        list.add(getString(R.string.most_kms_last_month));
        list.add(getString(R.string.most_kms_all_time));

//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this.getContext(),
//                R.layout.spinner_item, list);
//        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        spinnerAdapter = new HighLightArrayAdapter(this.getContext(),
                R.layout.leader_board_spinner, list);
        spinnerAdapter.setDropDownViewResource(R.layout.leader_board_spinner_list_item);
        intervalSpinner.setAdapter(spinnerAdapter);
    }

    @Override
    protected void refreshItems() {
        super.refreshItems();
        LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData(LAST_WEEK_INTERVAL);
    }

    @Override
    protected void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.leaderboard));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_leaderboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_league:
                int teamId = LeaderBoardDataStore.getInstance().getMyTeamId();
                if (teamId > 0) {
                    getFragmentController().replaceFragment(LeagueBoardFragment.getInstance(), true);
                } else {
                    getFragmentController().performOperation(IFragmentController.SHOW_LEAGUE_ACTIVITY, null);
                }
                AnalyticsEvent.create(Event.ON_CLICK_CUP_ICON)
                        .put("team_id", LeaderBoardDataStore.getInstance().getMyTeamId())
                        .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                        .buildAndDispatch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void fetchData() {
        // GlobalLeaderBoard

        if (LeaderBoardDataStore.getInstance().getGlobalLeaderBoard(interval) != null){
            showGlobalLeaderBoardData(LeaderBoardDataStore.getInstance().getGlobalLeaderBoard(interval));
        }else {
            LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData(interval);
            showProgressDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlobalLeaderBoardDataUpdated event){
        if (isAttachedToActivity()){
            hideProgressDialog();
            LeaderBoardList globalLeaderBoardData
                    = LeaderBoardDataStore.getInstance().getGlobalLeaderBoard(interval);
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

    public void showGlobalLeaderBoardData(LeaderBoardList list) {
        mleaderBoardList.clear();
        int userId = MainApplication.getInstance().getUserID();
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

    private void showMyRank(LeaderBoard myLeaderBoard){
        // Need to show rank at the bottom
        myListItem.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_gold));
        myListItem.setCardElevation(3f);
        myProfileName = (TextView) myListItem.findViewById(R.id.tv_profile_name);
        mylastWeekDistance = (TextView) myListItem.findViewById(R.id.tv_distance);
        myRank = (TextView) myListItem.findViewById(R.id.id_leaderboard);

        myRank.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        myProfileName.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        mylastWeekDistance.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        mRecyclerView.setPadding(0,0,0, (int) Utils.convertDpToPixel(getContext(), 68));
        myListItem.setVisibility(View.VISIBLE);
        mLeaderBoardAdapter.createMyViewHolder(myListItem).bindData(myLeaderBoard, myLeaderBoard.getRank());
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.GLOBAL_LEADERBOARD;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItemString = parent.getItemAtPosition(position).toString();
        if (getString(R.string.most_kms_last_7_days).equals(selectedItemString)){
            interval = LAST_WEEK_INTERVAL;
        }else if (getString(R.string.most_kms_last_month).equals(selectedItemString)){
            interval = LAST_MONTH_INTERVAL;
        }else if (getString(R.string.most_kms_all_time).equals(selectedItemString)){
            interval = ALL_TIME_INTERVAL;
        }
        spinnerAdapter.setSelection(position);
        fetchData();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    class HighLightArrayAdapter extends ArrayAdapter<String> {

        private int mSelectedIndex = -1;

        public void setSelection(int position) {
            mSelectedIndex =  position;
            notifyDataSetChanged();
        }

        public HighLightArrayAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View itemView =  super.getDropDownView(position, convertView, parent);

            if (position == mSelectedIndex) {
                itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_very_light));
            } else {
                itemView.setBackgroundColor(Color.WHITE);
            }

            return itemView;
        }
    }
}
