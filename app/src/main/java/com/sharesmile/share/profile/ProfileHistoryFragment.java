package com.sharesmile.share.profile;

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

import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.base.IFragmentController;
import com.sharesmile.share.helpcenter.PastWorkoutIssueFragment;
import com.sharesmile.share.helpcenter.FeedbackCategory;
import com.sharesmile.share.tracking.Run;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class ProfileHistoryFragment extends BaseFragment implements HistoryAdapter.AdapterInterface {


    View selectIssueContainer;
    RecyclerView mRecyclerView;
    ProgressBar mProgress;
    private List<Workout> mWorkoutList;
    private List<RunHistoryItem> mHistoryItemsList;

    HistoryAdapter mHistoryAdapter;

    private static String TAG = "ProfileHistoryFragment";

    private static final String ARG_IS_RUN_SELECTION = "arg_is_run_selection";

    private boolean isRunSelection;

    public static ProfileHistoryFragment newInstance(boolean isRunSelection) {

        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_RUN_SELECTION, isRunSelection);
        ProfileHistoryFragment fragment = new ProfileHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRunSelection = getArguments().getBoolean(ARG_IS_RUN_SELECTION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_history, null);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.lv_profile_history);
        mProgress = (ProgressBar) v.findViewById(R.id.progress_bar);
        selectIssueContainer = v.findViewById(R.id.container_select_workout);
        init();
        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isRunSelection){
            AnalyticsEvent.create(Event.ON_LOAD_FEEDBACK_PAST_WORKOUTS).buildAndDispatch();
        }else {
            AnalyticsEvent.create(Event.ON_LOAD_WORKOUT_HISTORY).buildAndDispatch();
        }
    }

    private void init() {
        Logger.d(TAG, "init");
        mHistoryAdapter = new HistoryAdapter(this, isRunSelection);
        mRecyclerView.setAdapter(mHistoryAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (isRunSelection){
            selectIssueContainer.setVisibility(View.VISIBLE);
        }else {
            selectIssueContainer.setVisibility(View.GONE);
        }
        fetchRunDataFromDb();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupToolbar();
    }

    protected void setupToolbar(){
        if (isRunSelection){
            setToolbarTitle(getString(R.string.select_workout));
        }else {
            setToolbarTitle(getString(R.string.workout_history_title));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.RunDataUpdated runDataUpdated) {
        fetchRunDataFromDb();
    }

    public void fetchRunDataFromDb() {
        Logger.d(TAG, "fetchRunDataFromDb, total workout count = " + MainApplication.getInstance().getUsersWorkoutCount());
        boolean isWorkoutDataUpToDate =
                SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_WORKOUT_DATA_UP_TO_DATE_IN_DB, false);
        if (isWorkoutDataUpToDate){
            WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
            mWorkoutList = mWorkoutDao.queryBuilder().orderDesc(WorkoutDao.Properties.Date).list();
            if (mWorkoutList == null || mWorkoutList.isEmpty()){
                // No runs to show yet, Do nothing
            }else {
                Logger.d(TAG, "fetchRunDataFromDb, setting rundata in historyAdapter");
                mHistoryItemsList = extractListItemsFromWorkouts();
                mHistoryAdapter.setData(mHistoryItemsList);
                hideProgressDialog();
                // Show WeightInputDialog if weight is not present
                if (MainApplication.getInstance().getBodyWeight() <= 0){
                    weightInputDialog = Utils.showWeightInputDialog(getActivity());
                }
            }
        }else {
            // Need to force refresh Workout Data
            Logger.e(TAG, "Must fetch historical runs before");
            SyncHelper.forceRefreshEntireWorkoutHistory();
        }

    }

    private List<RunHistoryItem> extractListItemsFromWorkouts(){
        TreeMap<Calendar, List<Workout>> treeMap = new TreeMap<>(Collections.reverseOrder());
        for (Workout workout : mWorkoutList){
            Calendar calendarInWorkout = Calendar.getInstance();
            if (workout.getBeginTimeStamp() != null && workout.getBeginTimeStamp() > 0){
                calendarInWorkout.setTimeInMillis(workout.getBeginTimeStamp());
            }else {
                calendarInWorkout.setTimeInMillis(workout.getDate().getTime());
            }

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            int year = calendarInWorkout.get(Calendar.YEAR);
            calendar.set(Calendar.YEAR, year);
            int month = calendarInWorkout.get(Calendar.MONTH);
            calendar.set(Calendar.MONTH, month);
            int dayOfMonth = calendarInWorkout.get(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

//            Logger.d(TAG, "Calendar entry for year = " + year +", month = " + month
//                    + ", dayOfMonth = " + dayOfMonth + " is" + calendar.getTime());

            if (treeMap.containsKey(calendar)){
                treeMap.get(calendar).add(workout);
            }else {
                List<Workout> list = new ArrayList<>();
                list.add(workout);
                treeMap.put(calendar, list);
            }
        }

        List<RunHistoryItem> items = new ArrayList<>();
        for (Calendar calendar : treeMap.keySet()){
            RunHistoryDateHeaderItem dateHeaderItem = new RunHistoryDateHeaderItem();
            dateHeaderItem.setCalendar(calendar);
            items.add(dateHeaderItem);
            float impact = 0;
            float calories = 0;
            for (Workout workout : treeMap.get(calendar)){
                items.add(new RunHistoryDetailsItem(workout));
                impact += workout.getRunAmount();
                calories += (workout.getCalories() != null ? workout.getCalories() : 0);
            }
            dateHeaderItem.setImpactInDay(impact);
            dateHeaderItem.setCaloriesInDay(calories);
        }
        return items;
    }

    private void showProgressDialog() {
        mProgress.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void hideProgressDialog() {
        mProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    AlertDialog weightInputDialog;
    AlertDialog caloriesNotAvailableRationaleDialog;
    AlertDialog invalidRunDialog;

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        if (weightInputDialog != null && weightInputDialog.isShowing()){
            weightInputDialog.dismiss();
        }
        if (caloriesNotAvailableRationaleDialog != null && caloriesNotAvailableRationaleDialog.isShowing()){
            caloriesNotAvailableRationaleDialog.dismiss();
        }
        if (invalidRunDialog != null && invalidRunDialog.isShowing()){
            invalidRunDialog.dismiss();
        }
        super.onDestroyView();
    }


    @Override
    public void showFlaggedRunDialog(final Run flaggedRun) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        invalidRunDialog = builder.setTitle(getString(R.string.invalid_run_title))
                .setMessage(getString(R.string.flagged_run_message))
                .setPositiveButton(getString(R.string.flagged_run_feedback), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getFragmentController().performOperation(IFragmentController.TAKE_FLAGGED_RUN_FEEDBACK, flaggedRun);
                    }
                }).setNegativeButton(getString(R.string.ok), null)
                .show();
    }


    @Override
    public void showCaloriesNotAvailableRationale() {
        if (MainApplication.getInstance().getBodyWeight() <= 0){
            weightInputDialog = Utils.showWeightInputDialog(getActivity());
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            caloriesNotAvailableRationaleDialog = builder.setTitle(getString(R.string.calories_not_available_rationale_title))
                    .setMessage(getString(R.string.calories_not_available_rationale_message_with_weight))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onSelectWorkoutWithIssue(Run selectedWorkout) {
        getFragmentController().replaceFragment(PastWorkoutIssueFragment
                .newInstance(FeedbackCategory.PAST_WORKOUT.copy(), selectedWorkout), true);
    }
}




