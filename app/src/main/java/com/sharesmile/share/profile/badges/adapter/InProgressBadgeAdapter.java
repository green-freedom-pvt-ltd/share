package com.sharesmile.share.profile.badges.adapter;

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
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.utils.Utils;

import java.util.List;

public class InProgressBadgeAdapter extends RecyclerView.Adapter<InProgressBadgeAdapter.AchievementsViewHolder> {

    private static final String TAG = "InProgressBadgeAdapter";
    List<AchievedBadge> achievedBadges;

    public InProgressBadgeAdapter(List<AchievedBadge> achievedBadges)
    {
        this.achievedBadges = achievedBadges;
    }
    @Override
    public AchievementsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_achievement_badges_row_item, parent, false);
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
        TextView achievementBadgeDescription;
        TextView achievementAmount;
        View levelProgressBar;
        BadgeDao badgeDao;
        ImageView badgeImageView;
        ImageView starImageView;
        public AchievementsViewHolder(View itemView) {
            super(itemView);
            badgeImageView = itemView.findViewById(R.id.iv_badge);
            starImageView = itemView.findViewById(R.id.iv_star);
            achievementBadgeTitle = itemView.findViewById(R.id.tv_achievement_badge_title);
            achievementBadgeDescription = itemView.findViewById(R.id.tv_achievement_badge_description);
            achievementAmount = itemView.findViewById(R.id.tv_achievement_amount);
            levelProgressBar = itemView.findViewById(R.id.level_progress_bar);

            badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
        }

        public void bindView(int position) {
            AchievedBadge achievedBadge = achievedBadges.get(position);
            achievementBadgeTitle.setText(achievedBadge.getCauseName() + "");
            List<Badge> badges = badgeDao.queryBuilder().where(BadgeDao.Properties.BadgeId.eq(achievedBadge.getBadgeIdInProgress())).limit(1).list();
            if(badges!=null && badges.size()>0) {
                Badge badge = badges.get(0);
                if(badge.getType().equalsIgnoreCase(Constants.BADGE_TYPE_STREAK))
                {
                    achievementAmount.setText(((int)achievedBadge.getParamDone()) + " days/ "+((int)badge.getBadgeParameter())+" days");
                }else
                {
                    achievementAmount.setText(UnitsManager.formatToMyDistanceUnitWithTwoDecimal((float) (achievedBadge.getParamDone()*1000)) + " "+UnitsManager.getDistanceLabel()+"/ "+UnitsManager.formatToMyDistanceUnitWithTwoDecimal((float) (badge.getBadgeParameter()*1000))+" "+UnitsManager.getDistanceLabel());
                }
                Utils.setStarImage(badge.getNoOfStars(),starImageView);
                float weight = ((float) (achievedBadge.getParamDone()/ badge.getBadgeParameter()));
                ((LinearLayout.LayoutParams)levelProgressBar.getLayoutParams()).weight = weight>1?1:weight;
                achievementBadgeDescription.setText(badge.getDescription1());
            }
        }
    }
}
