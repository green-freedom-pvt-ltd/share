package com.sharesmile.share.rfac;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.R;
import com.sharesmile.share.utils.Logger;

import base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 9/27/17.
 */

public class RateAndShareDialog extends BaseDialog {

    private static final String TAG = "RateAndShareDialog";

    @BindView(R.id.btn_rate_us_5)
    View rateUsButton;

    @BindView(R.id.btn_share_the_app)
    View shareAppButton;

    public RateAndShareDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rate_and_share);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @OnClick(R.id.btn_rate_us_5)
    public void onRateUsClick(){
        Logger.d(TAG, "onTellFriendsClick");
        if (listener != null){
            listener.onPrimaryClick(this);
        }
    }

    @OnClick(R.id.btn_share_the_app)
    public void onShareAppClick(){
        Logger.d(TAG, "onTellFriendsClick");
        if (listener != null){
            listener.onSecondaryClick(this);
        }
    }

}
