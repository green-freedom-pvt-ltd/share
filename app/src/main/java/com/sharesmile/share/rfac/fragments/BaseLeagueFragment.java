package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sharesmile.share.Events.ExitLeague;
import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.Events.LeagueDataEvent;
import com.sharesmile.share.Events.TeamLeaderBoardDataFetched;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.sharesmile.share.core.IFragmentController.OPEN_HELP_CENTER;
import static com.sharesmile.share.core.IFragmentController.START_MAIN_ACTIVITY;

/**
 * Created by ankitmaheshwari on 10/9/17.
 */

public abstract class BaseLeagueFragment extends BaseLeaderBoardFragment {

    private static final String TAG = "BaseLeagueFragment";

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

    abstract void onDataLoadEvent(LeagueDataEvent dataEvent);

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TeamLeaderBoardDataFetched event){
        onDataLoadEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LeagueBoardDataUpdated event){
        onDataLoadEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ExitLeague event){
        // Successful exit from League, need to take the user back to home screen
        if (isAttachedToActivity()){
            if (event.isSuccess()){
                getFragmentController().performOperation(START_MAIN_ACTIVITY, null);
                getFragmentController().exit();
            }else {
                hideProgressDialog();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_league_board, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                getFragmentController().performOperation(OPEN_HELP_CENTER,false);
                return true;
            case R.id.menu_exit:
                LeaderBoardDataStore.getInstance().exitLeague();
                showProgressDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
