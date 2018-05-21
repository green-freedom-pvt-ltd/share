package com.sharesmile.share.profile.badges.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.R;
import com.sharesmile.share.home.howitworks.HowItWorksAdapter;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementsViewHolder> {

    private static final String TAG = "AchievementsAdapter";
    List<AchievedBadge> achievedBadges;

    public AchievementsAdapter(List<AchievedBadge> achievedBadges)
    {
        this.achievedBadges = achievedBadges;
    }
    @Override
    public AchievementsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_achievements_row_item, parent, false);
        return new AchievementsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AchievementsViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return achievedBadges.size();
    }

    class AchievementsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView acheivementsImageView;
        TextView acheivementsTitle;
        public AchievementsViewHolder(View itemView) {
            super(itemView);
            acheivementsImageView = itemView.findViewById(R.id.iv_acheivements);
            acheivementsTitle = itemView.findViewById(R.id.tv_acheivements_title);
        }

        public void bindView(int position) {
            acheivementsTitle.setText(achievedBadges.get(position).getBadgeIdAchieved()+"");
        }
    }
}
