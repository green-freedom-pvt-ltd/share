package com.sharesmile.share.profile.editprofiledialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sharesmile.share.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ParentDialog extends Dialog {

    @BindView(R.id.dialog_title)
    TextView dialogTitle;

    @BindView(R.id.main_frame_layout)
    FrameLayout mainFrameLayout;

    @BindView(R.id.back_tv)
    TextView backTv;

    @BindView(R.id.continue_tv)
    TextView continueTv;

    public ParentDialog(@NonNull Context context) {
        super(context,R.style.dialog_fullscreen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_profile);
        getWindow().setLayout(MATCH_PARENT,MATCH_PARENT);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.back_tv)
    void onBackClick()
    {
        dismiss();
    }

    public void setDialogTitle(String msg)
    {
        dialogTitle.setText(msg);
    }

}
