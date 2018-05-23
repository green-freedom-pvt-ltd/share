package com.sharesmile.share.profile.badges.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.profile.badges.model.HallOfFameData;

import java.util.ArrayList;
import java.util.List;

public class HallOfFameAdapter extends RecyclerView.Adapter<HallOfFameAdapter.AchievementsViewHolder> {

    private static final String TAG = "HallOfFameAdapter";
    ArrayList<HallOfFameData> achievedBadges;

    public HallOfFameAdapter(ArrayList<HallOfFameData> achievedBadges)
    {
        this.achievedBadges = achievedBadges;
    }
    @Override
    public AchievementsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_hall_of_fame_row_item, parent, false);
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
        TextView achievementBadgeTitle;
        BadgeDao badgeDao;
        TextView badgeCount;

        public AchievementsViewHolder(View itemView) {
            super(itemView);
            achievementBadgeTitle = itemView.findViewById(R.id.tv_acheivements_title);
            badgeCount = itemView.findViewById(R.id.tv_badge_count);
            badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
        }

        public void bindView(int position) {
            List<Badge> badges = badgeDao.queryBuilder().where(BadgeDao.Properties.BadgeId.eq(achievedBadges.get(position).getBadgeId())).limit(1).list();
            if(badges!=null && badges.size()>0) {
                Badge badge = badges.get(0);
                achievementBadgeTitle.setText(badge.getName());
                badgeCount.setText("x"+achievedBadges.get(position).getCount());
            }
        }
    }
}
