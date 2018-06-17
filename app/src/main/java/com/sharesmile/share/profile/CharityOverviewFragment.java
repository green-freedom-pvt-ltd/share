package com.sharesmile.share.profile;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.profile.adapter.CharityCauseDetailsAdapter;
import com.sharesmile.share.profile.model.CategoryStats;
import com.sharesmile.share.profile.model.CharityOverview;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CharityOverviewFragment extends BaseFragment{

    private static final String TAG = "CharityOverviewFragment";
    @BindView(R.id.title_header_tv)
    TextView titleHeader;
    @BindView(R.id.title_tv)
    TextView title;
    @BindView(R.id.tv_charity_amount_raised)
    TextView charityAmountRaised;
    @BindView(R.id.tv_star_earned)
    TextView starEarned;
    @BindView(R.id.charityoverview_recyclerview)
    RecyclerView charityOverviewRecyclerview;
    @BindView(R.id.charity_overview_progressbar)
    ProgressBar charityOverviewProgressbar;

    CategoryStats categoryStats;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_charity_overview, null);
        ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        int position = bundle.getInt("position");
        getActivity().getLoaderManager().initLoader(Constants.LOADER_CHARITY_OVERVIEW, null, new LoaderManager.LoaderCallbacks<CharityOverview>() {
            @Override
            public Loader<CharityOverview> onCreateLoader(int id, Bundle args) {
                charityOverviewProgressbar.setVisibility(View.VISIBLE);
                charityOverviewProgressbar.setVisibility(View.GONE);
                return new CharityOverviewAsyncTaskLoader(getContext());
            }

            @Override
            public void onLoadFinished(Loader<CharityOverview> loader, CharityOverview data) {
                categoryStats = data.getCategoryStats().get(position);
                initUi();
                charityOverviewProgressbar.setVisibility(View.GONE);
                charityOverviewRecyclerview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoaderReset(Loader<CharityOverview> loader) {
                Logger.d(TAG,"onLoaderReset");
            }
        });
    }

    private void initUi() {
        setupToolbar();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        charityOverviewRecyclerview.setLayoutManager(linearLayoutManager);
        CharityCauseDetailsAdapter charityCauseDetailsAdapter = new CharityCauseDetailsAdapter(getContext(),categoryStats);
        charityOverviewRecyclerview.setAdapter(charityCauseDetailsAdapter);
        charityAmountRaised.setText(getResources().getString(R.string.rupee_symbol)+categoryStats.getCategoryRaised()+"");
    }

    private void setupToolbar() {
        setHasOptionsMenu(false);
        setToolbarTitle(categoryStats.getCategoryName());
    }

}
