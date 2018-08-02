package com.sharesmile.share.profile;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sharesmile.share.AchievedTitle;
import com.sharesmile.share.AchievedTitleDao;
import com.sharesmile.share.R;
import com.sharesmile.share.Title;
import com.sharesmile.share.TitleDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.profile.adapter.CharityCauseDetailsAdapter;
import com.sharesmile.share.profile.model.CategoryStats;
import com.sharesmile.share.profile.model.CharityOverview;

import java.util.List;

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        charityOverviewRecyclerview.setLayoutManager(linearLayoutManager);
        int totalStars = 0;
        for(int i=0;i<categoryStats.getCauseStats().size();i++)
        {
            totalStars+=categoryStats.getCauseStats().get(i).getCause_no_of_stars();
        }
        starEarned.setText(totalStars+"");
        CharityCauseDetailsAdapter charityCauseDetailsAdapter = new CharityCauseDetailsAdapter(getContext(),categoryStats);
        charityOverviewRecyclerview.setAdapter(charityCauseDetailsAdapter);
        charityAmountRaised.setText(UnitsManager.formatRupeeToMyCurrency(categoryStats.getCategoryRaised()));

        setTitle();
    }

    private void setTitle() {
        AchievedTitleDao achievedTitleDao = MainApplication.getInstance().getDbWrapper().getAchievedTitleDao();
        List<AchievedTitle> achievedTitles = achievedTitleDao.queryBuilder()
                .where(AchievedTitleDao.Properties.UserId.eq(MainApplication.getInstance().getUserID()),
                        AchievedTitleDao.Properties.CategoryName.eq(categoryStats.getCategoryName())).list();
        if(achievedTitles.size()>0)
        {
            TitleDao titleDao = MainApplication.getInstance().getDbWrapper().getTitleDao();
            List<Title> titles = titleDao.queryBuilder()
                    .where(TitleDao.Properties.TitleId.eq(achievedTitles.get(0).getTitleId())).list();
            titleHeader.setText(titles.get(0).getDescription_1());
            title.setText(achievedTitles.get(0).getTitle());
        }else
        {
            titleHeader.setText(getResources().getString(R.string.charity_overview_title_header_earn_stars_to_get_title));
            title.setText("\"Titles\"");
        }
    }

    private void setupToolbar() {
        setHasOptionsMenu(false);
        setToolbarTitle(categoryStats.getCategoryName());
    }

}
