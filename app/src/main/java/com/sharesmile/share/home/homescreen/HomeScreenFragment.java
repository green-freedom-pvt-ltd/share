package com.sharesmile.share.home.homescreen;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */


import android.animation.ValueAnimator;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.base.ExpoBackoffTask;
import com.sharesmile.share.core.base.IFragmentController;
import com.sharesmile.share.core.cause.CauseDataStore;
import com.sharesmile.share.core.cause.model.CauseData;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.refer_program.SomethingIsCookingDialog;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeScreenFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "HomeScreenFragment";
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.btn_lets_run)
    View mRunButton;

    @BindView(R.id.share_code_layout)
    LinearLayout shareCodeLayout;

    @BindView(R.id.tv_lets_run)
    TextView mRunButtonText;

    @BindView(R.id.iv_lets_run)
    View mRunButtonImage;

    @BindView(R.id.content_view)
    LinearLayout mContentView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_impact_so_far)
    TextView overallImpactTextView;

    @BindView(R.id.bt_home_drawer)
    View drawerButton;

    @BindView(R.id.bt_home_feed)
    RelativeLayout badge;

    @BindView(R.id.badge_indicator)
    View badgeIndictor;

    @BindView(R.id.overlay_swipe_to_pick)
    LinearLayout swipeToPickOverlay;

    @BindView(R.id.share_code)
    TextView shareCode;

    private CausePageAdapter mAdapter;

    private static final long NUMBER_ANIMATION_DURATION = 2500;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new CausePageAdapter(getChildFragmentManager());
        EventBus.getDefault().register(this);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        shareCodeLayout.setOnClickListener(this);
        mRunButton.setOnClickListener(this);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageTransformer(false, new ViewPagerTransformer());
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.view_pager_page_margin));
        viewPager.setPadding(getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_left), 0, getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_right), 0);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(mAdapter);

        drawerButton.setOnClickListener(this);
        badge.setOnClickListener(this);

        refreshFeedBadgeIndicator();

        int height = (int) getResources().getDimension(R.dimen.super_large_text);
        Shader textShader=new LinearGradient(0, 0, 0, height, new int[]{0xff04cbfd,0xff33f373},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        overallImpactTextView.getPaint().setShader(textShader);

        swipeToPickOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.d(TAG, "onTouch");
                swipeToPickOverlay.setVisibility(View.GONE);
                OnboardingOverlay.SWIPE_CAUSE.registerUseOfOverlay();
                prepareOnboardingOverlays();
                return false;
            }
        });

        showProgressDialog();
        return view;

    }

    private void refreshFeedBadgeIndicator(){

        // Rolling back to old feed
        boolean hasUnreadMessage = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_UNREAD_MESSAGE, false);
        badgeIndictor.setVisibility(hasUnreadMessage ? View.VISIBLE : View.GONE);

