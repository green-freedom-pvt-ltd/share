package com.sharesmile.share.profile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.profile.model.CategoryStats;
import com.sharesmile.share.profile.model.CauseStats;

public class CharityCauseDetailsAdapter extends RecyclerView.Adapter<CharityCauseDetailsAdapter.CauseDetailViewHolder> {

    private static final String TAG = "CharityCauseDetailsAdapter";
    CategoryStats categoryStats;
    Context context;
    public CharityCauseDetailsAdapter(Context context,CategoryStats categoryStats) {
    this.categoryStats = categoryStats;
    this.context = context;
    }

    @Override
    public CauseDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_charity_overview_row_item, parent, false);
        return new CauseDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CauseDetailViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return categoryStats.getCauseStats().size();
    }

    class CauseDetailViewHolder extends RecyclerView.ViewHolder{
        ImageView ivCause;
        TextView causeName;
        TextView causeDescription;
        LinearLayout layoutStar;
        TextView charityAmount;
        TextView charityWorkout;
        public CauseDetailViewHolder(View itemView) {
            super(itemView);
            ivCause = itemView.findViewById(R.id.iv_cause);
            causeName = itemView.findViewById(R.id.tv_cause_name);
            causeDescription = itemView.findViewById(R.id.tv_cause_description);
            layoutStar = itemView.findViewById(R.id.layout_star);
            charityAmount = itemView.findViewById(R.id.tv_charity_amount);
            charityWorkout = itemView.findViewById(R.id.tv_charity_workout);
        }

        public void bindView(int position) {
            CauseStats causeStats = categoryStats.getCauseStats().get(position);
            causeName.setText(causeStats.getCauseName());
            charityAmount.setText(UnitsManager.formatRupeeToMyCurrency(causeStats.getCause_raised()));
            charityWorkout.setText(causeStats.getCause_workouts()+"");


        }

    }
}
