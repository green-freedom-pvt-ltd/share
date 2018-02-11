package com.sharesmile.share.helpcenter.category;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.R;
import com.sharesmile.share.helpcenter.BaseFeedbackFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public abstract class BaseFeedbackCategoryFragment extends BaseFeedbackFragment implements FeedbackCategoryAdapter.ItemClickListener {

    private static final String TAG = "BaseFeedbackCategoryFragment";

    @BindView(R.id.rv_feedback_category)
    public RecyclerView recyclerView;

    LinearLayoutManager mLayoutManager;
    FeedbackCategoryAdapter adapter;

    @NonNull
    public abstract List<FeedbackCategory> getCategories();

    @Override
    public FeedbackCategory.Type getFeedbackNodeType() {
        return FeedbackCategory.Type.CATEGORY;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View l = inflater.inflate(R.layout.frag_feedback_category, null);
        ButterKnife.bind(this, l);
        init();
        return l;
    }

    protected void init(){
        adapter = new FeedbackCategoryAdapter(getCategories(), this);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.leaderboard_list_item_divider));
        recyclerView.addItemDecoration(mDividerItemDecoration);

        setupToolbar();
    }
}
