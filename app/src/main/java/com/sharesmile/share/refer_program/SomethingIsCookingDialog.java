package com.sharesmile.share.refer_program;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sharesmile.share.R;

public class SomethingIsCookingDialog extends Dialog {

    public SomethingIsCookingDialog(@NonNull Context context) {
        super(context);
    }

    public SomethingIsCookingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SomethingIsCookingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_something_is_cooking);
    }
}
