package com.sharesmile.share.Events;

/**
 * Created by ankitmaheshwari on 5/12/17.
 */

public class GlobalLeaderBoardDataUpdated {

    private boolean success;

    public GlobalLeaderBoardDataUpdated(boolean success){
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

}
