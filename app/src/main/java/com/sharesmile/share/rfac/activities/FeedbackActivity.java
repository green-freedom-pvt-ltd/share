package com.sharesmile.share.rfac.activities    ;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.sharesmile.share.R;
import com.sharesmile.share.core.PermissionCallback;
import com.sharesmile.share.core.ToolbarActivity;
import com.sharesmile.share.rfac.FeedbackResolutionFactory;
import com.sharesmile.share.rfac.fragments.FeedbackResolutionFragment;
import com.sharesmile.share.rfac.fragments.HelpCenterFragment;
import com.sharesmile.share.rfac.fragments.PastWorkoutIssueFragment;
import com.sharesmile.share.rfac.models.FeedbackCategory;
import com.sharesmile.share.rfac.models.FeedbackResolution;
import com.sharesmile.share.rfac.models.Run;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 9/4/17.
 */

public class FeedbackActivity extends ToolbarActivity {

    private static final String TAG = "FeedbackActivity";

    public static final String FEEDBACK_CATEGORY_ARG = "feedback_category_arg";
    public static final String FEEDBACK_CONCERNED_RUN_ARG = "feedback_concerned_run_arg";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_feedback);
        if (savedInstanceState == null) {
            loadInitialFragment();
        }
    }

    public void loadInitialFragment() {
        FeedbackCategory feedbackCategory = (FeedbackCategory) getIntent().getSerializableExtra(FEEDBACK_CATEGORY_ARG);
        if (feedbackCategory == null){
            // Open HelpCenter by default
            addFragment(HelpCenterFragment.newInstance(), false);
        }else if (FeedbackCategory.POST_RUN_SAD.equals(feedbackCategory)){
            Run concernedRun = (Run) getIntent().getSerializableExtra(FEEDBACK_CONCERNED_RUN_ARG);
            addFragment(PastWorkoutIssueFragment.newInstance(FeedbackCategory.POST_RUN_SAD.copy(),
                    concernedRun), false);
        }else if (FeedbackCategory.FLAGGED_RUN_HISTORY.equals(feedbackCategory)){
            Run concernedRun = (Run) getIntent().getSerializableExtra(FEEDBACK_CONCERNED_RUN_ARG);
            FeedbackResolution resolution =
                    FeedbackResolutionFactory.getResolutionForCategory(FeedbackCategory.FLAGGED_RUN_HISTORY);
            addFragment(FeedbackResolutionFragment.newInstance(resolution, concernedRun), false);
        }else {
            // Open HelpCenter by default
            addFragment(HelpCenterFragment.newInstance(), false);
        }
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.containerView;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public void requestPermission(int requestCode, PermissionCallback permissionsCallback) {

    }

    @Override
    public void unregisterForPermissionRequest(int requestCode) {

    }

    @Override
    public void updateToolBar(String title, boolean showAsUpEnable) {

    }
}
