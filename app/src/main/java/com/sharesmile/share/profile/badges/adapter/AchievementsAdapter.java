package com.sharesmile.share.profile.badges.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.home.howitworks.HowItWorksAdapter;
import com.sharesmile.share.profile.badges.SeeAchivedBadge;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementsViewHolder> {

    private static final String TAG = "AchievementsAdapter";
    List<AchievedBadge> achievedBadges;
    Context context;
    SeeAchivedBadge seeAchivedBadge;

    public AchievementsAdapter(List<AchievedBadge> achievedBadges, Context context, SeeAchivedBadge seeAchivedBadge) {
        this.achievedBadges = achievedBadges;
        this.context = context;
        this.seeAchivedBadge = seeAchivedBadge;
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

    class AchievementsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView acheivementsImageView;
        TextView acheivementsTitle;
        LinearLayout badgeLayout;

        public AchievementsViewHolder(View itemView) {
            super(itemView);
            acheivementsImageView = itemView.findViewById(R.id.iv_acheivements);
            acheivementsTitle = itemView.findViewById(R.id.tv_acheivements_title);
            badgeLayout = itemView.findViewById(R.id.badge_layout);
        }

        public void bindView(int position) {
            List<Badge> badges;
            boolean achieved = false;
            if (achievedBadges.get(position).getBadgeIdAchieved() != 0) {
               badges  = MainApplication.getInstance().getDbWrapper().getBadgeDao().queryBuilder()
                        .where(BadgeDao.Properties.BadgeId.eq(achievedBadges.get(position).getBadgeIdAchieved())).list();
                achieved = true;
            }else
            {
                badges  = MainApplication.getInstance().getDbWrapper().getBadgeDao().queryBuilder()
                        .where(BadgeDao.Properties.BadgeId.eq(achievedBadges.get(position).getBadgeIdInProgress())).list();
                achieved = false;
            }
            if (badges != null && badges.size() > 0) {
                Badge badge = badges.get(0);
                String s = badge.getName();
                acheivementsTitle.setText(s);
                acheivementsImageView.setImageResource(R.drawable.badge_image);
                if(achieved)
                {
                    acheivementsImageView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                }else
                {
                    acheivementsImageView.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                }

            }
            badgeLayout.setTag(position);
            badgeLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            AchievedBadge achievedBadge = achievedBadges.get(position);
            seeAchivedBadge.showBadgeDetails(achievedBadge.getBadgeIdInProgress(),achievedBadge.getBadgeType());
        }
    }
}
