package com.sharesmile.share.helpcenter.category.levelone;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.helpcenter.category.BaseFeedbackCategoryFragment;
import com.sharesmile.share.helpcenter.category.FeedbackCategory;
import com.sharesmile.share.helpcenter.levelthree.qna.model.FeedbackQna;
import com.sharesmile.share.helpcenter.levelthree.qna.FeedbackQnaFragment;
import com.sharesmile.share.helpcenter.levelthree.resolution.FeedbackResolution;
import com.sharesmile.share.helpcenter.levelthree.resolution.FeedbackResolutionFactory;
import com.sharesmile.share.helpcenter.levelthree.resolution.FeedbackResolutionFragment;
import com.sharesmile.share.helpcenter.category.leveltwo.OtherIssueFragment;
import com.sharesmile.share.profile.history.ProfileHistoryFragment;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class HelpCenterFragment extends BaseFeedbackCategoryFragment {

    private static final String TAG = "HelpCenterFragment";

    public static HelpCenterFragment newInstance() {

        Bundle args = new Bundle();

        HelpCenterFragment fragment = new HelpCenterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getFeedbackLevel() {
        return FeedbackCategory.LEVEL_1;
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getString(R.string.help_center));
    }

    @Override
    public String getScreenName() {
        return "Level1";
    }

    @NonNull
    @Override
    public List<FeedbackCategory> getCategories() {
        return Constants.HELP_CENTER_CATEGORIES;
    }

    @Override
    public void onItemClick(FeedbackCategory category) {
        if (FeedbackCategory.PAST_WORKOUT.equals(category)){
            getFragmentController().replaceFragment(ProfileHistoryFragment.newInstance(true), true);
        }
        else if (FeedbackCategory.QUESTIONS.equals(category)){
            FeedbackQna feedbackQna =  FeedbackResolutionFactory
                    .getQna(MainApplication.getInstance().getFaqsToShow());
            getFragmentController().replaceFragment(FeedbackQnaFragment.newInstance(feedbackQna), true);
        }
        else if (FeedbackCategory.FEEDBACK.equals(category)){
            FeedbackResolution resolution = FeedbackResolutionFactory.getResolutionForCategory(category);
            getFragmentController().replaceFragment(FeedbackResolutionFragment.newInstance(resolution), true);
        }
        else if (FeedbackCategory.SOMETHING_ELSE.equals(category)){
            getFragmentController().replaceFragment(OtherIssueFragment.newInstance(category), true);
        }
        AnalyticsEvent.create(Event.ON_CLICK_FEEDBACK_CATEGORY)
                .put("feedback_category", category.getValue())
                .buildAndDispatch();
    }
}
