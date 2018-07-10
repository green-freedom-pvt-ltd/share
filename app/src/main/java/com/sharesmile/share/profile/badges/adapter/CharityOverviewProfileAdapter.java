package com.sharesmile.share.profile.badges.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.profile.OpenCharityOverview;
import com.sharesmile.share.profile.model.CategoryStats;
import com.sharesmile.share.profile.model.CharityOverview;
import com.sharesmile.share.utils.Utils;

import java.util.List;

import static com.sharesmile.share.core.application.MainApplication.getContext;

public class CharityOverviewProfileAdapter extends RecyclerView.Adapter<CharityOverviewProfileAdapter.CharityOverviewViewHolder> {

    private static final String TAG = "CharityOverviewProfileAdapter";

    OpenCharityOverview openCharityOverview;
    CharityOverview charityOverview;
    Context context;
    public CharityOverviewProfileAdapter(OpenCharityOverview openCharityOverview, CharityOverview charityOverview,Context context)
    {
        this.context = context;
        this.openCharityOverview = openCharityOverview;
        this.charityOverview = charityOverview;
    }
    @Override
    public CharityOverviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_charity_overview_profile_row, parent, false);
        return new CharityOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CharityOverviewViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return charityOverview.getCategoryStats().size();
    }

    class CharityOverviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView charityOverviewCard;
        TextView charityCategoryTitle;
        ImageView charityOverviewImageView;
        TextView charityAmount;
        LinearLayout starLayout;
        LinearLayout charityOverviewLayout;
        public CharityOverviewViewHolder(View itemView) {
            super(itemView);
            charityCategoryTitle = itemView.findViewById(R.id.tv_charity_category_title);
            charityOverviewImageView = itemView.findViewById(R.id.iv_charity_overview);
            charityAmount = itemView.findViewById(R.id.tv_charity_amount);
            charityOverviewCard = itemView.findViewById(R.id.charity_overview_card);
            charityOverviewLayout = itemView.findViewById(R.id.charity_overview_layout);
            starLayout = itemView.findViewById(R.id.star_layout);

        }

        public void bindView(int position) {
            CategoryStats categoryStats = charityOverview.getCategoryStats().get(position);
            charityCategoryTitle.setText(categoryStats.getCategoryName());
            charityAmount.setText(UnitsManager.formatRupeeToMyCurrency(categoryStats.getCategoryRaised()));
            charityOverviewCard.setTag(position);
            charityOverviewCard.setOnClickListener(this);
            Utils.setGradientBackground(Color.parseColor("#FAAFD0"),Color.parseColor("#F37181"),charityCategoryTitle);
            Utils.addStars(starLayout,categoryStats.getCategoryNoOfStars(),context);
            ShareImageLoader.getInstance().loadImage(categoryStats.getCategoryImageUrl(),charityOverviewImageView,
                    ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);;
            if(position==0)
            {
                layoutParams.setMargins(Utils.dpToPx(24),0,Utils.dpToPx(12),0);
            }else if(position==charityOverview.getCategoryStats().size()-1)
            {
                layoutParams.setMargins(Utils.dpToPx(12),0,Utils.dpToPx(24),0);
            }else
            {
                layoutParams.setMargins(Utils.dpToPx(12),0,Utils.dpToPx(12),0);
            }
            charityOverviewLayout.setLayoutParams(layoutParams);
        }

        @Override
        public void onClick(View view) {
            openCharityOverview.openCharityOverviewFragment((int)view.getTag());
        }
    }
}
