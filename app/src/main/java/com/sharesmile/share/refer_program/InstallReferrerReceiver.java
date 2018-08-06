package com.sharesmile.share.refer_program;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReferrerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        String referrerId = intent.getStringExtra("referrer");

        if (referrerId == null) {
            return;
        }
    }
}