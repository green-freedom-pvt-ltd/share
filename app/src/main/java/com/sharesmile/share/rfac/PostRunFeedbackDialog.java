package com.sharesmile.share.rfac;

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
 * Created by ankitmaheshwari on 6/26/17.
 */

public class PostRunFeedbackDialog extends BaseDialog {

    private static final String TAG = "PostRunFeedbackDialog";

    @BindView(R.id.sad_container)
    View sadContainer;

    @BindView(R.id.happy_container)
    View happyContainer;

    public PostRunFeedbackDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_post_run_feedback);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ButterKnife.bind(this);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @OnClick(R.id.sad_container)
    public void onSadClick(){
        Logger.d(TAG, "onSadClick");
        if (listener != null){
            listener.onSecondaryClick(this);
        }
    }

    @OnClick(R.id.happy_container)
    public void onHappyClick(){
        Logger.d(TAG, "onHappyClick");
        if (listener != null){
            listener.onPrimaryClick(this);
        }
    }

}
