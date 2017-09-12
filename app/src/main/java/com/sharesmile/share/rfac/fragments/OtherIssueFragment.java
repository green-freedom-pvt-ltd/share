package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.rfac.FeedbackResolutionFactory;
import com.sharesmile.share.rfac.models.FeedbackCategory;
import com.sharesmile.share.rfac.models.FeedbackNode;
import com.sharesmile.share.rfac.models.FeedbackResolution;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/27/17.
 */

public class OtherIssueFragment extends BaseFeedbackCategoryFragment {

    private static final String TAG = "OtherIssueFragment";

    public static final String PARENT_CATEGORY_ARGS = "parent_category_args";

    private FeedbackCategory levelOneParent;

    public static OtherIssueFragment newInstance(FeedbackCategory levelOneParent) {
        Bundle args = new Bundle();
        args.putSerializable(PARENT_CATEGORY_ARGS, levelOneParent);
        OtherIssueFragment fragment = new OtherIssueFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        levelOneParent = (FeedbackCategory) getArguments().getSerializable(PARENT_CATEGORY_ARGS);
    }

    @Override
    public int getFeedbackLevel() {
        return FeedbackNode.LEVEL_2;
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getString(R.string.help_center));
    }

    @NonNull
    @Override
    public List<FeedbackCategory> getCategories() {
        return Constants.getOtherLevelTwoCategories();
    }

    @Override
    public void onItemClick(FeedbackCategory selectedCategory) {
        // Step: Set the levelOne parent of this selectedCategory
        selectedCategory.setParent(levelOneParent);
        // Step: Resolve this category to figure out the level three FeedbackResolution node
        FeedbackResolution resolution = FeedbackResolutionFactory.getResolutionForCategory(selectedCategory);
        // Step: Load FeedbackResolution fragment
        getFragmentController().replaceFragment(FeedbackResolutionFragment.newInstance(resolution), true);
    }
}
