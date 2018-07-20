package com.sharesmile.share.profile.badges.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.profile.badges.SeeAchievedBadge;
import com.sharesmile.share.profile.badges.model.AchievedBadgeCount;
import com.sharesmile.share.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.sharesmile.share.core.application.MainApplication.getContext;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementsViewHolder> {

    private static final String TAG = "AchievementsAdapter";
    private List<AchievedBadgeCount> achievedBadgeCounts;
    private Context context;
    private SeeAchievedBadge seeAchievedBadge;

    public AchievementsAdapter(List<AchievedBadgeCount> achievedBadgeCounts, Context context, SeeAchievedBadge seeAchievedBadge) {
        this.achievedBadgeCounts = achievedBadgeCounts;
        this.context = context;
        this.seeAchievedBadge = seeAchievedBadge;
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
        if(achievedBadgeCounts.size()>4)
        return SharedPrefsManager.getInstance().getBoolean(Constants.PREF_ACHIEVED_BADGES_OPEN)?achievedBadgeCounts.size():4;
        else return achievedBadgeCounts.size()/*4*/;
    }

    class AchievementsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView acheivementsImageView;
        ImageView starImageView;
        TextView acheivementsTitle;
        LinearLayout badgeLayout;
        TextView badgeCount;
        RelativeLayout moreLayout;
        RelativeLayout badgeRl;
        TextView tvMore;

        public AchievementsViewHolder(View itemView) {
            super(itemView);
            acheivementsImageView = itemView.findViewById(R.id.iv_acheivements);
            starImageView = itemView.findViewById(R.id.iv_star);
            acheivementsTitle = itemView.findViewById(R.id.tv_acheivements_title);
            badgeLayout = itemView.findViewById(R.id.badge_layout);
            badgeCount = itemView.findViewById(R.id.tv_badge_count);
            moreLayout = itemView.findViewById(R.id.more_layout);
            tvMore = itemView.findViewById(R.id.tv_more);
            badgeRl = itemView.findViewById(R.id.badge_rl);

        }

        public void bindView(int position) {
            badgeLayout.setTag(position);
            badgeLayout.setOnClickListener(this);
            if(position>achievedBadgeCounts.size()-1)
            {
                tvMore.setText("");
                moreLayout.setVisibility(View.VISIBLE);
                badgeRl.setVisibility(View.GONE);
                acheivementsTitle.setText("");

            }else
            {
            if((position==3 && !SharedPrefsManager.getInstance().getBoolean(Constants.PREF_ACHIEVED_BADGES_OPEN) && achievedBadgeCounts.size()>4)) {
                tvMore.setText(achievedBadgeCounts.size()-3+" More");
                moreLayout.setVisibility(View.VISIBLE);
                badgeRl.setVisibility(View.GONE);
                acheivementsTitle.setText("");
            }else {
                moreLayout.setVisibility(View.GONE);
                badgeRl.setVisibility(View.VISIBLE);
                List<Badge> badges = MainApplication.getInstance().getDbWrapper().getBadgeDao().queryBuilder()
                        .where(BadgeDao.Properties.BadgeId.eq(achievedBadgeCounts.get(position).getAchievedBadgeId())).list();
                if (badges != null && badges.size() > 0) {
                    Badge badge = badges.get(0);
                    String s = badge.getName();
                    acheivementsTitle.setText(s);
                    ShareImageLoader.getInstance().loadImage(badge.getImageUrl(), acheivementsImageView,
                            ContextCompat.getDrawable(context, R.drawable.badge_image));
                    int starCount = badge.getNoOfStars();
                    Utils.setStarImage(starCount, starImageView,badge.getType());
                    if (achievedBadgeCounts.get(position).getCount() > 1) {
                        badgeCount.setVisibility(View.VISIBLE);
                        badgeCount.setText("x" + achievedBadgeCounts.get(position).getCount());
                    } else {
                        badgeCount.setVisibility(View.INVISIBLE);
                    }

                }
            }
            }
        }

        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            if(position>achievedBadgeCounts.size()-1)
            {

            }else {
                if ((position == 3 && !SharedPrefsManager.getInstance().getBoolean(Constants.PREF_ACHIEVED_BADGES_OPEN))) {
                    SharedPrefsManager.getInstance().setBoolean(Constants.PREF_ACHIEVED_BADGES_OPEN, true);
                    notifyItemRangeChanged(3, achievedBadgeCounts.size() - 1);
                } else {
                    AchievedBadgeCount achievedBadgeCount = achievedBadgeCounts.get(position);
                    List<Badge> badges = MainApplication.getInstance().getDbWrapper().getBadgeDao().queryBuilder()
                            .where(BadgeDao.Properties.BadgeId.eq(achievedBadgeCounts.get(position).getAchievedBadgeId())).list();
                    if (badges != null && badges.size() > 0) {
                        seeAchievedBadge.showBadgeDetails(achievedBadgeCount.getAchievedBadgeId(), badges.get(0).getType());
                    }
                }
            }
        }
    }
}
