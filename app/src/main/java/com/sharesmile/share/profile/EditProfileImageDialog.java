package com.sharesmile.share.profile;

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

public class EditProfileImageDialog extends BaseDialog {

    private static final String TAG = "EditProfileImageDialog";

    public EditProfileImageDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_profile_picture);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @OnClick(R.id.take_photo)
    public void onTakePhoto(){
        if (listener != null){
            listener.onPrimaryClick(this);
        }
    }

    @OnClick(R.id.choose_existing_photo)
    public void onChooseExistingPhoto(){
        if (listener != null){
            listener.onSecondaryClick(this);
        }
    }

}
