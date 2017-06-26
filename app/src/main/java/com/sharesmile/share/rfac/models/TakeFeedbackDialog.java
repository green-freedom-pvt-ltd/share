package com.sharesmile.share.rfac.models;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;

import base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sharesmile.share.core.Constants.FEEDBACK_TAG_POST_RUN_SAD;

/**
 * Created by ankitmaheshwari on 6/26/17.
 */

public class TakeFeedbackDialog extends BaseDialog {

    private static final String TAG = "TakeFeedbackDialog";

    @BindView(R.id.et_take_feedback)
    EditText takeFeedbackEditText;

    @BindView(R.id.btn_feedback_submit)
    View submitButton;

    WorkoutData concernedWorkout;

    public TakeFeedbackDialog(Context context, int theme, WorkoutData concernedWorkout){
        super(context, theme);
        this.concernedWorkout = concernedWorkout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_take_feedback);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ButterKnife.bind(this);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @OnClick(R.id.btn_feedback_submit)
    public void onSubmitClick(){
        Logger.d(TAG, "onSubmitClick");
        String inputMessage = takeFeedbackEditText.getText().toString();
        if (TextUtils.isEmpty(inputMessage)){
            MainApplication.showToast(R.string.enter_atleast_50_chars);
        }else {
            pushFeedback(inputMessage);
            dismiss();
        }
    }

    private void pushFeedback(String message){
        UserFeedback feedback = new UserFeedback();
        if (MainApplication.isLogin()){
            feedback.setUserId(MainApplication.getInstance().getUserID());
            feedback.setEmail(MainApplication.getInstance().getUserDetails().getEmail());
            feedback.setPhoneNumber(MainApplication.getInstance().getUserDetails().getPhoneNumber());
        }
        String feedbackText = message;
        StringBuilder stringBuilder = new StringBuilder("Feedback by user:\nTime: " + DateUtil.getCurrentDate() + "\n");
        if (concernedWorkout != null){
            String concernedRunDetails = concernedWorkout.toString();
            stringBuilder.append("Concerned Run:\n" + concernedRunDetails
                    + "\n Feedback Message: " + feedbackText);
            feedback.setClientRunId(concernedWorkout.getWorkoutId());
        }else {
            stringBuilder.append("Feedback Message: " + feedbackText) ;;
        }
        feedback.setMessage(stringBuilder.toString());
        feedback.setTag(FEEDBACK_TAG_POST_RUN_SAD);
        SyncHelper.pushUserFeedback(feedback);
    }
}
