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
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @OnClick(R.id.share_code)
    public void onShareClick() {
        Utils.share(getOwnerActivity(),
                String.format(getContext().getString(R.string.smc_share_more_meals),
                        MainApplication.getInstance().getUserDetails().getMyReferCode()));
    }

    @OnClick(R.id.close)
    public void onCloseClick() {
        dismiss();
    }

}
