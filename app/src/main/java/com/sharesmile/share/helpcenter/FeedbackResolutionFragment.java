package com.sharesmile.share.helpcenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.TextView;

import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.tracking.Run;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.core.Logger;

import butterknife.BindView;

/**
 * Created by ankitmaheshwari on 8/27/17.
 */

public class FeedbackResolutionFragment extends FeedbackLevelThreeFragment {

    private static final String TAG = "FeedbackResolutionFragment";

    @BindView(R.id.tv_feedback_explanation)
    TextView explanationView;

    FeedbackResolution feedbackResolution;
    Run concernedRun;

    public static final String FEEDBACK_RESOLUTION_ARGS = "feedback_resolution";
    public static final String CONCERNED_RUN_ARGS = "concerned_run_args";

    public static FeedbackResolutionFragment newInstance(FeedbackResolution resolution) {
        Bundle args = new Bundle();
        args.putSerializable(FEEDBACK_RESOLUTION_ARGS, resolution);
        FeedbackResolutionFragment fragment = new FeedbackResolutionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static FeedbackResolutionFragment newInstance(FeedbackResolution resolution, Run concernedRun) {
        Bundle args = new Bundle();
        args.putSerializable(FEEDBACK_RESOLUTION_ARGS, resolution);
        args.putSerializable(CONCERNED_RUN_ARGS, concernedRun);
        FeedbackResolutionFragment fragment = new FeedbackResolutionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedbackResolution = (FeedbackResolution) getArguments().getSerializable(FEEDBACK_RESOLUTION_ARGS);
        concernedRun = (Run) getArguments().getSerializable(CONCERNED_RUN_ARGS);
    }

    @Override
    protected void init() {
        super.init();
        explanationView.setText(feedbackResolution.getExplanationText());
    }

    @Override
    public FeedbackNode.Type getFeedbackNodeType() {
        return FeedbackNode.Type.RESOLUTION;
    }

    @Override
    protected void setupToolbar() {
        FeedbackNode parent = feedbackResolution.getParent();
        if (parent != null){
            if (FeedbackCategory.FEEDBACK.equals(parent)){
                // Change title to "Suggestion"
                setToolbarTitle(getString(R.string.suggestion));
                return;
            }
        }
        setToolbarTitle(getString(R.string.help_center));
    }

    @Override
    public String getScreenName() {
        return "Resolution";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_feedback_resolution;
    }

    @Override
    protected String getPromptText() {
        return feedbackResolution.getPromptText();
    }

    @Override
    protected String getHintText() {
        return feedbackResolution.getHintText();
    }

    @Override
    protected boolean validateUserInput(String feedbackText) {
        return true;
    }

    @Override
    protected UserFeedback addToFeedback(UserFeedback.Builder builder) {
        if (concernedRun != null){
            // Need to add run details to feedback
            String message = builder.getMessage();
            if (!TextUtils.isEmpty(message)){
                // We need to append run details to feedback message
                String concernedDetails = concernedRun.extractRelevantInfoAsString();
                String enhancedMessage = message
                        +"\nTime: " + DateUtil.getCurrentDate()
                        +"\nConcerned " + concernedDetails;
                builder.message(enhancedMessage);
            }
            int runId = (int) concernedRun.getId();
            String clientRunId = concernedRun.getClientRunId();
            if (runId == 0){
                WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
                Workout workout = mWorkoutDao.queryBuilder()
                        .where(WorkoutDao.Properties.WorkoutId.eq(clientRunId),
                                WorkoutDao.Properties.Is_sync.eq(true))
                        .unique();
                runId = (workout != null && workout.getId() != null) ? workout.getId().intValue() : 0;
            }
            builder.runId(runId);
            builder.clientRunId(clientRunId);
        }

        FeedbackNode parent = feedbackResolution.getParent();
        if (parent != null){
            if (FeedbackNode.LEVEL_1 == parent.getLevel()){
                builder.tag(parent.getValue());
            }else if (FeedbackNode.LEVEL_2 == parent.getLevel()){
                builder.subTag(parent.getValue());
                if (parent.getParent() != null){
                    builder.tag(parent.getParent().getValue());
                }
            }
        }else {
            Logger.d(TAG, "addToFeedback: FeedbackNode Parent is null");
        }
        return builder.build();
    }
}