//        boolean newFeedArticleAvailable = SharedPrefsManager.getInstance()
//                .getBoolean(Constants.PREF_NEW_FEED_ARTICLE_AVAILABLE, false);
//        Logger.d(TAG, "Setting feed indicator: " + newFeedArticleAvailable);
//        badgeIndictor.setVisibility(newFeedArticleAvailable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated");
        getFragmentController().hideToolbar();
        render();
        DrawerLayout drawerLayout = (getActivity().findViewById(R.id.drawerLayout));
        if(drawerLayout!=null)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        shareCode.setText(MainApplication.getInstance().getUserDetails().getMyReferCode());
    }



    private void prepareOnboardingOverlays(){

        int screenLaunchCount = getScreenLaunchCount();
        long workoutCount = MainApplication.getInstance().getUsersWorkoutCount();
        Logger.d(TAG, "prepareOnboardingOverlays, screenLaunchCount = " + screenLaunchCount
                + ", workoutCount = " + workoutCount);

        if (OnboardingOverlay.SWIPE_CAUSE.isEligibleForDisplay(screenLaunchCount, workoutCount)){
            MainApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAttachedToActivity()
                            && isResumed()
                            && !getFragmentController().isDrawerVisible()
                            && !CauseDataStore.getInstance().getCausesToShow().isEmpty()){
                        // Show swipe screen overlay
                        swipeToPickOverlay.setVisibility(View.VISIBLE);
                    }
                }
            }, OnboardingOverlay.SWIPE_CAUSE.getDelayInMillis());
        } else {
            checkAndScheduleMaterialTapOverlays(screenLaunchCount, workoutCount);
        }
    }
    private void checkAndScheduleMaterialTapOverlays(int screenLaunchCount, long workoutCount){
        if (OnboardingOverlay.LETS_GO.isEligibleForDisplay(screenLaunchCount, workoutCount)){
            scheduleOverlay(OnboardingOverlay.LETS_GO, mRunButton, true);
        }else if (OnboardingOverlay.DRAWER.isEligibleForDisplay(screenLaunchCount, workoutCount)){
            scheduleOverlay(OnboardingOverlay.DRAWER, drawerButton, false);
        }else if (OnboardingOverlay.FEED.isEligibleForDisplay(screenLaunchCount, workoutCount)){
            scheduleOverlay(OnboardingOverlay.FEED, badge, false);
        }else if (OnboardingOverlay.OVERALL_IMAPACT.isEligibleForDisplay(screenLaunchCount, workoutCount)){
            scheduleOverlay(OnboardingOverlay.OVERALL_IMAPACT, overallImpactTextView, true);
        }
    }

    private ShowOverlayRunnable showOverlayRunnable;

    private void scheduleOverlay(final OnboardingOverlay overlay, final View target,
                                 final boolean isRectangular){
        Logger.d(TAG, "scheduleOverlay: " + overlay.name());
        if (showOverlayRunnable != null){
            showOverlayRunnable.cancel();
        }
        showOverlayRunnable = new ShowOverlayRunnable(this, overlay, target, isRectangular);
        MainApplication.getMainThreadHandler().postDelayed(showOverlayRunnable, overlay.getDelayInMillis());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (showOverlayRunnable != null){
            showOverlayRunnable.cancel();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
        if (render()){
            Logger.d(TAG, "onStart: Triggering updateCauseData because render returned true");
            // Trigger an update call
            CauseDataStore.getInstance().updateCauseData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFeedBadgeIndicator();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Fetches (if required) the cause data and then displays it
     * @return true if an update for fresh data needs to be triggered, false if not
     */
    private boolean render() {
        Logger.d(TAG, "render");
       /* if(!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_GOT_BADGES,false)){
            showProgressDialog();
            SyncHelper.syncBadgesData();
        }else if(!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_GOT_ACHIEVED_BADGES,false))
        {
            showProgressDialog();
            SyncHelper.getAchievedBadged();
        }else if(!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_GOT_ACHIEVED_TITLE,false))
        {
            showProgressDialog();
            SyncHelper.getAchievedTitle();
        }else*/ if (CauseDataStore.getInstance().getCausesToShow().isEmpty()){
            // Data not fetched in DataStore
            Logger.d(TAG, "render: Data not fetched in CauseDataStore");
            showProgressDialog();

            if (!NetworkUtils.isNetworkConnected(getContext())){
                Snackbar.make(mContentView, "No connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( NetworkUtils.isNetworkConnected(getContext()) ){
                            render();
                        }
                    }
                }).show();
            }else {
                CauseDataStore.getInstance().updateCauseData();
            }
            return false;
        } else if (mAdapter.getCount() <= 0){
            // Data not being shown on screen
            Logger.d(TAG, "render: Data not being shown on screen");
            showProgressDialog();
            setOverallImpactTextView(CauseDataStore.getInstance().getOverallImpact());
            setCausedata(CauseDataStore.getInstance().getCausesToShow());
            CauseDataStore.getInstance().registerVisibleCauses();
            return true;
        } else if (CauseDataStore.getInstance().isNewUpdateAvailable()){
            // Old Data on display
            Logger.d(TAG, "render: Old data on display, need to refresh it");
            int lastSeenImpact = CauseDataStore.getInstance().getLastSeenOverallImpact();
            int updatedImpact = CauseDataStore.getInstance().getOverallImpact();
            if (updatedImpact > lastSeenImpact){
                startCountAnimation(lastSeenImpact, updatedImpact);
            }
            setCausedata(CauseDataStore.getInstance().getCausesToShow());
            CauseDataStore.getInstance().registerVisibleCauses();
            return false;
        }else {
            // Don't update UI as there is no update available to display
            // But return true so that the client can invoke a call to fetch fresh data
            return true;
        }
//        return true;
    }

    private void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
        mContentView.setVisibility(View.GONE);
    }

    private void hideProgressDialog() {
        mProgressBar.setVisibility(View.GONE);
        mContentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_lets_run:

                CauseData causeData = mAdapter.getItemAtPosition(viewPager.getCurrentItem());
                if (causeData.isCompleted()){
                    Utils.shareImageWithMessage(getContext(), causeData.getCauseCompletedImage(),
                            causeData.getCauseCompletedShareMessageTemplate());
                    AnalyticsEvent.create(Event.ON_CLICK_CAUSE_COMPLETED_SHARE)
                            .addBundle(causeData.getCauseBundle())
                            .put("cause_index", viewPager.getCurrentItem())
                            .put("cause_id",mAdapter.getItemAtPosition(viewPager.getCurrentItem()).getId())
                            .put("cause_name",mAdapter.getItemAtPosition(viewPager.getCurrentItem()).getTitle())
                            .buildAndDispatch();
                } else {
//                    Utils.checkStreak();
                    // If it is not completed then it must be an active on going cause
                    CauseDataStore.getInstance().registerCauseSelection(causeData);
                    getFragmentController().performOperation(IFragmentController.START_RUN, causeData);
                    OnboardingOverlay.LETS_GO.registerUseOfOverlay();
                    AnalyticsEvent.create(Event.ON_CLICK_LETS_GO)
                            .addBundle(causeData.getCauseBundle())
                            .put("cause_index", viewPager.getCurrentItem())
                            .put("cause_id",mAdapter.getItemAtPosition(viewPager.getCurrentItem()).getId())
                            .put("cause_name",mAdapter.getItemAtPosition(viewPager.getCurrentItem()).getTitle())
                            .buildAndDispatch();
                }
                break;
            case R.id.share_code_layout:
                AssetManager assetManager = getContext().getAssets();
                InputStream istr;
                Bitmap bitmap = null;
                try {
                    istr = assetManager.open("images/share_image_2.jpg");
                    bitmap = BitmapFactory.decodeStream(istr);
                } catch (IOException e) {
                    // handle exception
                }
                Utils.share(getContext(), Utils.getLocalBitmapUri(bitmap, getContext()),
                        getString(R.string.share_msg) + " Use this code : " + MainApplication.getInstance().getUserDetails().getMyReferCode());
                break;
            case R.id.bt_home_feed:
                getFragmentController().performOperation(IFragmentController.SHOW_MESSAGE_CENTER, null);
                OnboardingOverlay.FEED.registerUseOfOverlay();
                AnalyticsEvent.create(Event.ON_CLICK_FEED)
                        .put("source", "feed_icon_home_screen")
                        .buildAndDispatch();
                break;
            case R.id.bt_home_drawer:
                getFragmentController().performOperation(IFragmentController.OPEN_DRAWER, null);
                OnboardingOverlay.DRAWER.registerUseOfOverlay();
                break;

            default:

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.CauseDataUpdated causeDataUpdated) {
        Logger.d(TAG, "onEvent: CauseDataUpdated");
        if (isVisible()) {
            render();
        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.BadgeUpdated badgeUpdated) {
        Logger.d(TAG, "onEvent: BadgeUpdated");
        if (isVisible()){
            checkBadgeData();
        }
    }*/

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        CauseData causeData = mAdapter.getItemAtPosition(position);
        if (causeData.getId() != -1) {
            mRunButton.setVisibility(View.VISIBLE);
            shareCodeLayout.setVisibility(View.GONE);
            setLetsRunButton(causeData.isCompleted());
            OnboardingOverlay.SWIPE_CAUSE.registerUseOfOverlay();
        } else {
            mRunButton.setVisibility(View.GONE);
            shareCodeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setLetsRunButton(boolean isCauseCompleted){
        if (isCauseCompleted){
            mRunButtonText.setText(getString(R.string.tell_your_friends));
            mRunButtonImage.setVisibility(View.GONE);
        }else {
            mRunButtonText.setText(getString(R.string.let_go));
            mRunButtonImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    public void setCausedata(List<CauseData> causes) {
        Logger.d(TAG, "setCausedata: number of cause cards = " + causes.size());
        if (causes == null || causes.isEmpty()){
            // No cause data to show
            Snackbar.make(mContentView, getString(R.string.some_error_occurred), Snackbar.LENGTH_INDEFINITE).show();
        }
        CauseDataStore.getInstance().sortCauses(causes);
        boolean b = false;
        for (int i = 0; i < causes.size(); i++) {
            if (causes.get(i).isCompleted()) {
                CauseData causeData = new CauseData();
                causeData.setId(-1);
                causes.add(i, causeData);
                b = true;
                break;
            }
        }
        if (!b) {
            CauseData causeData = new CauseData();
            causeData.setId(-1);
            causes.add(causeData);
        }

        mAdapter.setData(causes);
        setLetsRunButton(causes.get(viewPager.getCurrentItem()).isCompleted());
        mRunButton.setVisibility(View.VISIBLE);
        AnalyticsEvent.create(Event.ON_LOAD_CAUSE_SCREEN).buildAndDispatch();
        hideProgressDialog();
        prepareOnboardingOverlays();
        Utils.checkBadgeData(false);
        showSMCMatchDialog();
    }

    private void showSMCMatchDialog() {
        if (SharedPrefsManager.getInstance().getBoolean(Constants.PREF_SHOW_SMC_MATCH_DIALOG, false)) {
            SomethingIsCookingDialog somethingIsCookingDialog = new SomethingIsCookingDialog(getContext(), Constants.USER_NEW);
            somethingIsCookingDialog.show();
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_SMC_MATCH_DIALOG, false);
        }
    }

    public CauseData getCurrentCause(){
        if (viewPager != null){
            return mAdapter.getItemAtPosition(viewPager.getCurrentItem());
        }
        return null;
    }

    private void setOverallImpactTextView(int overallImpact){
        overallImpactTextView.setText(UnitsManager.formatRupeeToMyCurrency(overallImpact));
    }

    private void startCountAnimation(int from, int to) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(NUMBER_ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                setOverallImpactTextView((int)animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onEvent(UpdateEvent.OnGetStreak onGetStreak)
    {
        if(onGetStreak.result == ExpoBackoffTask.RESULT_SUCCESS)
        {
            render();
        }else
        {
//            showHideProgress(false,null);
            MainApplication.showToast(getResources().getString(R.string.some_error));
        }
    }

}