package com.sharesmile.share.leaderboard.global;

/**
 * Created by ankitmaheshwari on 5/12/17.
 */

public class GlobalLeaderBoardDataUpdated {

    private boolean success;
    private String interval;

    public GlobalLeaderBoardDataUpdated(boolean success, String interval){
        this.success = success;
        this.interval = interval;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getInterval() {
        return interval;
    }
}
