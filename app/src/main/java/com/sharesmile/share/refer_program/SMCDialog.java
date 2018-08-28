package com.sharesmile.share.refer_program;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SMCDialog extends Dialog {

    @BindView(R.id.bacha_plate)
    ImageView bachaPlate;

    @BindView(R.id.share_code)
    TextView shareCode;

    public SMCDialog(@NonNull Context context) {
        super(context);
    }

    public SMCDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SMCDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_smc);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        shareCode.setText(MainApplication.getInstance().getUserDetails().getMyReferCode());
    }

}
