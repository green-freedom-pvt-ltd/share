package com.sharesmile.share.rfac.fragments;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.ViewPagerTransformer;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.adapters.CausePageAdapter;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.MLButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OnScreenFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = OnScreenFragment.class.getSimpleName();
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.btn_lets_run)
    MLButton mRunButton;

    @BindView(R.id.content_view)
    LinearLayout mContentView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private CausePageAdapter mAdapter;
    private View badgeIndictor;

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onstart");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new CausePageAdapter(getChildFragmentManager());
        EventBus.getDefault().register(this);
        View view = inflater.inflate(R.layout.fragment_cause, container, false);
        ButterKnife.bind(this, view);
        mRunButton.setOnClickListener(this);
        getFragmentController().updateToolBar(getString(R.string.impactrun), false);
        viewPager.setClipToPadding(false);
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageTransformer(false, new ViewPagerTransformer());
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.view_pager_page_margin));
        viewPager.setPadding(getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_left), 0, getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_right), 0);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(mAdapter);
        showProgressDialog();
        setHasOptionsMenu(true);
        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        MenuItem messageItem = menu.findItem(R.id.item_message);

        RelativeLayout badge = (RelativeLayout) messageItem.getActionView();
        badgeIndictor = badge.findViewById(R.id.badge_indicator);
        boolean hasUnreadMessage = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_UNREAD_MESSAGE, false);
        badgeIndictor.setVisibility(hasUnreadMessage ? View.VISIBLE : View.GONE);

        badge.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated");
        if (mAdapter.getCount() <= 0) {
            fetchPageData();
        } else {
            hideProgressDialog();
        }
        updateActionbar();
        AnalyticsEvent.create(Event.ON_LOAD_CAUSE_SELECTION)
                .buildAndDispatch();
    }

    private void updateActionbar() {
        getFragmentController().updateToolBar(getString(R.string.title_cause), false);
    }

    private void fetchPageData() {
        Logger.d(TAG, "fetchPageData");
        if (MainApplication.getInstance().getCausesToShow().isEmpty()){
            showProgressDialog();
            EventBus.getDefault().post(new TriggerCauseDataSync());
        }else {
            setCausedata(MainApplication.getInstance().getCausesToShow());
        }
    }

    private void addCauses(List<CauseData> causes) {
        Collections.sort(causes, new Comparator<CauseData>() {
            @Override
            public int compare(CauseData lhs, CauseData rhs) {
                return lhs.getOrderPriority() - rhs.getOrderPriority();
            }
        });
        mAdapter.addData(causes);
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
                }else {
                    // If it is not completed then it must be an active on going cause
                    getFragmentController().performOperation(IFragmentController.START_RUN, causeData);
                    AnalyticsEvent.create(Event.ON_CLICK_LETS_GO)
                            .addBundle(causeData.getCauseBundle())
                            .put("cause_index", viewPager.getCurrentItem())
                            .buildAndDispatch();
                }
                break;

            case R.id.badge_layout:
                getFragmentController().performOperation(IFragmentController.SHOW_MESSAGE_CENTER, null);
                break;
            default:

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DBEvent.CauseDataUpdated causeDataUpdated) {
        Logger.d(TAG, "onEvent: CauseDataUpdated");
        if (isAdded()){
            setCausedata(MainApplication.getInstance().getCausesToShow());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(TriggerCauseDataSync triggerSync) {
        Logger.d(TAG, "onEvent: triggerSync");
        SyncService.updateCauseData();
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

    private static class TriggerCauseDataSync {

    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    public void setCausedata(List<CauseData> causes) {
        Logger.d(TAG, "setCausedata");
        addCauses(causes);
        hideProgressDialog();
        mRunButton.setVisibility(View.VISIBLE);
        if (mAdapter.getCount() <= 0) {
            mRunButton.setVisibility(View.GONE);
            if (!NetworkUtils.isNetworkConnected(getContext())) {
                Snackbar.make(mContentView, "No connection", Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( NetworkUtils.isNetworkConnected(getContext()) ){
                            fetchPageData();
                        }
                    }
                }).show();
            } else {
                fetchPageData();
            }
        }
    }
}