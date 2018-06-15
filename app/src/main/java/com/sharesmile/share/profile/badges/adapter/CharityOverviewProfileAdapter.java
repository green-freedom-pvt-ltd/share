package com.sharesmile.share.profile.badges.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.profile.OpenCharityOverview;
import com.sharesmile.share.profile.model.CategoryStats;
import com.sharesmile.share.profile.model.CharityOverview;

import java.util.List;

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
        CardView charity_overview_card;
        TextView charityCategoryTitle;
        ImageView charityOverviewImageView;
        TextView charityAmount;
        public CharityOverviewViewHolder(View itemView) {
            super(itemView);
            charityCategoryTitle = itemView.findViewById(R.id.tv_charity_category_title);
            charityOverviewImageView = itemView.findViewById(R.id.iv_charity_overview);
            charityAmount = itemView.findViewById(R.id.tv_charity_amount);
            charity_overview_card = itemView.findViewById(R.id.charity_overview_card);

        }

        public void bindView(int position) {
            CategoryStats categoryStats = charityOverview.getCategoryStats().get(position);
            charityCategoryTitle.setText(categoryStats.getCategoryName());
            charityAmount.setText(context.getResources().getString(R.string.rupee_symbol)+categoryStats.getCategoryRaised()+"");
            charity_overview_card.setTag(position);
            charity_overview_card.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            openCharityOverview.openCharityOverviewFragment((int)view.getTag());
        }
    }
}