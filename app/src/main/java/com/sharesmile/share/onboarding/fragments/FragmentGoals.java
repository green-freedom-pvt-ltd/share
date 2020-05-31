package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentGoals extends BaseFragment {
    public static final String TAG = "FragmentGoals";
    CommonActions commonActions;
    ArrayList<Goal> goals;
    @BindView(R.id.layout_goals)
    LinearLayout layoutGoals;

    ArrayList<View> radioButtons;
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
        commonActions.setExplainText(getContext().getResources().getString(R.string.pick_a_daily_goal),getContext().getResources().getString(R.string.maintain_daily_streak));
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
            View view = LayoutInflater.from(getContext()).inflate(R.layout.rv_onboarding_row_item,null,false);
            TextView goalName = view.findViewById(R.id.goal_name);
            TextView goalStreakDistance = view.findViewById(R.id.goal_streak_distance);
            goalName.setText(goal.getName());
            goalName.setTag(goal);
            goalStreakDistance.setText(goal.getValue()+"km per day");
            RelativeLayout goalRow = view.findViewById(R.id.goal_row);
            goalRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setChecked(view);
                }
            });

            radioButtons.add(view);
            layoutGoals.addView(view);
        }
        int goalID = MainApplication.getInstance().getUserDetails().getStreakGoalID();
        if(goalID!=0)
        {
            for(int i=0;i<goals.size();i++)
            {
                if(goals.get(i).getId() == goalID)
                {
                    ((TextView)radioButtons.get(i).findViewById(R.id.goal_name)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected,0,0,0);
                    break;
                }
            }
        }else
        {
            setChecked(radioButtons.get(0));
        }
    }

    private void setChecked(View view) {
        checkRunning = true;
        for (int i = 0; i < radioButtons.size(); i++) {
            ((TextView)radioButtons.get(i).findViewById(R.id.goal_name)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_unselected,0,0,0);
        }
        ((TextView)view.findViewById(R.id.goal_name)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected,0,0,0);
        checkRunning = false;
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        TextView goalName = view.findViewById(R.id.goal_name);
        userDetails.setStreakGoalID(((Goal)goalName.getTag()).getId());
        userDetails.setStreakGoalDistance(((Goal)goalName.getTag()).getValue());
        if (userDetails.getStreakCount() == 0) {
            userDetails.setStreakRunProgress(0);
            userDetails.setStreakCount(0);
            userDetails.setStreakMaxCount(0);
            userDetails.setStreakCurrentDate(Utils.getCurrentDateDDMMYYYY());
            userDetails.setStreakAdded(false);
        }
        MainApplication.getInstance().setUserDetails(userDetails);
    }

}
