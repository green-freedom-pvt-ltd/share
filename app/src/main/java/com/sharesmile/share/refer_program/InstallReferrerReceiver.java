package com.sharesmile.share.refer_program;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.SharedPrefsManager;

public class InstallReferrerReceiver extends CampaignTrackingReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        String referrerId = intent.getStringExtra("referrer");
        SharedPrefsManager.getInstance().setString(Constants.PREF_REFERRAL_CODE, referrerId);
        if (referrerId == null) {
            return;
        }
    }
}