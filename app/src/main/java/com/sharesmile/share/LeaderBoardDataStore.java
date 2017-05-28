package com.sharesmile.share;

import android.content.Context;
import android.text.TextUtils;

import com.sharesmile.share.Events.GlobalLeaderBoardDataUpdated;
import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.Events.TeamLeaderBoardDataFetched;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.LeaderBoardList;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Models.TeamBoard;
import Models.TeamLeaderBoard;

import static com.sharesmile.share.R.string.team;

/**
 * Created by ankitmaheshwari on 5/12/17.
 */

public class LeaderBoardDataStore {

    private static final String TAG = "LeaderBoardDataStore";

    private static LeaderBoardDataStore uniqueInstance;

    private TeamBoard leagueBoard;
    private LeaderBoardList globalLeaderBoard;
    private TeamLeaderBoard myTeamLeaderBoard;
    private int leagueTeamId;
    private Context context;

    private LeaderBoardDataStore(Context appContext){
        this.context = appContext;
        this.leagueTeamId = SharedPrefsManager.getInstance().getInt(Constants.PREF_LEAGUE_TEAM_ID);
        this.globalLeaderBoard = SharedPrefsManager.getInstance()
                .getObject(Constants.PREF_GLOBAL_LEADERBOARD_CACHED_DATA, LeaderBoardList.class);
        this.leagueBoard = SharedPrefsManager.getInstance()
                .getObject(Constants.PREF_LEAGUEBOARD_CACHED_DATA, TeamBoard.class);
        this.myTeamLeaderBoard = SharedPrefsManager.getInstance()
                .getObject(Constants.PREF_MY_TEAM_LEADERBOARD_CACHED_DATA, TeamLeaderBoard.class);
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique LeaderBoardDataStore instance
     */
    public static LeaderBoardDataStore getInstance() {
        if (uniqueInstance == null) {
            throw new IllegalStateException(
                    "LeaderBoardDataStore is not initialized, call initialize(applicationContext) " +
                            "static method first");
        }
        return uniqueInstance;
    }

    /**
     Initialize this class using application Context,
     should be called once in the beginning by any application Component

     @param appContext application context
     */
    public static void initialize(Context appContext) {
        if (appContext == null) {
            throw new NullPointerException("Provided application context is null");
        }
        if (uniqueInstance == null) {
            synchronized (LeaderBoardDataStore.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new LeaderBoardDataStore(appContext);
                }
            }
        }
    }

    public TeamBoard getLeagueBoard() {
        return leagueBoard;
    }

    public LeaderBoardList getGlobalLeaderBoard() {
        return globalLeaderBoard;
    }

    public TeamLeaderBoard getMyTeamLeaderBoard() {
        return myTeamLeaderBoard;
    }

    public int getMyTeamId() {
        return leagueTeamId;
    }

    public String getLeagueName(){
        if (leagueBoard != null ){
            Iterator<TeamBoard.Team> iter = leagueBoard.getTeamList().iterator();
            while (iter.hasNext()){
                TeamBoard.Team team = iter.next();
                if (!TextUtils.isEmpty(team.getLeagueName())){
                    return team.getLeagueName();
                }
            }
        }
        return null;
    }

    public String getMyTeamName(){
        if (myTeamLeaderBoard != null && myTeamLeaderBoard.getTeamList().get(0) != null){
            Iterator<TeamLeaderBoard.UserDetails> iter = myTeamLeaderBoard.getTeamList().iterator();
            while (iter.hasNext()){
                TeamLeaderBoard.UserDetails team = iter.next();
                if (!TextUtils.isEmpty(team.getTeamName())){
                    return team.getTeamName();
                }
            }
        }
        return null;
    }

    public String getTeamName(int teamId){
        if (leagueBoard != null){
            Iterator<TeamBoard.Team> iter = leagueBoard.getTeamList().iterator();
            while (iter.hasNext()){
                TeamBoard.Team team = iter.next();
                if (team.getId() == teamId){
                    return team.getTeamName();
                }
            }
        }
        return null;
    }

    public boolean isLeagueActive(){
        if (leagueBoard != null){
            return leagueBoard.isLeagueActive();
        }
        return false;
    }

