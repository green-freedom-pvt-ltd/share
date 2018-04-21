package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;
import com.sharesmile.share.profile.streak.model.Goal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentAskReminder extends BaseFragment implements CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "FragmentAskReminder";
    CommonActions commonActions;
    @BindView(R.id.yes)
    RadioButton yes;
    @BindView(R.id.no)
    RadioButton no;

    boolean isChecked = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_ask_reminder, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity) getActivity());
        commonActions.setExplainText(getContext().getResources().getString(R.string.set_reminder_for_daily_jog_walk), getContext().getResources().getString(R.string.reminder_explain_txt));
        commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
        commonActions.setContinueTextColor(R.color.white_10);
        yes.setOnCheckedChangeListener(this);
        no.setOnCheckedChangeListener(this);

    }

    private void setChecked(CompoundButton compoundButton) {
        yes.setChecked(false);
        no.setChecked(false);
        compoundButton.setChecked(true);
        isChecked = false;
        if(yes.isChecked())
        {
            commonActions.setBackAndContinue(TAG,getResources().getString(R.string.set_reminder));
            commonActions.setContinueTextColor(R.color.white);
        }else if(no.isChecked())
        {
            commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
            commonActions.setContinueTextColor(R.color.white);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(!isChecked) {
            isChecked = true;
            setChecked(compoundButton);
        }
    }
}
