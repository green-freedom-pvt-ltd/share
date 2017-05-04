package com.sharesmile.share.rfac.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.rfac.adapters.HistoryAdapter;
import com.sharesmile.share.rfac.models.Run;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class ProfileHistoryFragment extends BaseFragment implements HistoryAdapter.AdapterInterface {
    RecyclerView mRecyclerView;
    ProgressBar mProgress;
    private List<Workout> mWorkoutList;

    HistoryAdapter mHistoryAdapter;

    private static String TAG = "ProfileHistoryFragment";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_history, null);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.lv_profile_history);
        mProgress = (ProgressBar) v.findViewById(R.id.progress_bar);
        init();
        EventBus.getDefault().register(this);
        return v;
    }

    private void init() {
        Logger.d(TAG, "init");
        mHistoryAdapter = new HistoryAdapter(this);
        mRecyclerView.setAdapter(mHistoryAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchRunDataFromDb();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DBEvent.RunDataUpdated runDataUpdated) {
        fetchRunDataFromDb();
    }

    public void fetchRunDataFromDb() {
        Logger.d(TAG, "fetchRunDataFromDb");
        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        mWorkoutList = mWorkoutDao.queryBuilder().orderDesc(WorkoutDao.Properties.Date).list();
        if (mWorkoutList == null || mWorkoutList.isEmpty()){
            SyncHelper.pullEntireWorkoutHistory();
            showProgressDialog();
        }else {
            Logger.d(TAG, "fetchRunDataFromDb, setting rundata in historyAdapter");
            mHistoryAdapter.setData(mWorkoutList);
            hideProgressDialog();
        }
    }

    private void showProgressDialog() {
        mProgress.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void hideProgressDialog() {
        mProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }


    @Override
    public void showInvalidRunDialog(final Run invalidRun) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.invalid_run_title))
                .setMessage(getString(R.string.invalid_run_message))
                .setPositiveButton(getString(R.string.feedback), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getFragmentController().performOperation(IFragmentController.SHOW_FEEDBACK_FRAGMENT, invalidRun);
                    }
                }).setNegativeButton(getString(R.string.ok), null).show();
    }
}




