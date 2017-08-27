package com.sharesmile.share.rfac.fragments;

import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.rfac.models.FeedbackNode;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public abstract class BaseFeedbackFragment extends BaseFragment {

    private static final String TAG = "BaseFeedbackFragment";

    public abstract FeedbackNode.Type getFeedbackNodeType();
    public abstract int getFeedbackLevel();
    protected abstract void setupToolbar();

    @Override
    public void onStart() {
        super.onStart();
        setupToolbar();
    }
}