    public boolean toShowLeague(){
        if (leagueBoard != null){
            if (isLeagueActive()){
                return true;
            }else {
                long leagueStartDateEpoch = leagueBoard.getLeagueStartDateEpoch();
                long leagueDurationInSecs = ((long)leagueBoard.getDurationInDays())*86400;
                long oneWeekBufferInSecs = 604800;
                return (DateUtil.getServerTimeInMillis() / 1000) < leagueStartDateEpoch + leagueDurationInSecs + oneWeekBufferInSecs;
            }
        }
        return false;
    }

    public void clearLeagueData(){
        this.leagueBoard = null;
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_LEAGUEBOARD_CACHED_DATA);
        this.myTeamLeaderBoard = null;
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_MY_TEAM_LEADERBOARD_CACHED_DATA);
    }

    public void setLeagueTeamId(int teamId){
        // LeagueTeamId is set only when it is changed
        leagueTeamId = teamId;
        SharedPrefsManager.getInstance().setInt(Constants.PREF_LEAGUE_TEAM_ID, teamId);
        // LeagueTeamId has changed, clear existing League data and immediately start the sync process
        clearLeagueData();
        Thread syncThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SyncService.syncLeaderBoardData();
            }
        });
        syncThread.start();
    }

    public void setLeagueBoardData(TeamBoard leagueBoard){
        this.leagueBoard = leagueBoard;
        SharedPrefsManager.getInstance().setObject(Constants.PREF_LEAGUEBOARD_CACHED_DATA, leagueBoard);
    }

    public void setGlobalLeaderBoardData(LeaderBoardList globalLeaderBoard){
        this.globalLeaderBoard = globalLeaderBoard;
        SharedPrefsManager.getInstance().setObject(Constants.PREF_GLOBAL_LEADERBOARD_CACHED_DATA, globalLeaderBoard);
    }

    public void setMyTeamLeaderBoardData(TeamLeaderBoard board){
        LeaderBoardDataStore.this.myTeamLeaderBoard = board;
        SharedPrefsManager.getInstance().setObject(Constants.PREF_MY_TEAM_LEADERBOARD_CACHED_DATA, board);
    }

    public void updateLeagueBoardData() {
        NetworkDataProvider.doGetCallAsync(Urls.getTeamBoardUrl(), new NetworkAsyncCallback<TeamBoard>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "Couldn't fetch LeagueBoard data: " + ne.getMessage());
                ne.printStackTrace();
                EventBus.getDefault().post(new LeagueBoardDataUpdated(false));
            }

            @Override
            public void onNetworkSuccess(TeamBoard board) {
                Logger.d(TAG, "Successfully fetched LeagueBoardData");
                setLeagueBoardData(board);
                EventBus.getDefault().post(new LeagueBoardDataUpdated(true));
            }
        });
    }

    public void updateGlobalLeaderBoardData() {
        NetworkDataProvider.doGetCallAsync(Urls.getLeaderboardUrl(), new NetworkAsyncCallback<LeaderBoardList>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "Couldn't fetch GlobalLeaderBoard data: " + ne.getMessage());
                ne.printStackTrace();
                EventBus.getDefault().post(new GlobalLeaderBoardDataUpdated(false));
            }

            @Override
            public void onNetworkSuccess(LeaderBoardList list) {
                Logger.d(TAG, "Successfully fetched LeagueBoardData");
                setGlobalLeaderBoardData(list);
                EventBus.getDefault().post(new GlobalLeaderBoardDataUpdated(true));
            }
        });
    }

    public void updateTeamLeaderBoardData(final int teamId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("team_id", String.valueOf(teamId));
        NetworkDataProvider.doGetCallAsync(Urls.getTeamLeaderBoardUrl(), queryParams, null,
                new NetworkAsyncCallback<TeamLeaderBoard>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "Couldn't fetch TeamLeaderBoardData for teamId: "+teamId+", because: "
                        + ne.getMessage());
                ne.printStackTrace();
                EventBus.getDefault().post(new TeamLeaderBoardDataFetched(teamId, false, null));
            }

            @Override
            public void onNetworkSuccess(TeamLeaderBoard board) {
                Logger.d(TAG, "Successfully fetched TeamLeaderBoardData for teamId: " + teamId);
                if (teamId == getMyTeamId()){
                    // It is myTeamLeaderBoard, will cache it for future use
                    setMyTeamLeaderBoardData(board);
                }
                EventBus.getDefault().post(new TeamLeaderBoardDataFetched(teamId, true, board));
            }
        });
    }
}
