package com.sharesmile.share.rfac.fragments;

import android.support.annotation.NonNull;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.rfac.models.FeedbackCategory;
import com.sharesmile.share.rfac.models.FeedbackNode;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class PastWorkoutIssueFragment extends BaseFeedbackCategoryFragment {

    private static final String TAG = "PastWorkoutIssueFragment";

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
        return Constants.PAST_WORKOUT_CATEGORIES;
    }

    @Override
    public void onItemClick(FeedbackCategory category) {
        // TODO: Load Level_3 FeedbackFragment depending on the category clicked
        getFragmentController().replaceFragment();
    }
}
