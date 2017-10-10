package com.sharesmile.share;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.Events.ExitLeague;
import com.sharesmile.share.Events.GlobalLeaderBoardDataUpdated;
import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.Events.TeamLeaderBoardDataFetched;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.ExpoBackoffTask;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.LeaderBoardData;
import com.sharesmile.share.rfac.models.LeaderBoardList;
import com.sharesmile.share.rfac.models.UserDetails;
import com.sharesmile.share.utils.BasicNameValuePair;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.NameValuePair;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.LeagueBoard;
import Models.LeagueTeam;
import Models.TeamLeaderBoard;

/**
 * Created by ankitmaheshwari on 5/12/17.
 */

public class LeaderBoardDataStore {

    private static final String TAG = "LeaderBoardDataStore";

    private static LeaderBoardDataStore uniqueInstance;

    private LeagueBoard leagueBoard;
    private Map<String, LeaderBoardList> globalLeaderBoardMap;
    private TeamLeaderBoard myTeamLeaderBoard;

    private Context context;

    public static final String ALL_TIME_INTERVAL = "all_time";
    public static final String LAST_WEEK_INTERVAL = "last_7";
    public static final String LAST_MONTH_INTERVAL = "last_30";

    public static final List<String> ALL_INTERVALS = new ArrayList<String>(){{
        add(LAST_WEEK_INTERVAL);
        add(LAST_MONTH_INTERVAL);
        add(ALL_TIME_INTERVAL);
    }};

    private LeaderBoardDataStore(Context appContext){
        this.context = appContext;
        this.globalLeaderBoardMap = SharedPrefsManager.getInstance().getCollection(
                Constants.PREF_GLOBAL_LEADERBOARD_CACHED_DATA,
                new TypeToken<Map<String, LeaderBoardList>>(){}.getType());

        if (globalLeaderBoardMap == null){
            globalLeaderBoardMap = new HashMap<>();
        }

        this.leagueBoard = SharedPrefsManager.getInstance()
                .getObject(Constants.PREF_LEAGUEBOARD_CACHED_DATA, LeagueBoard.class);
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

    public LeagueBoard getLeagueBoard() {
        return leagueBoard;
    }

    public LeaderBoardList getGlobalLeaderBoard(String interval){
        return globalLeaderBoardMap.get(interval);
    }

    public TeamLeaderBoard getMyTeamLeaderBoard() {
        if (myTeamLeaderBoard == null){
            myTeamLeaderBoard = SharedPrefsManager.getInstance()
                    .getObject(Constants.PREF_MY_TEAM_LEADERBOARD_CACHED_DATA, TeamLeaderBoard.class);
        }
        return myTeamLeaderBoard;
    }

    public int getMyTeamId() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        if (userDetails != null){
//            Logger.d(TAG, "Returning myTeamId: " + userDetails.getTeamId());
            return userDetails.getTeamId();
        }
        return 0;
    }

    public String getLeagueName(){
        if (leagueBoard != null ){
            return leagueBoard.getLeagueName();
        }
        return null;
    }

    public String getMyTeamName(){
        if (myTeamLeaderBoard != null){
            return myTeamLeaderBoard.getTeamName();
        }
        return null;
    }

