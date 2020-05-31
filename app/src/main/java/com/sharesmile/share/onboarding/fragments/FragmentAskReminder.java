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
import butterknife.OnClick;

public class FragmentAskReminder extends BaseFragment implements View.OnClickListener {
    public static final String TAG = "FragmentAskReminder";
    CommonActions commonActions;
    @BindView(R.id.yes)
    TextView yes;
    @BindView(R.id.no)
    TextView no;

    int actionSelected = -1;
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
        setChecked();
    }

    private void setChecked() {
        yes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_unselected,0,0,0);
        no.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_unselected,0,0,0);

        if(actionSelected == 1)
        {
            commonActions.setBackAndContinue(TAG,getResources().getString(R.string.set_reminder));
            commonActions.setContinueTextColor(R.color.white);
            yes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected,0,0,0);
            ((OnBoardingActivity)getActivity()).setProgressLevel(6.0f);
        }else if(actionSelected == 0)
        {
            commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
            commonActions.setContinueTextColor(R.color.white);
            no.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected,0,0,0);
            ((OnBoardingActivity)getActivity()).setProgressLevel(7.0f);
        }
    }
    @OnClick({R.id.yes_layout,R.id.no_layout})
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.yes_layout :
                actionSelected = 1;
                break;
            case R.id.no_layout :
                actionSelected = 0;
                break;
        }
        setChecked();
    }
}
