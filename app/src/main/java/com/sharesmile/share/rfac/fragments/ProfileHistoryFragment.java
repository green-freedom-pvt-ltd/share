package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.User;
import com.sharesmile.share.UserDao;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.rfac.adapters.HistoryAdapter;

import java.util.List;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class ProfileHistoryFragment extends Fragment {
    RecyclerView mRecyclerView;
    private List<Workout> mWorkoutList;

    HistoryAdapter mHistoryAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        mWorkoutList = mWorkoutDao.queryBuilder().orderDesc(WorkoutDao.Properties.Date).list();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_history, null);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.lv_profile_history);
        init();
        return v;
    }

    private void init() {

        mHistoryAdapter = new HistoryAdapter();
        mHistoryAdapter.setData(mWorkoutList);
        mRecyclerView.setAdapter(mHistoryAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}