    public String getTeamName(int teamId){
        if (leagueBoard != null){
            Iterator<LeagueBoard.Team> iter = leagueBoard.getTeamList().iterator();
            while (iter.hasNext()){
                LeagueBoard.Team team = iter.next();
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
            Logger.d(TAG, "LeagueBoard is not null");
            if (isLeagueActive()){
                Logger.d(TAG, "League is active");
                return true;
            } else {
                return isLeagueInWithdrawlPeriod();
            }
        }else {
            Logger.d(TAG, "LeagueBoard is null");
        }
        return false;
    }

    public boolean toSyncLeaugeData(){
        if (getMyTeamId() > 0){
            if (leagueBoard == null || isLeagueActive()){
                return true;
            } else {
                return isLeagueInWithdrawlPeriod();
            }
        }
        return false;
    }

    private boolean isLeagueInWithdrawlPeriod(){
        long leagueStartDateEpoch = leagueBoard.getLeagueStartDateEpoch();
        long leagueDurationInSecs = ((long)leagueBoard.getDurationInDays())*86400;
        Logger.d(TAG, "League not active, leagueStartDateEpoch = " + leagueStartDateEpoch
                + ", leagueDurationInSecs = " + leagueDurationInSecs);
        long oneWeekWithdrawlPeriodInSecs = 604800;
        return (DateUtil.getServerTimeInMillis() / 1000) < leagueStartDateEpoch
                + leagueDurationInSecs + oneWeekWithdrawlPeriodInSecs;
    }

    public void clearLeagueData(){
        Logger.d(TAG, "clearLeagueData");
        this.leagueBoard = null;
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_LEAGUEBOARD_CACHED_DATA);
        this.myTeamLeaderBoard = null;
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_MY_TEAM_LEADERBOARD_CACHED_DATA);
    }

    /**
     * Updates and persists new teamId
     * triggers updates due to the change in teamId
     * @param teamId
     */
    public void updateMyTeamId(int teamId){
        Logger.d(TAG, "updateMyTeamId with " + teamId);
        // LeagueTeamId has changed, clear existing League data and immediately start the sync process
        clearLeagueData();
        if (teamId > 0){
            ExpoBackoffTask task = new ExpoBackoffTask() {
                @Override
                public int performtask() {
                    return SyncService.syncLeagueBoardData();
                }
            };
            task.run();
        }
    }

    public void setLeagueBoardData(LeagueBoard leagueBoard){
        this.leagueBoard = leagueBoard;
        SharedPrefsManager.getInstance().setObject(Constants.PREF_LEAGUEBOARD_CACHED_DATA, leagueBoard);
    }

    public void setGlobalLeaderBoard(String interval, LeaderBoardList leaderBoardList){
        List<LeaderBoardData> list = leaderBoardList.getLeaderBoardList();

        Set<Integer> rankSet = new HashSet<>();
        Iterator<LeaderBoardData> iterator = list.iterator();
        while (iterator.hasNext()){
            LeaderBoardData data = iterator.next();
            Integer rank = data.getRank();
            if (rankSet.contains(rank)){
                iterator.remove();
            }else {
                rankSet.add(rank);
            }
        }

        Collections.sort(list, new Comparator<LeaderBoardData>() {
            @Override
            public int compare(LeaderBoardData o1, LeaderBoardData o2) {
                return o1.getRank() - o2.getRank();
            }
        });
        this.globalLeaderBoardMap.put(interval, leaderBoardList);
        SharedPrefsManager.getInstance().setCollection(Constants.PREF_GLOBAL_LEADERBOARD_CACHED_DATA,
                globalLeaderBoardMap);

    }

    public void setMyTeamLeaderBoardData(TeamLeaderBoard board){
        LeaderBoardDataStore.this.myTeamLeaderBoard = board;
        SharedPrefsManager.getInstance().setObject(Constants.PREF_MY_TEAM_LEADERBOARD_CACHED_DATA, board);
    }

    public void updateLeagueBoardData() {
        NetworkDataProvider.doGetCallAsync(Urls.getLeagueBoardUrl(), new NetworkAsyncCallback<LeagueBoard>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "Couldn't fetch LeagueBoard data: " + ne);
                ne.printStackTrace();
                EventBus.getDefault().post(new LeagueBoardDataUpdated(false));
            }

            @Override
            public void onNetworkSuccess(LeagueBoard board) {
                Gson gson = new Gson();
                Logger.d(TAG, "Successfully fetched LeagueBoardData: " + gson.toJson(board));
                setLeagueBoardData(board);
                EventBus.getDefault().post(new LeagueBoardDataUpdated(true));
            }
        });
    }

    public void updateGlobalLeaderBoardData(final String interval) {
        NetworkDataProvider.doGetCallAsync(Urls.getLeaderboardUrl(interval), new NetworkAsyncCallback<LeaderBoardList>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "Couldn't fetch GlobalLeaderBoard data: " + ne);
                ne.printStackTrace();
                EventBus.getDefault().post(new GlobalLeaderBoardDataUpdated(false, interval));
            }

            @Override
            public void onNetworkSuccess(LeaderBoardList list) {
                Logger.d(TAG, "Successfully fetched LeaderBoard data for " + interval);
                setGlobalLeaderBoard(interval, list);
                EventBus.getDefault().post(new GlobalLeaderBoardDataUpdated(true, interval));
            }
        });
    }

    public void updateTeamLeaderBoardData(final int teamId) {
        Logger.d(TAG, "updateTeamLeaderBoardData");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("team_id", String.valueOf(teamId));
        NetworkDataProvider.doGetCallAsync(Urls.getTeamLeaderBoardUrl(), queryParams, null,
                new NetworkAsyncCallback<TeamLeaderBoard>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "Couldn't fetch TeamLeaderBoardData for teamId: "+teamId+", because: "
                        + ne);
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

    public void exitLeague() {
        Logger.d(TAG, "exitLeague");

        List<NameValuePair> data = new ArrayList<>();
        final int userId = MainApplication.getInstance().getUserID();
        final int teamId = getMyTeamId();
        data.add(new BasicNameValuePair("user", String.valueOf(userId)));
        data.add(new BasicNameValuePair("team_code", String.valueOf(teamId)));
        data.add(new BasicNameValuePair("is_logout", "true"));

        NetworkDataProvider.doPutCallAsyncWithFormData(Urls.getLeagueRegistrationUrl(), data,
                new NetworkAsyncCallback<LeagueTeam>() {
                    @Override
                    public void onNetworkFailure(NetworkException ne) {
                        MainApplication.showToast(R.string.network_error_cant_exit);
                        EventBus.getDefault().post(new ExitLeague(false));
                        String message = "Put request for Exit failed for teamId: "+teamId+", and userId: "
                                + userId+", because: " + ne;
                        Logger.e(TAG, message);
                        ne.printStackTrace();
                        Crashlytics.log(message);
                        Crashlytics.logException(ne);
                    }

                    @Override
                    public void onNetworkSuccess(LeagueTeam leagueTeam) {
                        Logger.d(TAG, "Successfully logged out from teamId: " + teamId);
                        // Remove all cached data
                        updateMyTeamId(0);
                        // Notify the UI about it
                        EventBus.getDefault().post(new ExitLeague(true));
                    }
                });
    }
}
