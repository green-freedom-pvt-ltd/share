package com.sharesmile.share.rfac.fragments;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */


import android.animation.ValueAnimator;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.CauseDataStore;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.R;
import com.sharesmile.share.ViewPagerTransformer;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.core.UnitsManager;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.adapters.CausePageAdapter;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeScreenFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "HomeScreenFragment";
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.btn_lets_run)
    Button mRunButton;

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

    private CausePageAdapter mAdapter;

    private static final long NUMBER_ANIMATION_DURATION = 2500;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new CausePageAdapter(getChildFragmentManager());
        EventBus.getDefault().register(this);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        mRunButton.setOnClickListener(this);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageTransformer(false, new ViewPagerTransformer());
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.view_pager_page_margin));
        viewPager.setPadding(getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_left), 0, getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_right), 0);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mAdapter);

        drawerButton.setOnClickListener(this);
        badge.setOnClickListener(this);

        boolean hasUnreadMessage = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_UNREAD_MESSAGE, false);
        badgeIndictor.setVisibility(hasUnreadMessage ? View.VISIBLE : View.GONE);

        int height = (int) getResources().getDimension(R.dimen.super_large_text);
        Shader textShader=new LinearGradient(0, 0, 0, height, new int[]{0xff04cbfd,0xff33f373},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        overallImpactTextView.getPaint().setShader(textShader);

        showProgressDialog();
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated");
        getFragmentController().hideToolbar();
        AnalyticsEvent.create(Event.ON_LOAD_CAUSE_SELECTION)
                .buildAndDispatch();
        render();
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
        if (render()){
            // Trigger an update call
            CauseDataStore.getInstance().updateCauseData();
        }
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
        if (CauseDataStore.getInstance().getCausesToShow().isEmpty()){
            // Data not fetched in DataStore
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
            showProgressDialog();
            setOverallImpactTextView(CauseDataStore.getInstance().getOverallImpact());
            setCausedata(CauseDataStore.getInstance().getCausesToShow());
            CauseDataStore.getInstance().registerVisibleCauses();
            return true;
        } else if (CauseDataStore.getInstance().isNewUpdateAvailable()){
            // Old Data on display
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
                            .buildAndDispatch();
                }else {
                    // If it is not completed then it must be an active on going cause
                    getFragmentController().performOperation(IFragmentController.START_RUN, causeData);
                    AnalyticsEvent.create(Event.ON_CLICK_LETS_GO)
                            .addBundle(causeData.getCauseBundle())
                            .put("cause_index", viewPager.getCurrentItem())
                            .buildAndDispatch();
                }
                break;

            case R.id.bt_home_feed:
                getFragmentController().performOperation(IFragmentController.SHOW_MESSAGE_CENTER, null);
                AnalyticsEvent.create(Event.ON_CLICK_FEED).buildAndDispatch();
                break;
            case R.id.bt_home_drawer:
                getFragmentController().performOperation(IFragmentController.OPEN_DRAWER, null);
                break;
            default:

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DBEvent.CauseDataUpdated causeDataUpdated) {
        Logger.d(TAG, "onEvent: CauseDataUpdated");
        if (isVisible()){
            render();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        CauseData causeData = mAdapter.getItemAtPosition(position);
        if (causeData.isCompleted()){
            mRunButton.setText(getString(R.string.tell_your_friends));
        }else {
            mRunButton.setText(getString(R.string.let_go));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    public void setCausedata(List<CauseData> causes) {
        Logger.d(TAG, "setCausedata");
        if (causes == null || causes.isEmpty()){
            // No cause data to show
            Snackbar.make(mContentView, getString(R.string.some_error_occurred), Snackbar.LENGTH_INDEFINITE).show();
        }
        Collections.sort(causes, new Comparator<CauseData>() {
            @Override
            public int compare(CauseData lhs, CauseData rhs) {
                return lhs.getOrderPriority() - rhs.getOrderPriority();
            }
        });
        mAdapter.setData(causes);
        mRunButton.setVisibility(View.VISIBLE);
        hideProgressDialog();

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
}