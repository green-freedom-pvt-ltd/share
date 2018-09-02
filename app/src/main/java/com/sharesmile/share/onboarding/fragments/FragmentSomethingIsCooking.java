package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.sharesmile.share.R;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;
import com.sharesmile.share.views.CircularImageView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentSomethingIsCooking extends BaseFragment {

    public static final String TAG = "FragmentSomethingIsCooking";
    CommonActions commonActions;

    @BindView(R.id.close)
    TextView close;
    @BindView(R.id.continue_tv)
    TextView continueTv;
    @BindView(R.id.smc_buttons_layout)
    LinearLayout smcButtonsLayout;

    @BindView(R.id.profile_pic_1)
    CircularImageView profilePic1;
    @BindView(R.id.profile_pic_2)
    CircularImageView profilePic2;
    @BindView(R.id.something_is_cooking_desc)
    TextView somethingIsCookingDesc;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_something_is_cooking, null);
        ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity) getActivity());
        commonActions.setExplainText("", "");
        commonActions.setBackAndContinue(TAG, "");
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        String imageUrl = "";
        if (userDetails.getProfilePicture() != null &&
                userDetails.getProfilePicture().length() > 0) {
            imageUrl = Urls.getImpactProfileS3BucketUrl() + userDetails.getProfilePicture();
        } else {
            imageUrl = userDetails.getSocialThumb();
        }
        ShareImageLoader.getInstance().loadImage(imageUrl, profilePic1,
                ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));

        ShareImageLoader.getInstance().loadImage(MainApplication.getInstance().getUserDetails().getReferrerProfilePicture(), profilePic2,
                ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
        somethingIsCookingDesc.setText(getResources().getString(R.string.such_a_noble_beginning));
        EventBus.getDefault().post(new UpdateEvent.OnCodeVerified(new JsonObject()));

        smcButtonsLayout.setVisibility(View.GONE);
        continueTv.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.continue_tv, R.id.close})
    public void onClick(View v) {
        getFragmentController().replaceFragment(new FragmentGender(), true);
    }

}
