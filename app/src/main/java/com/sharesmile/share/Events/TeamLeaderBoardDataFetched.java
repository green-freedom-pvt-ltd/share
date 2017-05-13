package com.sharesmile.share.Events;

import Models.TeamLeaderBoard;

/**
 * Created by ankitmaheshwari on 5/12/17.
 */

public class TeamLeaderBoardDataFetched {

    private boolean success;
    private int teamId;
    private TeamLeaderBoard teamLeaderBoard;

    public TeamLeaderBoardDataFetched(int teamId, boolean success, TeamLeaderBoard teamLeaderBoard){
        this.success = success;
        this.teamId = teamId;
        this.teamLeaderBoard = teamLeaderBoard;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getTeamId() {
        return teamId;
    }

    public TeamLeaderBoard getTeamLeaderBoard() {
        return teamLeaderBoard;
    }
}