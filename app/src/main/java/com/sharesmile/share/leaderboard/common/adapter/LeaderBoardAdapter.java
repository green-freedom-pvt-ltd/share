package com.sharesmile.share.leaderboard.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.leaderboard.common.LeaderBoardItem;
import com.sharesmile.share.leaderboard.common.model.BaseLeaderBoardItem;
import com.sharesmile.share.leaderboard.referprogram.SMCLeagueBoardBannerPagerAdapter;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Models.LeagueBoard;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.application.MainApplication.getContext;

/**
 * Created by piyush on 9/1/16.
 */
public class LeaderBoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "LeaderBoardAdapter";

    private Parent mParent;
    private List<BaseLeaderBoardItem> itemList;
    private Context mContext;

    int headerOffSet = 0;
    int noOfUnsyncWorkout = 0;
    boolean showUnsync = false;
    boolean showMeals = false;
    Activity activity;

    public LeaderBoardAdapter(Context context, Parent parent, List<Workout> mWorkoutList, boolean showMeals, Activity activity) {

        this.mContext = context;
        this.mParent = parent;
        this.showMeals = showMeals;
        this.activity = activity;
        headerOffSet = (mParent != null && mParent.toShowBanner() > 0) ? 1 : 0;
        Logger.d(TAG, "LeaderBoardAdapter, setting headeroffset as " + headerOffSet);
        noOfUnsyncWorkout = mWorkoutList!=null?mWorkoutList.size():0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LeaderBoardItem.BANNER_HEADER){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.leaderboard_banner_container, parent, false);
            return new BannerHeaderViewHolder(view);
        }else if (viewType == LeaderBoardItem.ROW_ITEM){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.leaderboard_list_item, parent, false);
            return new LeaderBoardViewHolder(view);
        } else if (viewType == LeaderBoardItem.BANNER_HEADER_SMC) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.leaderboard_banner_container, parent, false);
            return new SMCBannerHeaderViewHolder(view);
        }else {
            throw new IllegalStateException("Invalid LeaderBoardItem viewtype: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        int type = getItemViewType(i);
        if (type == LeaderBoardItem.BANNER_HEADER){
            LeagueBoard leagueBoard = mParent.getBannerData();
            BannerHeaderViewHolder headerViewHolder =  (BannerHeaderViewHolder) viewHolder;
            headerViewHolder.bindData(leagueBoard);
        }else if (type == LeaderBoardItem.ROW_ITEM){
            LeaderBoardViewHolder rowItemViewHolder = (LeaderBoardViewHolder) viewHolder;
            int position = i - headerOffSet;
            rowItemViewHolder.bindData(itemList.get(position), itemList.get(position).getRanking());
        } else if (type == LeaderBoardItem.BANNER_HEADER_SMC) {
            SMCBannerHeaderViewHolder headerViewHolder = (SMCBannerHeaderViewHolder) viewHolder;
            headerViewHolder.bindData();
        }else {
            throw new IllegalStateException("Invalid LeaderBoardItem viewtype: " + type);
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? (itemList.size() + headerOffSet) : 0;
    }

    @Override
    public int getItemViewType(int position) {
//        Logger.d(TAG, "getItemViewType for position: " + position + ", headerOffset = " + headerOffSet);
        if (headerOffSet > 0){
            if (position == 0){
                if (mParent.toShowBanner() == 1)
                    return LeaderBoardItem.BANNER_HEADER;
                else return LeaderBoardItem.BANNER_HEADER_SMC;
            }
        }
        return LeaderBoardItem.ROW_ITEM;
    }

    public void setData(List<BaseLeaderBoardItem> data) {
        Logger.d(TAG, "setData");
        this.itemList = data;
        notifyDataSetChanged();
    }

    public int getHeaderOffSet(){
        return headerOffSet;
    }

    public LeaderBoardViewHolder createMyViewHolder(View myListItem){
        return new LeaderBoardViewHolder(myListItem);
    }

    public void setShowUnsync(boolean b) {
        showUnsync = b;
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

        @BindView(R.id.container_show_unsync)
        LinearLayout containerShowUnsync;

        @BindView(R.id.not_sync_tv)
        TextView notSync;

        public LeaderBoardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mParent.toShowLogo()) {
                mProfileImage.setVisibility(View.VISIBLE);
            }else {
                mProfileImage.setVisibility(View.GONE);
            }
        }

        public void bindData(final BaseLeaderBoardItem leaderboard, int rank) {
            if(showUnsync && itemList.indexOf(leaderboard)==0 && headerOffSet == 0 && noOfUnsyncWorkout>0)
            {
                if(noOfUnsyncWorkout==1) {
                    notSync.setText(noOfUnsyncWorkout + " workout not yet synced!");
                }else
                {
                    notSync.setText(noOfUnsyncWorkout + " workouts not yet synced!");
                }
                containerShowUnsync.setVisibility(View.VISIBLE);
            }else
            {
                containerShowUnsync.setVisibility(View.GONE);
            }
            mleaderBoard.setText(String.valueOf(rank));

            ShareImageLoader.getInstance().loadImage(leaderboard.getImage(), mProfileImage,
                    ContextCompat.getDrawable(mleaderBoard.getContext(), R.drawable.placeholder_profile));

            mProfileName.setText(leaderboard.getName());
            if(showMeals)
            {
                mImpact.setText(((int) leaderboard.getAmount()) + "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mImpact.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.smc_bowl_nav_black, 0);
                    mImpact.setCompoundDrawablePadding(10);
                }
            }else {
                mImpact.setText(UnitsManager.formatRupeeToMyCurrency(leaderboard.getAmount()));
            }

            final int id = mParent.getMyId();

            if (id == leaderboard.getId()) {
                container.setCardBackgroundColor(mContext.getResources().getColor(R.color.bright_sky_blue));
                container.setCardElevation(3f);
                mleaderBoard.setTextColor(mContext.getResources().getColor(R.color.white));
                mProfileName.setTextColor(mContext.getResources().getColor(R.color.white));
                mImpact.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                container.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
                mleaderBoard.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
                mProfileName.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
                mImpact.setTextColor(mContext.getResources().getColor(R.color.greyish_brown_two));
                if(mParent.toShowLogo()) container.setCardElevation(3f);
                else container.setCardElevation(.5f);
                container.setOnClickListener(null);
            }

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mParent.onItemClick(leaderboard.getId());
                }
            });
        }

        public void show(){
            Logger.d(TAG, "show");
            container.setVisibility(View.VISIBLE);
            container.requestLayout();
        }

        public void hide(){
            Logger.d(TAG, "hide");
            container.setVisibility(View.GONE);
        }

        public boolean isVisible(){
            return container.getVisibility() == View.VISIBLE && container.getHeight() > 0;
        }

    }

    public class SMCBannerHeaderViewHolder extends RecyclerView.ViewHolder {
        SMCLeagueBoardBannerPagerAdapter smcLeagueBoardBannerPagerAdapter;

        @BindView(R.id.banner_view_pager)
        ViewPager bannerViewPager;

        @BindView(R.id.banner_carousel_indicator_holder)
        LinearLayout carouselIndicatorsHolder;

        private int currentPageIndex;
        private List<ImageView> indicators = new ArrayList<>();
        Timer timer;
        public SMCBannerHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            initBanner();
        }

        private void initBanner() {
            Logger.d(TAG, "initBanner");
            smcLeagueBoardBannerPagerAdapter = new SMCLeagueBoardBannerPagerAdapter(activity);
            bannerViewPager.setAdapter(smcLeagueBoardBannerPagerAdapter);
            currentPageIndex = 0;
            bannerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    //Position is the index of the newPage on screen
                    /*if (position < currentPageIndex){
                        // User moved to left
                        AnalyticsEvent.create(Event.ON_SWIPE_LEAGEBOARD_BANNER)
                                .put("direction", "left")
                                .buildAndDispatch();

                    }else if (position > currentPageIndex) {
                        // User moved to right
                        AnalyticsEvent.create(Event.ON_SWIPE_LEAGEBOARD_BANNER)
                                .put("direction", "right")
                                .buildAndDispatch();
                    }*/
                    // Setting newImage as current image
                    setSelectedPage(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    mParent.enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
                }
            });
            autoSroll();
            addIndicators(2, currentPageIndex);
        }

        private void addIndicators(int numPages, int displayImageIndex) {
            Logger.d(TAG, "addIndicators, numpages = " + numPages + ", displayImageIndex = " + displayImageIndex);
            if (Utils.isCollectionFilled(indicators) && indicators.size() == numPages) {
                // Indicators already added, no need to add again.
                Logger.d(TAG, "no need to add indicators again");
                return;
            } else {
                carouselIndicatorsHolder.removeAllViews();
                indicators = new ArrayList<>();
                for (int i = 0; i < numPages; i++) {
                    View indicatorContainer = LayoutInflater.from(getContext()).inflate(R.layout.carousel_indicator,
                            carouselIndicatorsHolder, false);
                    indicators.add((ImageView) indicatorContainer.findViewById(R.id.iv_indicator_circle));
                    if (i == displayImageIndex) {
                        indicators.get(i).setSelected(true);
                    }
                    carouselIndicatorsHolder.addView(indicatorContainer);
                }
            }
        }

        private void setSelectedPage(int newPosition) {
            int prevIndex = currentPageIndex;
            // Setting newPosition as current page
            currentPageIndex = newPosition;
            if (indicators != null) {
                indicators.get(prevIndex).setSelected(false);
                indicators.get(currentPageIndex).setSelected(true);
            }
        }

        public void bindData() {

        }

        public void autoSroll() {
            Handler handler = new Handler();
            Runnable update = new Runnable() {
                @Override
                public void run() {
                    int position = 0;
                    if (currentPageIndex == 1) {
                        position = 0;
                    } else {
                        position = 1;
                    }
                    bannerViewPager.setCurrentItem(position, true);
                }
            };
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(update);
                }
            }, 5000, 7500);
        }
    }
    public class BannerHeaderViewHolder extends RecyclerView.ViewHolder{

        LeagueBoardBannerPagerAdapter bannerPagerAdapter;

        @BindView(R.id.banner_view_pager)
        ViewPager bannerViewPager;

        @BindView(R.id.banner_carousel_indicator_holder)
        LinearLayout carouselIndicatorsHolder;

        private int currentPageIndex;
        private List<ImageView> indicators = new ArrayList<>();

        public BannerHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            initBanner();
        }

        private void initBanner(){
            Logger.d(TAG, "initBanner");
            bannerPagerAdapter = new LeagueBoardBannerPagerAdapter();
            bannerViewPager.setAdapter(bannerPagerAdapter);
            currentPageIndex = 0;
            bannerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    //Position is the index of the newPage on screen
                    if (position < currentPageIndex){
                        // User moved to left
                        AnalyticsEvent.create(Event.ON_SWIPE_LEAGEBOARD_BANNER)
                                .put("direction", "left")
                                .buildAndDispatch();

                    }else if (position > currentPageIndex) {
                        // User moved to right
                        AnalyticsEvent.create(Event.ON_SWIPE_LEAGEBOARD_BANNER)
                                .put("direction", "right")
                                .buildAndDispatch();
                    }
                    // Setting newImage as current image
                    setSelectedPage(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    mParent.enableDisableSwipeRefresh( state == ViewPager.SCROLL_STATE_IDLE );
                }
            });
            addIndicators(2, currentPageIndex);
        }

        private void addIndicators(int numPages, int displayImageIndex){
            Logger.d(TAG, "addIndicators, numpages = "+ numPages+", displayImageIndex = " + displayImageIndex);
            if (Utils.isCollectionFilled(indicators) && indicators.size() == numPages) {
                // Indicators already added, no need to add again.
                Logger.d(TAG, "no need to add indicators again");
                return;
            }else{
                carouselIndicatorsHolder.removeAllViews();
                indicators = new ArrayList<>();
                for(int i=0; i < numPages; i++){
                    View indicatorContainer = LayoutInflater.from(getContext()).inflate(R.layout.carousel_indicator,
                            carouselIndicatorsHolder, false);
                    indicators.add((ImageView) indicatorContainer.findViewById(R.id.iv_indicator_circle));
                    if (i == displayImageIndex){
                        indicators.get(i).setSelected(true);
                    }
                    carouselIndicatorsHolder.addView(indicatorContainer);
                }
            }
        }

        private void setSelectedPage(int newPosition){
            int prevIndex = currentPageIndex;
            // Setting newPosition as current page
            currentPageIndex = newPosition;
            if (indicators != null){
                indicators.get(prevIndex).setSelected(false);
                indicators.get(currentPageIndex).setSelected(true);
            }
        }

        public void bindData(LeagueBoard leagueBoard){
            // Sets data for banner
            if (leagueBoard != null){
                bannerPagerAdapter.setData(leagueBoard);
                mParent.enableDisableSwipeRefresh(true);
            }
        }
    }

    public interface Parent {
        void onItemClick(long id);
        int getMyId();
        boolean toShowLogo();

        int toShowBanner();
        LeagueBoard getBannerData();
        void enableDisableSwipeRefresh(boolean enable);
    }
}
