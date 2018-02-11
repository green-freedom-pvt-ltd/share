package com.sharesmile.share.helpcenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.rfac.models.FeedbackNode;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public abstract class BaseFeedbackFragment extends BaseFragment {

    private static final String TAG = "BaseFeedbackFragment";

    public abstract FeedbackNode.Type getFeedbackNodeType();
    public abstract int getFeedbackLevel();
    protected abstract void setupToolbar();
    public abstract String getScreenName();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AnalyticsEvent.create(Event.ON_LOAD_FEEDBACK_SCREEN)
                .put("screen", getScreenName())
                .buildAndDispatch();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupToolbar();
    }
}
