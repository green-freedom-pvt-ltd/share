package com.sharesmile.share.rfac.fragments;

import android.support.annotation.NonNull;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.rfac.models.FeedbackCategory;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class HelpCenterFragment extends BaseFeedbackCategoryFragment {

    private static final String TAG = "HelpCenterFragment";

    @Override
    public int getFeedbackLevel() {
        return FeedbackCategory.LEVEL_1;
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getString(R.string.help_center));
    }

    @NonNull
    @Override
    public List<FeedbackCategory> getCategories() {
        return Constants.HELP_CENTER_CATEGORIES;
    }

    @Override
    public void onItemClick(FeedbackCategory category) {
        // TODO: Load Level_2 or Level_3 FeedbackFragment depending on the category clicked
        getFragmentController().replaceFragment();
    }
}
