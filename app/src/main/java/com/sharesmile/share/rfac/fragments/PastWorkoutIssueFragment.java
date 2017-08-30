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
import com.sharesmile.share.rfac.models.Run;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class PastWorkoutIssueFragment extends BaseFeedbackCategoryFragment {

    private static final String TAG = "PastWorkoutIssueFragment";

    public static final String PARENT_CATEGORY_ARGS = "parent_category_args";
    public static final String CONCERNED_RUN_ARGS = "conceerned_run_args";

    private FeedbackCategory levelOneParent;
    private Run concernedRun;

    public static PastWorkoutIssueFragment newInstance(FeedbackCategory levelOneParent, Run run) {
        Bundle args = new Bundle();
        args.putSerializable(PARENT_CATEGORY_ARGS, levelOneParent);
        args.putSerializable(CONCERNED_RUN_ARGS, run);
        PastWorkoutIssueFragment fragment = new PastWorkoutIssueFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        levelOneParent = (FeedbackCategory) getArguments().getSerializable(PARENT_CATEGORY_ARGS);
        concernedRun = (Run) getArguments().getSerializable(CONCERNED_RUN_ARGS);
    }

    @Override
    public int getFeedbackLevel() {
        return FeedbackNode.LEVEL_2;
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getString(R.string.select_issue));
    }

    @NonNull
    @Override
    public List<FeedbackCategory> getCategories() {
        return Constants.getPastWorkoutCategories();
    }

    @Override
    public void onItemClick(FeedbackCategory selectedCategory) {
        // Step: Set the levelOne parent of this selectedCategory
        selectedCategory.setParent(levelOneParent);
        // Step: Resolve this category to figure out the level three FeedbackResolution node
        FeedbackResolution resolution = FeedbackResolutionFactory.getResolutionForCategory(selectedCategory);
        // Step: Load FeedbackResolution fragment
        getFragmentController().replaceFragment(FeedbackResolutionFragment.newInstance(resolution, concernedRun), true);
    }
}
