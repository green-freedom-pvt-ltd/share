package com.sharesmile.share.profile.badges.adapter;

import android.graphics.Color;
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

import java.util.List;

public class CharityOverviewProfileAdapter extends RecyclerView.Adapter<CharityOverviewProfileAdapter.CharityOverviewViewHolder> {

    private static final String TAG = "CharityOverviewProfileAdapter";


    public CharityOverviewProfileAdapter()
    {

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
        return 0;
    }

    class CharityOverviewViewHolder extends RecyclerView.ViewHolder
    {

        public CharityOverviewViewHolder(View itemView) {
            super(itemView);

        }

        public void bindView(int position) {

        }
    }
}
