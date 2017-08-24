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
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.views.CircularImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by piyush on 9/1/16.
 */
public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.LeaderBoardViewHolder> {

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

        @BindView(R.id.tv_distance)
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

        public void bindData(final BaseLeaderBoardItem leaderboard, int rank) {
            mleaderBoard.setText(String.valueOf(rank));

            ShareImageLoader.getInstance().loadImage(leaderboard.getImage(), mProfileImage,
                    ContextCompat.getDrawable(mleaderBoard.getContext(), R.drawable.placeholder_profile));

//            String firstName = leaderboard.getFirst_name();
//            if (!TextUtils.isEmpty(firstName)){
//                if (firstName.length() > 1){
//                    firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
//                }
//            }
//
//            String lastName = leaderboard.getLast_name();
//            if (!TextUtils.isEmpty(lastName)){
//                if (lastName.length() > 1){
//                    lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
//                }
//            }

            mProfileName.setText(leaderboard.getName());
            String distanceString;
            Float lastWeekDist = leaderboard.getDistance();
            if (lastWeekDist > 10f){
                distanceString = String.valueOf(Math.round(lastWeekDist));
            }else {
                distanceString = String.format("%.1f", lastWeekDist);
            }

            mlastWeekDistance.setText(distanceString + " Km");

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
                mlastWeekDistance.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                container.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
                mleaderBoard.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
                mProfileName.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
                mlastWeekDistance.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
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
