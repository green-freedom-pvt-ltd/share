package com.sharesmile.share.rfac.fragments;

import com.sharesmile.share.R;
import com.sharesmile.share.rfac.models.FeedbackNode;

/**
 * Created by ankitmaheshwari on 8/27/17.
 */

public class FeedbackResolutionFragment extends BaseFeedbackFragment {
    @Override
    public FeedbackNode.Type getFeedbackNodeType() {
        return FeedbackNode.Type.RESOLUTION;
    }

    @Override
    public int getFeedbackLevel() {
        return 3;
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getString(R.string.help_center));
    }
}
