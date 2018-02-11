package com.sharesmile.share.impactleague.event;

/**
 * Created by ankitmaheshwari on 9/21/17.
 */

public class ExitLeague {

    public ExitLeague(boolean success) {
        this.success = success;
    }

    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
