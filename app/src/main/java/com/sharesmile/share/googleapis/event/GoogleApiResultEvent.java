package com.sharesmile.share.googleapis.event;

import android.content.Intent;

/**
 * Created by ankitmaheshwari on 12/2/17.
 */

public class GoogleApiResultEvent {

    int resultCode;
    int requestCode;
    Intent data;

    public GoogleApiResultEvent(int resultCode, int requestCode) {
        this.resultCode = resultCode;
        this.requestCode = requestCode;
    }

    public GoogleApiResultEvent(int resultCode, int requestCode, Intent data) {
        this.resultCode = resultCode;
        this.requestCode = requestCode;
        this.data = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public Intent getData() {
        return data;
    }
}
