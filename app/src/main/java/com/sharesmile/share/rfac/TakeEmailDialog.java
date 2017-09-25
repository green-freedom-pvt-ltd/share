package com.sharesmile.share.rfac;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.utils.Logger;

import base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 6/26/17.
 */

@Deprecated
public class TakeEmailDialog extends BaseDialog {

    private static final String TAG = "TakeEmailDialog";

    @BindView(R.id.et_take_email)
    EditText takeEmailEditText;

    @BindView(R.id.btn_email_submit)
    View submitButton;

    public TakeEmailDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_enter_email);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ButterKnife.bind(this);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @OnClick(R.id.btn_email_submit)
    public void onSubmitClick(){
        Logger.d(TAG, "onSubmitClick");
        String inputMessage = takeEmailEditText.getText().toString();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inputMessage).matches()){
            MainApplication.showToast(R.string.please_enter_valid_email_address);
        }else {
            if (listener != null){
                listener.onPrimaryClick(this);
            }
        }
    }

}
