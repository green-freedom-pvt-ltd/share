package com.sharesmile.share.home.howitworks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.home.howitworks.model.HowItWorksRowItem;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 1/31/18.
 */

public class HowItWorksFragment extends BaseFragment {

    private static final String TAG = "HowItWorksFragment";

    @BindView(R.id.rv_how_it_works)
    RecyclerView recyclerView;

    List<HowItWorksRowItem> rowItems;
    LinearLayoutManager linearLayoutManager;
    HowItWorksAdapter adapter;

    public static HowItWorksFragment newInstance() {
        HowItWorksFragment howItWorksFragment = new HowItWorksFragment();
        return howItWorksFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rowItems = Constants.getDefaultHowItWorksRowItems();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_how_it_works, null);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    private void init(){
        // Step: add a toolbar
        getFragmentController().updateToolBar(getString(R.string.action_how_it_works), true);
        // Step: setup Recyclerview
        adapter = new HowItWorksAdapter(MainApplication.getInstance().getHowItWorksSteps());
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.btn_how_it_works_tell_friends)
    public void onTellYourFriendsClick(){
        Utils.share(getActivity(), getString(R.string.share_msg));
        AnalyticsEvent.create(Event.ON_CLICK_HOW_IT_WORKS_SHARE)
                .buildAndDispatch();
    }
}
