package com.sharesmile.share.Events;

/**
 * Created by ankitmaheshwari on 5/12/17.
 */

public class LeagueBoardDataUpdated implements LeagueDataEvent{

    private boolean success;

    public LeagueBoardDataUpdated(boolean success){
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
