package com.sharesmile.share.rfac.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.rfac.models.BaseLeaderBoardItem;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.views.CircularImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by piyush on 9/1/16.
 */
public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.LeaderBoardViewHolder> {

    private static final String TAG = "LeaderBoardAdapter";

    private ItemClickListener mListener;
    private boolean isLeagueBoard = false;
    private List<BaseLeaderBoardItem> mData;
    private Context mContext;

    public LeaderBoardAdapter(Context context, boolean isLeagueBoard) {
        this.mContext = context;
        this.isLeagueBoard = isLeagueBoard;
    }

    public void setItemClickListener(ItemClickListener listener){
        this.mListener = listener;
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

    public void setData(List<BaseLeaderBoardItem> data) {
        Logger.d(TAG, "setData");
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

        @BindView(R.id.tv_list_item_impact)
        TextView mImpact;

        @BindView(R.id.container_list_item)
        CardView container;


        public LeaderBoardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (isLeagueBoard) {
                mProfileImage.setVisibility(View.GONE);
            }
        }

        public void bindData(final BaseLeaderBoardItem leaderboard, int rank) {
            mleaderBoard.setText(String.valueOf(rank));

            ShareImageLoader.getInstance().loadImage(leaderboard.getImage(), mProfileImage,
                    ContextCompat.getDrawable(mleaderBoard.getContext(), R.drawable.placeholder_profile));

            mProfileName.setText(leaderboard.getName());
            String impactString = String.valueOf(Math.round(leaderboard.getAmount()));
            mImpact.setText("\u20B9 " + impactString);

            final int id;
            if (isLeagueBoard) {
                id = LeaderBoardDataStore.getInstance().getMyTeamId();
            } else {
                id = MainApplication.getInstance().getUserID();
            }

            if (id == leaderboard.getId()) {
                container.setCardBackgroundColor(mContext.getResources().getColor(R.color.light_gold));
                container.setCardElevation(3f);
                mleaderBoard.setTextColor(mContext.getResources().getColor(R.color.white));
                mProfileName.setTextColor(mContext.getResources().getColor(R.color.white));
                mImpact.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                container.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
                mleaderBoard.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
                mProfileName.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
                mImpact.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
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
