package com.sharesmile.share.helpcenter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.core.Logger;

import base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 6/26/17.
 */

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
            UserDetails userDetails = MainApplication.getInstance().getUserDetails();
            userDetails.setEmail(inputMessage);
            MainApplication.getInstance().setUserDetails(userDetails);
            SyncHelper.oneTimeUploadUserData();
            if (listener != null){
                listener.onPrimaryClick(this);
            }
        }
    }

}
