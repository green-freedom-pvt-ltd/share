package com.sharesmile.share.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;

/**
 * Created by ankitmaheshwari1 on 28/01/16.
 */
public class HomeFragment extends BaseFragment implements View.OnClickListener{

    private static final String TAG = "HomeFragment";
    private View baseView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        baseView = inflater.inflate(R.layout.fragment_home, null);
        populateViews();
        return baseView;
    }

    private void populateViews() {
        Toolbar toolbar = (Toolbar) baseView.findViewById(R.id.toolbar_home);
        toolbar.setTitle(R.string.main_title);
        populateCard(R.id.news_card);
        populateCard(R.id.events_card);
        populateCard(R.id.rfac_card);
    }

    private void populateCard(int cardViewId) {
        CardView cardView = (CardView) baseView.findViewById(cardViewId);
        int titleStringResId = R.string.news_title;
        int subTitleStringResId = R.string.news_sub_title;
        int descStringResId = R.string.news_desc;

        switch (cardViewId) {
            case R.id.news_card:
                titleStringResId = R.string.news_title;
                subTitleStringResId = R.string.news_sub_title;
                descStringResId = R.string.news_desc;
                break;
            case R.id.events_card:
                titleStringResId = R.string.event_title;
                subTitleStringResId = R.string.event_sub_title;
                descStringResId = R.string.event_desc;
                break;
            case R.id.rfac_card:
                titleStringResId = R.string.rfac_title;
                subTitleStringResId = R.string.rfac_sub_title;
                descStringResId = R.string.rfac_desc;
                break;
        }

        ((TextView) cardView.findViewById(R.id.tv_title))
                .setText(getString(titleStringResId));
        ((TextView) cardView.findViewById(R.id.tv_sub_title))
                .setText(getString(subTitleStringResId));
        ((TextView) cardView.findViewById(R.id.tv_desc))
                .setText(getString(descStringResId));
        cardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.news_card:
                // Open NewsActivity
                break;
            case R.id.events_card:
                // Open EventsActivity
                break;
            case R.id.rfac_card:
                // Open RFACActivity
                break;
        }
    }
}
