package com.sharesmile.share.refer_program;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import java.io.IOException;
import java.io.InputStream;

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
    int userType = 0;//1=new & 2=old

    @BindView(R.id.something_is_cooking_desc)
    TextView somethingIsCookingDesc;
    @BindView(R.id.share_layout)
    LinearLayout shareLayout;

    public SomethingIsCookingDialog(@NonNull Context context, int userType) {
        super(context);
        this.userType = userType;
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
            profilePic1Url = Urls.getImpactProfileS3BucketUrl() + MainApplication.getInstance().getUserDetails().getProfilePicture();
        } else {
            profilePic1Url = MainApplication.getInstance().getUserDetails().getSocialThumb();
        }
        ShareImageLoader.getInstance().loadImage(profilePic1Url, profilePic1,
                ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
        ShareImageLoader.getInstance().loadImage(MainApplication.getInstance().getUserDetails().getReferrerProfilePic(), profilePic2,
                ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));

        if (userType == Constants.USER_NEW)//new user
        {
            somethingIsCookingDesc.setText
                    (String.format(getContext().getResources().getString(R.string.something_is_cooking_description_new_user)
                            , MainApplication.getInstance().getUserDetails().getReferrerDetails().getReferalName()));
        } else //old user
        {
            somethingIsCookingDesc.setText
                    (String.format(getContext().getResources().getString(R.string.something_is_cooking_description_old_user)
                            , MainApplication.getInstance().getUserDetails().getReferrerDetails().getReferalName()));
        }
    }

    @OnClick({R.id.close, R.id.btn_share_more_meals, R.id.btn_tell_your_friends})
    public void onCLick(View view) {
        switch (view.getId()) {
            case R.id.close:
                dismiss();
                break;
            case R.id.btn_share_more_meals:
                AssetManager assetManager = getContext().getAssets();
                InputStream istr;
                Bitmap bitmap = null;
                try {
                    istr = assetManager.open("images/share_image_2.jpg");
                    bitmap = BitmapFactory.decodeStream(istr);
                } catch (IOException e) {
                    // handle exception
                }
                Utils.share(getContext(), Utils.getLocalBitmapUri(bitmap, getContext()),
                        String.format(getContext().getString(R.string.smc_share_more_meals),
                                MainApplication.getInstance().getUserDetails().getMyReferCode()));
                break;
            case R.id.btn_tell_your_friends:
                shareLayout.setBackgroundResource(R.drawable.something_is_cooking_bg);
                Bitmap toShare = Utils.getBitmapFromLiveView(shareLayout);
                shareLayout.setBackgroundResource(0);
                Utils.share(getContext(), Utils.getLocalBitmapUri(toShare, getContext()),
                        String.format(getContext().getString(R.string.smc_tell_your_friends),
                                MainApplication.getInstance().getUserDetails().getReferrerDetails().getReferalName()
                                , MainApplication.getInstance().getUserDetails().getMyReferCode()));
                break;
        }
    }
}
