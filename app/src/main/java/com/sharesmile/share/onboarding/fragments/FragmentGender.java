package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentGender extends BaseFragment {
    public static final String TAG = "FragmentGender";
    CommonActions commonActions;
    @BindView(R.id.female_cv)
    CardView femaleCardView;
    @BindView(R.id.female_iv)
    ImageView femaleImageView;
    @BindView(R.id.female_tv)
    TextView femaleTextView;

    @BindView(R.id.male_cv)
    CardView maleCardView;
    @BindView(R.id.male_iv)
    ImageView maleImageView;
    @BindView(R.id.male_tv)
    TextView maleTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_gender, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity)getActivity());
        commonActions.setExplainText(getContext().getResources().getString(R.string.choose_your_gender),"");
        commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
        selectBG(-1);
        setData();

    }

    private void setData() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        String gender = userDetails.getGenderUser();
        if(gender!=null && gender.length()>0)
        {
            if(gender.toLowerCase().startsWith("m"))
            {
                maleCardView.performClick();
            }else if(gender.toLowerCase().startsWith("f"))
            {
                femaleCardView.performClick();
            }
        }
    }

    @OnClick({R.id.female_cv,R.id.male_cv})
    void selectGender(View view)
    {
        selectBG(view.getId());
        commonActions.setContinueTextColor(R.color.white);
    }

    private void selectBG(int id)
    {
        femaleCardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        femaleImageView.setImageResource(R.drawable.female);
        femaleTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        maleCardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        maleImageView.setImageResource(R.drawable.male);
        maleTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        switch (id)
        {
            case R.id.female_cv :
                femaleCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                femaleTextView.setTextColor(getResources().getColor(R.color.white));
                femaleImageView.setImageResource(R.drawable.female_white);
                userDetails.setGenderUser("f");
                break;
            case R.id.male_cv :
                maleCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                maleTextView.setTextColor(getResources().getColor(R.color.white));
                maleImageView.setImageResource(R.drawable.male_white);
                userDetails.setGenderUser("m");
                break;
        }
        MainApplication.getInstance().setUserDetails(userDetails);
    }
}
