package com.sharesmile.share.rfac.models;

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
 * Created by ankitmaheshwari on 6/27/17.
 */

public class RateUsDialog extends BaseDialog {

    private static final String TAG = "RateUsDialog";

    @BindView(R.id.btn_rate_us)
    View rateUsButton;

    public RateUsDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rate_us);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @OnClick(R.id.btn_rate_us)
    public void onRateUsClick(){
        Logger.d(TAG, "onRateUsClick");
        if (listener != null){
            listener.onPrimaryClick(this);
        }
    }

}
