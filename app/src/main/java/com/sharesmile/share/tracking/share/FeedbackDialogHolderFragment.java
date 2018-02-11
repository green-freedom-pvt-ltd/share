package com.sharesmile.share.tracking.share;

import android.content.DialogInterface;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.base.IFragmentController;
import com.sharesmile.share.tracking.models.WorkoutData;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
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
                        .put("num_spikes", workoutData.getNumGpsSpikes())
                        .put("bolt_count", workoutData.getUsainBoltCount())
                        .put("num_update_events", workoutData.getNumUpdateEvents())
                        .buildAndDispatch();
                showRateAndShareDialog(workoutData);
            }

            @Override
            public void onSecondaryClick(BaseDialog dialog) {
                // Sad
                dialog.dismiss();
                AnalyticsEvent.create(Event.ON_CLICK_SAD_WORKOUT)
                        .addBundle(workoutData.getWorkoutBundle())
                        .put("num_spikes", workoutData.getNumGpsSpikes())
                        .put("bolt_count", workoutData.getUsainBoltCount())
                        .put("num_update_events", workoutData.getNumUpdateEvents())
                        .buildAndDispatch();
                getFragmentController().performOperation(IFragmentController.TAKE_POST_RUN_SAD_FEEDBACK,
                        Utils.convertWorkoutDataToRun(workoutData));
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
                .put("num_spikes", workoutData.getNumGpsSpikes())
                .put("bolt_count", workoutData.getUsainBoltCount())
                .put("num_update_events", workoutData.getNumUpdateEvents())
                .buildAndDispatch();
    }

    protected void showRateAndShareDialog(final WorkoutData workoutData){
        boolean isRated = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_DID_USER_RATE_APP, false);
        feedbackDialog = new RateAndShareDialog(getActivity(), R.style.BackgroundDimDialog, !isRated);
        feedbackDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.d(TAG, "RateAndShareDialog: onCancel");
                exitFeedback(workoutData);
            }
        });
        feedbackDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Logger.d(TAG, "TakeEmailDialog: onDismiss");
                exitFeedback(workoutData);
            }
        });
        feedbackDialog.setListener(new BaseDialog.Listener() {
            @Override
            public void onPrimaryClick(BaseDialog dialog) {
                // User chose to Rate
                if (isAttachedToActivity()){
                    Utils.redirectToPlayStore(getContext());
                    SharedPrefsManager.getInstance().setBoolean(Constants.PREF_DID_USER_RATE_APP, true);
                    dialog.dismiss();
                    AnalyticsEvent.create(Event.ON_CLICK_RATE_US_AFTER_FEEDBACK)
                            .addBundle(workoutData.getWorkoutBundle())
                            .buildAndDispatch();
                }
            }

            @Override
            public void onSecondaryClick(BaseDialog dialog) {
                // User chose to share
                if (isAttachedToActivity()){
                    Utils.share(getActivity(), getString(R.string.share_msg));
                    dialog.dismiss();
                    AnalyticsEvent.create(Event.ON_CLICK_SHARE_AFTER_FEEDBACK)
                            .addBundle(workoutData.getWorkoutBundle())
                            .buildAndDispatch();
                }
            }
        });
        feedbackDialog.show();
        AnalyticsEvent.create(Event.ON_LOAD_RATE_US_POPUP)
                .addBundle(workoutData.getWorkoutBundle())
                .put("num_spikes", workoutData.getNumGpsSpikes())
                .put("bolt_count", workoutData.getUsainBoltCount())
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
