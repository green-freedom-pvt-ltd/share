package com.sharesmile.share.leaderboard.global;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sharesmile.share.leaderboard.common.BaseLeaderBoardFragment;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;
import com.sharesmile.share.leaderboard.global.model.LeaderBoardData;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.leaderboard.global.model.LeaderBoardList;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;

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
 * Created by ankitmaheshwari on 8/5/17.
 */

public class GlobalLeaderBoardFragment extends BaseLeaderBoardFragment implements AdapterView.OnItemSelectedListener{

    @BindView(R.id.interval_spinner)
    Spinner intervalSpinner;

    private String interval = LAST_WEEK_INTERVAL;

    HighLightArrayAdapter spinnerAdapter;
    private boolean isFirstSpinnerItemSelected = false;

    private LeaderBoardList origData;

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

        spinnerAdapter = new HighLightArrayAdapter(this.getContext(),
                R.layout.leader_board_spinner, list);
        spinnerAdapter.setDropDownViewResource(R.layout.leader_board_spinner_list_item);
        intervalSpinner.setAdapter(spinnerAdapter);
//        // Set last month by default
//        intervalSpinner.setSelection(1);
    }

    @Override
    protected void refreshItems() {
        super.refreshItems();
        LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData(interval);
    }

    @Override
    protected void setupToolbar() {
//        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.leaderboard));
        getFragmentController().setToolbarElevation(0);
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        getFragmentController().setToolbarElevation(2);
//    }

    @Override
    public void onDestroyView() {
        getFragmentController().setToolbarElevation(2);
        super.onDestroyView();
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_leaderboard, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.item_league:
//                if (LeaderBoardDataStore.getInstance().toShowLeague()){
//                    getFragmentController().replaceFragment(LeagueBoardFragment.getInstance(), true);
//                }else {
//                    getFragmentController().performOperation(IFragmentController.SHOW_LEAGUE_ACTIVITY, null);
//                }
//                AnalyticsEvent.create(Event.ON_CLICK_CUP_ICON)
//                        .put("team_id", LeaderBoardDataStore.getInstance().getMyTeamId())
//                        .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
//                        .buildAndDispatch();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    protected void fetchData() {
        // GlobalLeaderBoard
        origData = LeaderBoardDataStore.getInstance().getGlobalLeaderBoard(interval);
        if (origData != null){
            prepareDatasetAndRender();
        }else {
            LeaderBoardDataStore.getInstance().updateGlobalLeaderBoardData(interval);
            showProgressDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlobalLeaderBoardDataUpdated event){
        if (isAttachedToActivity()){
            hideProgressDialog();
            origData = LeaderBoardDataStore.getInstance().getGlobalLeaderBoard(interval);
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
        List<LeaderBoardData> origList = origData.getLeaderBoardList();
        for (int i=0; i < origList.size(); i++) {
            LeaderBoardData data = origList.get(i);
            if (myUserId == data.getUserid()){
                myPos = i;
            }
            itemList.add(data.getLeaderBoardDbObject());
        }
        render(itemList, myPos,0);
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
        if (!isFirstSpinnerItemSelected){
            isFirstSpinnerItemSelected = true;
        }else {
            AnalyticsEvent.create(Event.ON_CHANGE_GLOBAL_LEADERBOARD_RANGE).put("selected_range", interval).buildAndDispatch();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
