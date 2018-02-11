package com.sharesmile.share.impactleague.event;

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
