package com.sharesmile.share.profile.streak;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.profile.streak.model.Goal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 1/31/18.
 */

public class StreakGoalAdapter extends RecyclerView.Adapter<StreakGoalAdapter.ViewHolder> {

    private static final String TAG = "StreakGoalAdapter";

    private List<Goal> list;
    Context context;

    int position = -1;
    public StreakGoalAdapter(List<Goal> list,Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rv_pick_goal_row_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindData(list.get(i));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        @BindView(R.id.goal_name)
        RadioButton goalName;

        @BindView(R.id.goal_streak_icons)
        LinearLayout goalStreakIcons;

        @BindView(R.id.goal_streak_distance)
        TextView goalStreakDistance;

        @BindView(R.id.goal_row)
        LinearLayout goal_row;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Goal item){
            goalName.setText(item.getName());
            if((position == -1 && MainApplication.getInstance().getUserDetails().getStreakGoalID() == item.getId()) || position == getAdapterPosition())
            {
                goalName.setChecked(true);
                position = getAdapterPosition();
            }else
            {
                goalName.setChecked(false);
            }
            goalStreakIcons.removeAllViews();
            for (int i = 0; i < item.getIconCount(); i++) {
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.streak_icon);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(50, 53);
                layoutParams.setMargins(5,5,5,5);
                imageView.setLayoutParams(layoutParams);
                goalStreakIcons.addView(imageView);
            }
            goalStreakDistance.setText(UnitsManager.formatToMyDistanceUnitWithTwoDecimal(item.getValue()*1000)+" "+ UnitsManager.getDistanceLabel()+" per day");
        }
        @OnClick(R.id.goal_row)
        void onClicked()
        {
                position = getAdapterPosition();
                notifyDataSetChanged();
                ((MainActivity)context).invalidateOptionsMenu();
        }
    }

    public int getPosition()
    {
        return position;
    }

}
