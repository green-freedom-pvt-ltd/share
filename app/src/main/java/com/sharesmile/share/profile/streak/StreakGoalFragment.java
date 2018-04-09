package com.sharesmile.share.profile.streak;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.profile.streak.model.Goal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class StreakGoalFragment extends BaseFragment {

    private static final String TAG = "StreakGoalFragment";
    @BindView(R.id.lv_streak_goal)
    RecyclerView streakGoalRecycler;

    LinearLayoutManager linearLayoutManager;
    
    StreakGoalAdapter streakGoalAdapter;
    ArrayList<Goal> goals;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_streak_goal_select, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        initUi();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pick_a_goal, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.pick_a_goal));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_save:
                int position = streakGoalAdapter.getPosition();
                UserDetails userDetails = MainApplication.getInstance().getUserDetails();
                userDetails.setStreakGoalID(goals.get(position).getId());
                userDetails.setStreakGoalDistance(goals.get(position).getValue());
                userDetails.addStreakCount();
                MainApplication.getInstance().setUserDetails(userDetails);
                getFragmentController().goBack();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUi() {
        goals = new Gson().fromJson(MainApplication.getInstance().getGoalDetails(), new TypeToken<List<Goal>>(){}.getType());
        linearLayoutManager = new LinearLayoutManager(getContext());
        streakGoalRecycler.setLayoutManager(linearLayoutManager);
        streakGoalAdapter = new StreakGoalAdapter(goals,getContext());
        streakGoalRecycler.setAdapter(streakGoalAdapter);
    }



}
