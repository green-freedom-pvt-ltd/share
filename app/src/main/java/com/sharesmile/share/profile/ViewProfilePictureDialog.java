package com.sharesmile.share.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.sharesmile.share.R;
import com.sharesmile.share.refer_program.model.ReferrerDetails;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewProfilePictureDialog extends Dialog {


    @BindView(R.id.profile_pic)
    ImageView profilePic;

    public ViewProfilePictureDialog(@NonNull Context context, int userType) {
        super(context);

    }

    public ViewProfilePictureDialog(@NonNull Context context, int userType, ReferrerDetails referrerDetails) {
        super(context);
    }

    protected ViewProfilePictureDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_view_profile_picture);
        ButterKnife.bind(this);
        init();
    }

    private void init() {

    }

}
