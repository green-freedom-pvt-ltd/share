package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.rfac.adapters.LeaderBoardAdapter;
import com.sharesmile.share.rfac.adapters.LeagueBoardBannerPagerAdapter;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import Models.LeagueBoard;
import butterknife.BindView;

/**
 * Created by ankitmaheshwari on 8/5/17.
 */

public class LeagueBoardFragment extends BaseLeaderBoardFragment implements LeaderBoardAdapter.ItemClickListener {

    private static final String TAG = "LeagueBoardFragment";

    LeagueBoardBannerPagerAdapter bannerPagerAdapter;

    @BindView(R.id.banner_view_pager)
    ViewPager bannerViewPager;

    @BindView(R.id.container_banner)
    View bannerContainer;

    @BindView(R.id.banner_carousel_indicator_holder)
    LinearLayout carouselIndicatorsHolder;

    public static LeagueBoardFragment getInstance() {
        LeagueBoardFragment fragment = new LeagueBoardFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Override
    protected void init() {
        initBanner();
        super.init();
        mLeaderBoardAdapter.setItemClickListener(this);
    }

    @Override
    protected void refreshItems() {
        super.refreshItems();
        bannerContainer.setVisibility(View.GONE);
        LeaderBoardDataStore.getInstance().updateLeagueBoardData();
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(LeaderBoardDataStore.getInstance().getLeagueName());
    }

    private int currentPageIndex;
    private List<ImageView> indicators = new ArrayList<>();

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
                enableDisableSwipeRefresh( state == ViewPager.SCROLL_STATE_IDLE );
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

    @Override
    protected void fetchData() {
        if (LeaderBoardDataStore.getInstance().getLeagueBoard() != null){
            showLeagueBoardData(LeaderBoardDataStore.getInstance().getLeagueBoard());
        }else {
            LeaderBoardDataStore.getInstance().updateLeagueBoardData();
            showProgressDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LeagueBoardDataUpdated event){
        if (isAttachedToActivity()){
            hideProgressDialog();
            LeagueBoard board = LeaderBoardDataStore.getInstance().getLeagueBoard();
            if (event.isSuccess()){
                showLeagueBoardData(board);
            }else {
                if (board != null){
                    showLeagueBoardData(board);
                    MainApplication.showToast("Network Error, Couldn't refresh");
                }else {
                    MainApplication.showToast("Network Error, Please try again");
                }
            }
        }
    }

    private void showLeagueBoardData(LeagueBoard board){
        Logger.d(TAG, "showLeagueBoardData");
        mleaderBoardList.clear();
        String leagueName = board.getLeagueName();
        for (LeagueBoard.Team team : board.getTeamList()) {
            mleaderBoardList.add(team.convertToLeaderBoard());
        }
        setBanner(board);
        hideProgressDialog();
        if (!TextUtils.isEmpty(leagueName)){
            setToolbarTitle(leagueName);
        }
        mLeaderBoardAdapter.setData(mleaderBoardList);
    }

    private void setBanner(LeagueBoard board) {
        bannerPagerAdapter.setData(board);
        bannerContainer.setVisibility(View.VISIBLE);
        enableDisableSwipeRefresh(true);
    }

    @Override
    public void onItemClick(long id) {
        getFragmentController().replaceFragment(TeamLeaderBoardFragment.getInstance((int) id), true);
        int myTeamId = LeaderBoardDataStore.getInstance().getMyTeamId();
        if (myTeamId == id){
            AnalyticsEvent.create(Event.ON_CLICK_SELF_TEAM_LEAGUE_BOARD)
                    .put("team_id", id)
                    .put("team_name", LeaderBoardDataStore.getInstance().getMyTeamName())
                    .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                    .buildAndDispatch();
        } else {
            AnalyticsEvent.create(Event.ON_CLICK_OTHER_TEAM_LEAGUE_BOARD)
                    .put("team_id", id)
                    .put("team_name", LeaderBoardDataStore.getInstance().getTeamName((int) id))
                    .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                    .buildAndDispatch();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        indicators = null;
    }

    @Override
    public BOARD_TYPE getBoardType() {
        return BOARD_TYPE.LEAGUEBOARD;
    }

}
