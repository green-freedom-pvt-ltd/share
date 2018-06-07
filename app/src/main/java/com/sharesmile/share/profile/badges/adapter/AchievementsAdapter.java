package com.sharesmile.share.profile.badges.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
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
import com.sharesmile.share.profile.badges.model.AchievedBadgeCount;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementsViewHolder> {

    private static final String TAG = "AchievementsAdapter";
    List<AchievedBadgeCount> achievedBadgeCounts;
    Context context;
    SeeAchivedBadge seeAchivedBadge;
    int leftPX;
    int rightPX;
    LinearLayout.LayoutParams lastLayoutParams;
    LinearLayout.LayoutParams layoutParams;

    public AchievementsAdapter(List<AchievedBadgeCount> achievedBadgeCounts, Context context, SeeAchivedBadge seeAchivedBadge) {
        this.achievedBadgeCounts = achievedBadgeCounts;
        this.context = context;
        this.seeAchivedBadge = seeAchivedBadge;
        Resources r = context.getResources();
        leftPX = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                r.getDisplayMetrics()
        );
        rightPX = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,
                r.getDisplayMetrics()
        );
        lastLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lastLayoutParams.setMargins(leftPX,0,rightPX,0);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(leftPX,0,0,0);

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
        return achievedBadgeCounts.size();
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
            List<Badge> badges  = MainApplication.getInstance().getDbWrapper().getBadgeDao().queryBuilder()
                        .where(BadgeDao.Properties.BadgeId.eq(achievedBadgeCounts.get(position).getAchievedBadgeId())).list();


            if (badges != null && badges.size() > 0) {
                Badge badge = badges.get(0);
                String s = badge.getName();
                acheivementsTitle.setText(s);
                acheivementsImageView.setImageResource(R.drawable.badge_image);
            }
            badgeLayout.setTag(position);
            badgeLayout.setOnClickListener(this);
            if(position==achievedBadgeCounts.size()-1)
            {
                badgeLayout.setLayoutParams(lastLayoutParams);
            }else
            {
                badgeLayout.setLayoutParams(layoutParams);
            }
        }

        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            AchievedBadgeCount achievedBadgeCount = achievedBadgeCounts.get(position);
            List<Badge> badges  = MainApplication.getInstance().getDbWrapper().getBadgeDao().queryBuilder()
                    .where(BadgeDao.Properties.BadgeId.eq(achievedBadgeCounts.get(position).getAchievedBadgeId())).list();
            if(badges!=null && badges.size()>0) {
                seeAchivedBadge.showBadgeDetails(achievedBadgeCount.getAchievedBadgeId(), badges.get(0).getType());
            }
        }
    }
}
