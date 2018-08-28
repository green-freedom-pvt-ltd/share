package com.sharesmile.share.refer_program;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.views.CircularImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SomethingIsCookingDialog extends Dialog {

    @BindView(R.id.smc_buttons_layout)
    LinearLayout smcButtonsLayout;
    @BindView(R.id.continue_tv)
    TextView continueTv;

    @BindView(R.id.btn_share_more_meals)
    TextView btnShareMoreMeals;
    @BindView(R.id.btn_tell_your_friends)
    TextView btnTellYourFriends;
    @BindView(R.id.close)
    TextView close;

    @BindView(R.id.profile_pic_1)
    CircularImageView profilePic1;
    @BindView(R.id.profile_pic_2)
    CircularImageView profilePic2;

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
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        continueTv.setVisibility(View.GONE);
        smcButtonsLayout.setVisibility(View.VISIBLE);
        String profilePic1Url = "";
        if (MainApplication.getInstance().getUserDetails().getProfilePicture() != null &&
                MainApplication.getInstance().getUserDetails().getProfilePicture().length() > 0) {
            profilePic1Url = MainApplication.getInstance().getUserDetails().getProfilePicture();
        } else {
            profilePic1Url = MainApplication.getInstance().getUserDetails().getSocialThumb();
        }
        ShareImageLoader.getInstance().loadImage(profilePic1Url, profilePic1,
                ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
    }

    @OnClick({R.id.close, R.id.btn_share_more_meals, R.id.btn_tell_your_friends})
    public void onCLick(View view) {
        switch (view.getId()) {
            case R.id.close:

                break;
            case R.id.btn_share_more_meals:

                break;
            case R.id.btn_tell_your_friends:

                break;
        }
    }
}
