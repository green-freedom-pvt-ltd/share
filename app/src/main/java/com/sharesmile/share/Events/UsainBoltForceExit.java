package com.sharesmile.share.Events;

/**
 * Created by ankitmaheshwari on 4/24/17.
 */

public class UsainBoltForceExit {

    private boolean autoFlagged;

    public UsainBoltForceExit(boolean autoFlagged) {
        this.autoFlagged = autoFlagged;
    }

    public boolean isAutoFlagged() {
        return autoFlagged;
    }
}
