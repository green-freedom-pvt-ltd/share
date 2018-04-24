package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentThankYou extends BaseFragment {

    public static final String TAG = "FragmentThankYou";
    @BindView(R.id.welcome_tv)
    TextView welcome;
    @BindView(R.id.change_maker_tv)
    TextView changeMaker;
    @BindView(R.id.change_maker_tv_2)
    TextView changeMaker2;

    CommonActions commonActions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_welcome, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity) getActivity());
        commonActions.setExplainText("", "");
        commonActions.setBackAndContinue(TAG, getResources().getString(R.string.continue_txt)+">");
        welcome.setText(getResources().getString(R.string.thank_you));
        changeMaker.setText(getResources().getString(R.string.life_as_change_maker2));
        changeMaker2.setText(getResources().getString(R.string.every_step2));
    }
}
