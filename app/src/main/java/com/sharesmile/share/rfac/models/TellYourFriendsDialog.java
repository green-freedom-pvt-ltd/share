package com.sharesmile.share.rfac.models;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;

import base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 6/27/17.
 */

public class TellYourFriendsDialog extends BaseDialog {

    private static final String TAG = "TellYourFriendsDialog";

    @BindView(R.id.btn_tell_friends)
    View rateUsButton;

    public TellYourFriendsDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tell_your_friends);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @OnClick(R.id.btn_tell_friends)
    public void onTellFriendsClick(){
        Logger.d(TAG, "onTellFriendsClick");
        if (listener != null){
            listener.onPrimaryClick(this);
        }
    }

}
