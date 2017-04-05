package com.sharesmile.share.rfac.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.LeaderBoard;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.views.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by piyush on 9/1/16.
 */
public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.LeaderBoardViewHolder> {

    private ItemClickListener mListener;
    private boolean isLeagueBoard = false;
    private List<LeaderBoard> mData;
    private Context mContext;

    public LeaderBoardAdapter(Context context, List<LeaderBoard> leaderBoard) {
        this.mData = leaderBoard;
        this.mContext = context;
    }

    public LeaderBoardAdapter(Context context, List<LeaderBoard> leaderBoard, boolean isLeagueBoard, ItemClickListener lis) {
        this(context, leaderBoard);
        this.isLeagueBoard = isLeagueBoard;
        mListener = lis;
    }

    @Override
    public LeaderBoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_list_item, parent, false);
        return new LeaderBoardViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public void setData(List<LeaderBoard> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(LeaderBoardViewHolder holder, int position) {
        holder.bindData(mData.get(position), position + 1);
    }

    public LeaderBoardViewHolder createMyViewHolder(View myListItem){
        return new LeaderBoardViewHolder(myListItem);
    }

    public class LeaderBoardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.id_leaderboard)
        TextView mleaderBoard;
        @BindView(R.id.img_profile)
        CircularImageView mProfileImage;

        @BindView(R.id.tv_profile_name)
        TextView mProfileName;

        @BindView(R.id.last_week_distance)
        TextView mlastWeekDistance;

        @BindView(R.id.containerView)
        CardView container;


        public LeaderBoardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (isLeagueBoard) {
                mProfileImage.setVisibility(View.GONE);
            }
        }

        public void bindData(final LeaderBoard leaderboard, int rank) {
            mleaderBoard.setText(String.valueOf(rank));

            if (!TextUtils.isEmpty(leaderboard.getSocial_thumb())) {
                Picasso.with(mContext).
                        load(leaderboard.getSocial_thumb()).
                        placeholder(R.drawable.placeholder_profile).
                        into(mProfileImage);
            } else {
                mProfileImage.setImageResource(R.drawable.placeholder_profile);
            }

            String firstName = leaderboard.getFirst_name().substring(0, 1).toUpperCase() + leaderboard.getFirst_name().substring(1);
            String name = firstName;
            if (!TextUtils.isEmpty(leaderboard.getLast_name())) {
                String lastName = leaderboard.getLast_name().substring(0, 1).toUpperCase() + leaderboard.getLast_name().substring(1);
                name = firstName + ' ' + lastName;
            }
            mProfileName.setText(name);
            String last_Week_Distance = String.format("%.2f", leaderboard.getLast_week_distance());
            mlastWeekDistance.setText(last_Week_Distance + " Km");

            final int id;
            if (isLeagueBoard) {
                id = SharedPrefsManager.getInstance().getInt(Constants.PREF_LEAGUE_TEAM_ID);
            } else {
                id = MainApplication.getInstance().getUserID();
            }

            if (id == leaderboard.getId()) {
                container.setCardBackgroundColor(mContext.getResources().getColor(R.color.light_gold));
                container.setCardElevation(3f);
                mleaderBoard.setTextColor(mContext.getResources().getColor(R.color.white));
                mProfileName.setTextColor(mContext.getResources().getColor(R.color.white));
                mlastWeekDistance.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                container.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
                mleaderBoard.setTextColor(mContext.getResources().getColor(R.color.black));
                mProfileName.setTextColor(mContext.getResources().getColor(R.color.black));
                mlastWeekDistance.setTextColor(mContext.getResources().getColor(R.color.black));
                if(isLeagueBoard) container.setCardElevation(3f);
                else container.setCardElevation(.5f);
                container.setOnClickListener(null);
            }

            if (isLeagueBoard) {
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(leaderboard.getId());
                    }
                });
            }
        }
    }

    public interface ItemClickListener {
        public void onItemClick(long id);
    }
}
