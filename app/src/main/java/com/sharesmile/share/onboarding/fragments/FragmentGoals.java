package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;
import com.sharesmile.share.profile.streak.model.Goal;
import com.sharesmile.share.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentGoals extends BaseFragment {
    public static final String TAG = "FragmentGoals";
    CommonActions commonActions;
    ArrayList<Goal> goals;
    @BindView(R.id.layout_goals)
    LinearLayout layoutGoals;

    ArrayList<RadioButton> radioButtons;
    boolean checkRunning = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_pick_a_goal, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity)getActivity());
        commonActions.setExplainText(getContext().getResources().getString(R.string.pick_a_daily_goal),getContext().getResources().getString(R.string.reminder_explain_txt));
        commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
        setGoals();
    }

    private void setGoals() {
        goals = new Gson().fromJson(MainApplication.getInstance().getGoalDetails(), new TypeToken<List<Goal>>(){}.getType());
        layoutGoals.removeAllViews();
        radioButtons = new ArrayList<>();
        for(int i=0;i<goals.size();i++)
        {
            Goal goal = goals.get(i);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.row_onboarding,null,false);
            RadioButton goalName = view.findViewById(R.id.goal_name);
            TextView goalStreakDistance = view.findViewById(R.id.goal_streak_distance);
            goalName.setText(goal.getName());
            goalStreakDistance.setText(goal.getValue()+" km per day");
            goalName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(!checkRunning)
                    setChecked(compoundButton);
                }
            });
            radioButtons.add(goalName);
            layoutGoals.addView(view);
        }
        int goalID = MainApplication.getInstance().getUserDetails().getStreakGoalID();
        if(goalID!=0)
        {
            for(int i=0;i<goals.size();i++)
            {
                if(goals.get(i).getId() == goalID)
                {
                    radioButtons.get(i).setChecked(true);
                    break;
                }
            }
        }else
        {
            radioButtons.get(0).setChecked(true);
        }
    }

    private void setChecked(CompoundButton compoundButton) {
        checkRunning = true;
        for (int i = 0; i < radioButtons.size(); i++) {
            radioButtons.get(i).setChecked(false);
        }
        compoundButton.setChecked(true);
        checkRunning = false;

    }

}
