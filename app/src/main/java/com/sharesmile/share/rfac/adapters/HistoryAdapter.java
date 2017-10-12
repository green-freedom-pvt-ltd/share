package com.sharesmile.share.rfac.adapters;

import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.core.UnitsManager;
import com.sharesmile.share.rfac.models.Run;
import com.sharesmile.share.rfac.models.RunHistoryDateHeaderItem;
import com.sharesmile.share.rfac.models.RunHistoryDetailsItem;
import com.sharesmile.share.rfac.models.RunHistoryItem;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.utils.DateUtil.HH_MM_AMPM;
import static com.sharesmile.share.utils.DateUtil.USER_FORMAT_DATE_DATE_ONLY;

/**
 * Created by Shine on 13/05/16.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "HistoryAdapter";

    private final AdapterInterface mInterface;
    private List<RunHistoryItem> mItems;
    private boolean isRunSelection;

    public HistoryAdapter(AdapterInterface adapterInterface, boolean isRunSelection) {
        mInterface = adapterInterface;
        this.isRunSelection = isRunSelection;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RunHistoryItem.DATE_HEADER){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.run_history_date_header_item, parent, false);
            return new RunHistoryDateHeaderViewHolder(view);
        }else if (viewType == RunHistoryItem.RUN_ITEM){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.run_history_details_item, parent, false);
            return new RunHistoryDetailsViewHolder(view);
        }else {
            throw new IllegalStateException("Invalid RunHistoryItem viewtype: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == RunHistoryItem.DATE_HEADER){
            RunHistoryDateHeaderItem dateHeaderItem = (RunHistoryDateHeaderItem) mItems.get(position);
            RunHistoryDateHeaderViewHolder headerViewHolder = (RunHistoryDateHeaderViewHolder) holder;
            headerViewHolder.bindData(dateHeaderItem);
        }else if (type == RunHistoryItem.RUN_ITEM){
            RunHistoryDetailsItem detailsItem = (RunHistoryDetailsItem) mItems.get(position);
            RunHistoryDetailsViewHolder detailsViewHolder = (RunHistoryDetailsViewHolder) holder;
            detailsViewHolder.bindData(detailsItem.getWorkout());
        }else {
            throw new IllegalStateException("Invalid RunHistoryItem viewtype: " + type);
        }
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    public void setData(List<RunHistoryItem> data) {
        this.mItems = data;
        notifyDataSetChanged();
    }


    class RunHistoryDetailsViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.tv_run_time)
        TextView mRunTime;
        @BindView(R.id.cause_name)
        TextView mCause;
        @BindView(R.id.distance)
        TextView mDistance;

        @BindView(R.id.impact)
        TextView mImpact;

        @BindView(R.id.duration)
        TextView mDuration;

        @BindView(R.id.calories)
        TextView calories;

        @BindView(R.id.error_indicator)
        ImageView mIndicator;

        @BindView(R.id.content_view)
        CardView mCard;

        public RunHistoryDetailsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (Build.VERSION.SDK_INT >= 21){
                if (isRunSelection){
                    mCard.setElevation(Utils.convertDpToPixel(MainApplication.getContext(), 4));
                }else {
                    mCard.setElevation(Utils.convertDpToPixel(MainApplication.getContext(), 1));
                }
            }
        }

        public void bindData(final Workout workout) {
            long startEpochMillis;
            if (workout.getBeginTimeStamp() != null && workout.getBeginTimeStamp() > 0){
                startEpochMillis = workout.getBeginTimeStamp();
            }else {
                startEpochMillis = workout.getDate().getTime();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startEpochMillis);
            mRunTime.setText(DateUtil.getCustomFormattedDate(cal.getTime(), HH_MM_AMPM));
            mCause.setText(workout.getCauseBrief());
            String distanceCovered = Utils.formatWithOneDecimal(workout.getDistance());
            mDistance.setText(distanceCovered + " km");
            mImpact.setText(UnitsManager.formatRupeeToMyCurrency(workout.getRunAmount()));
            calories.setText(Utils.formatCalories(workout.getCalories() == null ? 0 : workout.getCalories()));
            long timeInSec = Utils.stringToSec(workout.getElapsedTime());
            if (timeInSec >= 60) {
                int timeInMin = (int) (Utils.stringToSec(workout.getElapsedTime()) / 60);
                mDuration.setText(mImpact.getResources().getQuantityString(R.plurals.time_in_min, timeInMin, timeInMin));
            } else {
                mDuration.setText(mImpact.getResources().getQuantityString(R.plurals.time_in_sec, (int) timeInSec, (int) timeInSec));
            }

            if (workout.getIsValidRun()) {
                mIndicator.setVisibility(View.GONE);
                mCard.setCardBackgroundColor(itemView.getResources().getColor(R.color.white));
                mImpact.setPaintFlags(mImpact.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                mIndicator.setVisibility(View.VISIBLE);
                mCard.setCardBackgroundColor(itemView.getResources().getColor(R.color.very_light_grey));
                mImpact.setPaintFlags(mImpact.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onWorkoutClicked(workout);
                }
            });
        }

        private void onWorkoutClicked(Workout workout){
            if (isRunSelection){
                if (mInterface != null){
                    mInterface.onSelectWorkoutWithIssue(Utils.convertWorkoutToRun(workout));
                }
            }else {
                if (workout.getIsValidRun()){
                    if (workout.getCalories() == null || workout.getCalories() <= 0){
                        mInterface.showCaloriesNotAvailableRationale();
                    }
                }else {
                    Logger.d("HistoryAdapter", "Flagged run card clicked: " + Utils.createJSONStringFromObject(workout));
                    mInterface.showFlaggedRunDialog(Utils.convertWorkoutToRun(workout));
                }
            }
        }
    }

    class RunHistoryDateHeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_run_history_date_header)
        TextView dateView;

        @BindView(R.id.tv_header_total_raised)
        TextView totalRaised;

        @BindView(R.id.tv_header_calories)
        TextView calories;

        public RunHistoryDateHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(RunHistoryDateHeaderItem dateHeaderItem){
            String dateString = DateUtil.getCustomFormattedDate(dateHeaderItem.getCalendar().getTime(), USER_FORMAT_DATE_DATE_ONLY);
            dateView.setText(dateString);
            totalRaised.setText(UnitsManager.formatRupeeToMyCurrency(dateHeaderItem.getImpactInDay()));
            calories.setText(Utils.formatCalories(dateHeaderItem.getCaloriesInDay()));
        }
    }

    public interface AdapterInterface {
        void showFlaggedRunDialog(Run invalidRun);
        void showCaloriesNotAvailableRationale();
        void onSelectWorkoutWithIssue(Run selectedWorkout);
    }
}
