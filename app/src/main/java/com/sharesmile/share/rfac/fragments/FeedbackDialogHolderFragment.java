package com.sharesmile.share.rfac.fragments;

import android.content.DialogInterface;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.PostRunFeedbackDialog;
import com.sharesmile.share.rfac.TakeFeedbackDialog;
import com.sharesmile.share.rfac.models.RateUsDialog;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import base.BaseDialog;

/**
 * Created by ankitmaheshwari on 6/28/17.
 */

public abstract class FeedbackDialogHolderFragment extends BaseFragment {

    private static final String TAG = "FeedbackDialogHolderFragment";

    protected abstract void exitFeedback(WorkoutData workoutData);

    BaseDialog feedbackDialog;

    protected void showPostRunFeedbackDialog(final WorkoutData workoutData){
        if (feedbackDialog != null){
            feedbackDialog.dismiss();
        }
        feedbackDialog = new PostRunFeedbackDialog(getActivity(), R.style.BackgroundDimDialog);
        feedbackDialog.setListener(new BaseDialog.Listener() {
            @Override
            public void onPrimaryClick(BaseDialog dialog) {
                // Happy
                dialog.dismiss();
                AnalyticsEvent.create(Event.ON_CLICK_HAPPY_WORKOUT)
                        .addBundle(workoutData.getWorkoutBundle())
                        .buildAndDispatch();
                showRateUsDialog(workoutData);
            }

            @Override
            public void onSecondaryClick(BaseDialog dialog) {
                // Sad
                dialog.dismiss();
                AnalyticsEvent.create(Event.ON_CLICK_SAD_WORKOUT)
                        .addBundle(workoutData.getWorkoutBundle())
                        .buildAndDispatch();
                showTakeFeedbackDialog(workoutData);
            }
        });
        feedbackDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.d(TAG, "PostRunFeedbackDialog: onCancel");
                // User cancelled the dialog without acting on it, lets just trigger exit
                exitFeedback(workoutData);
            }
        });
        feedbackDialog.show();
        AnalyticsEvent.create(Event.ON_LOAD_HAPPY_SAD_POPUP)
                .addBundle(workoutData.getWorkoutBundle())
                .buildAndDispatch();
    }

    protected void showRateUsDialog(final WorkoutData workoutData){
        feedbackDialog = new RateUsDialog(getActivity(), R.style.BackgroundDimDialog);
        feedbackDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.d(TAG, "TakeFeedbackDialog: onCancel");
                // User cancelled the dialog without acting on it, lets just trigger exit
                exitFeedback(workoutData);
            }
        });
        feedbackDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Logger.d(TAG, "TakeFeedbackDialog: onDismiss");
                // Dialog dismissed explicitly
                exitFeedback(workoutData);
            }
        });
        feedbackDialog.setListener(new BaseDialog.Listener() {
            @Override
            public void onPrimaryClick(BaseDialog dialog) {
                Utils.redirectToPlayStore(getContext());
                dialog.dismiss();
                AnalyticsEvent.create(Event.ON_CLICK_RATE_US)
                        .addBundle(workoutData.getWorkoutBundle())
                        .buildAndDispatch();
            }

            @Override
            public void onSecondaryClick(BaseDialog dialog) {
                // Will never be called
            }
        });
        feedbackDialog.show();
        AnalyticsEvent.create(Event.ON_LOAD_RATE_US_POPUP)
                .addBundle(workoutData.getWorkoutBundle())
                .buildAndDispatch();
    }

    protected void showTakeFeedbackDialog(final WorkoutData workoutData){
        feedbackDialog = new TakeFeedbackDialog(getActivity(), R.style.BackgroundDimDialog, workoutData);
        feedbackDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.d(TAG, "TakeFeedbackDialog: onCancel");
                // User cancelled the dialog without acting on it, lets just trigger exit
                exitFeedback(workoutData);
            }
        });
        feedbackDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Logger.d(TAG, "TakeFeedbackDialog: onDismiss");
                // Dialog dismissed explicitly
                exitFeedback(workoutData);
            }
        });
        feedbackDialog.show();
        AnalyticsEvent.create(Event.ON_LOAD_TAKE_FEEDBACK_POPUP)
                .addBundle(workoutData.getWorkoutBundle())
                .buildAndDispatch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (feedbackDialog != null) {
            feedbackDialog.dismiss();
        }
    }

}
