package com.sharesmile.share.core;

/**
 * Created by ankitm on 22/04/16.
 */
public interface PermissionCallback {

    int OTP_VERIFICATION_READ_SMS = 101;

    void onPermissionRequestResult(int requestCode, boolean result);

}
