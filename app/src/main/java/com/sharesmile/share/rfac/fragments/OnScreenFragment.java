package com.sharesmile.share.rfac.fragments;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.sharesmile.share.Cause;
import com.sharesmile.share.CauseDao;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.ViewPagerTransformer;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.adapters.CausePageAdapter;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.CauseList;
import com.sharesmile.share.sync.SyncTaskManger;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.MLButton;
import com.sharesmile.share.views.MRButton;
import com.mixpanel.android.mpmetrics.MixpanelAPI;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import org.json.JSONException;
import org.json.JSONObject;


public class OnScreenFragment extends BaseFragment implements View.OnClickListener {

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
    MixpanelAPI mixpanel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mAdapter = new CausePageAdapter(getChildFragmentManager());
        SyncTaskManger.startCauseSync(getActivity());

    }

    @Override
    public void onStart() {
        super.onStart();

        Logger.d(TAG, "onstart");

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cause, container, false);
        ButterKnife.bind(this, view);
        mRunButton.setOnClickListener(this);
        getFragmentController().updateToolBar(getString(R.string.impactrun), false);
        viewPager.setClipToPadding(false);
        viewPager.setPageTransformer(false, new ViewPagerTransformer());
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.view_pager_page_margin));
        viewPager.setPadding(getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_left), 0, getResources().getDimensionPixelOffset(R.dimen.view_pager_margin_right), 0);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(mAdapter);
        showProgressDialog();
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAdapter.getCount() <= 0) {
            fetchPageData();
        } else {
            hideProgressDialog();
        }
        updateActionbar();
    }

    private void updateActionbar() {
        getFragmentController().updateToolBar(getString(R.string.title_cause), false);
    }

    @Override
    public void onStop() {
        //  EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void fetchPageData() {
        showProgressDialog();
        EventBus.getDefault().post(new DBEvent.CauseFetchDataFromDb());
    }

    private void AddCauseList(CauseList causesList) {
        mAdapter.addData(causesList);
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
                mixpanel = MixpanelAPI.getInstance(getActivity().getBaseContext(), getString(R.string.mixpanel_project_token));
                try {
                    JSONObject props = new JSONObject();
                    props.put("Let's Run", "Clicked");
                    mixpanel.track("OnScreenFragment - onClick called", props);
                } catch (JSONException e) {
                    Logger.e(TAG, "Unable to add properties to JSONObject", e);
                }
                getFragmentController().performOperation(IFragmentController.START_RUN, mAdapter.getItemAtPosition(viewPager.getCurrentItem()));
                break;
            default:

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DBEvent.CauseDataUpdated causeDataUpdated) {
        List<CauseData> causes = new ArrayList<CauseData>();
        CauseList causesList = causeDataUpdated.getCauseList();
        for (CauseData causeData : causesList.getCauses()) {
            if (causeData.isActive()) {
                causes.add(causeData);
            }
        }
        setCausedata(causesList);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(DBEvent.CauseFetchDataFromDb causeFetchDataFromDb) {
        Logger.d(TAG, "causeFetchDataFromDb");
        CauseDao causeDao = MainApplication.getInstance().getDbWrapper().getCauseDao();
        List<Cause> causes = causeDao.queryBuilder().where(CauseDao.Properties.IsActive.eq(true)).list();
        List<CauseData> causeDataList = new ArrayList<>();
        for (Cause cause : causes) {
            causeDataList.add(new CauseData(cause));
        }
        EventBus.getDefault().post(causeDataList);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(List<CauseData> causeDataList) {
        Logger.d(TAG, "causes");
        CauseList causeList = new CauseList();
        causeList.setCauses(causeDataList);
        setCausedata(causeList);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }

    public void setCausedata(CauseList causeList) {
        AddCauseList(causeList);
        hideProgressDialog();
        mRunButton.setVisibility(View.VISIBLE);
        if (mAdapter.getCount() <= 0) {
            mRunButton.setVisibility(View.GONE);
            if (!NetworkUtils.isNetworkConnected(getContext())) {

                Snackbar.make(mContentView, "No connection", Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SyncTaskManger.startCauseSync(getActivity());
                        showProgressDialog();
                    }
                }).show();
            } else {
                showProgressDialog();
            }
        }
    }
}