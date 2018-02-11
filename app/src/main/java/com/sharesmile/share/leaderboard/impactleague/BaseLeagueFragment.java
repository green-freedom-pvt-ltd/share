package com.sharesmile.share.leaderboard.impactleague;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sharesmile.share.leaderboard.common.BaseLeaderBoardFragment;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.leaderboard.impactleague.event.ExitLeague;
import com.sharesmile.share.leaderboard.impactleague.event.LeagueBoardDataUpdated;
import com.sharesmile.share.leaderboard.impactleague.event.LeagueDataEvent;
import com.sharesmile.share.leaderboard.impactleague.event.TeamLeaderBoardDataFetched;
import com.sharesmile.share.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.sharesmile.share.core.base.IFragmentController.OPEN_HELP_CENTER;
import static com.sharesmile.share.core.base.IFragmentController.START_MAIN_ACTIVITY;

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

    public abstract void onDataLoadEvent(LeagueDataEvent dataEvent);

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
                showExitLeagueConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showExitLeagueConfirmationDialog() {
        if (isAttachedToActivity() && !getActivity().isFinishing()){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(getString(R.string.exit_league_title));
            String leagueName = LeaderBoardDataStore.getInstance().getLeagueName() == null
                    ? "League" : LeaderBoardDataStore.getInstance().getLeagueName();
            alertDialog.setMessage(getString(R.string.exit_league_message, leagueName));
            alertDialog.setPositiveButton(getString(R.string.yes_sure), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LeaderBoardDataStore.getInstance().exitLeague();
                    showProgressDialog();
                }
            });

            alertDialog.setNegativeButton(getString(R.string.not_now), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

}
