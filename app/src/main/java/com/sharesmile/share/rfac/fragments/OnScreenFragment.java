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

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.ViewPagerTransformer;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.adapters.CausePageAdapter;
import com.sharesmile.share.rfac.models.CauseList;
import com.sharesmile.share.rfac.models.CausesPage;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Urls;
import com.sharesmile.share.views.MRButton;

import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OnScreenFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = OnScreenFragment.class.getSimpleName();
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.btn_lets_run)
    MRButton mRunButton;

    @BindView(R.id.content_view)
    LinearLayout mContentView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private CausePageAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CausePageAdapter(getChildFragmentManager());
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
        if (mAdapter.getCount() <= 0) {
            fetchPageData();
        }
        return view;

    }

    private void fetchPageData() {
        Logger.d(TAG, "Fetching Causes Data");
        showProgressDialog();
        NetworkDataProvider.doGetCallAsync(Urls.getCauseListUrl(), new NetworkAsyncCallback<CauseList>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "onNetworkFailure: Can't fetch events page data: " + ne.getMessageFromServer(), ne);
                MainApplication.showToast("Unable to fetch events");
                hideProgressDialog();
                mRunButton.setVisibility(View.GONE);
                Snackbar.make(mContentView, "No connection", Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchPageData();
                    }
                }).show();
            }

            @Override
            public void onNetworkSuccess(CauseList causesList) {
                Logger.d(TAG, "onNetworkSuccess");
                AddCauseList(causesList);
                hideProgressDialog();
                mRunButton.setVisibility(View.VISIBLE);
            }
        });
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
                getFragmentController().performOperation(IFragmentController.START_RUN, mAdapter.getItemAtPosition(viewPager.getCurrentItem()));
                break;
            default:

        }
    }

}