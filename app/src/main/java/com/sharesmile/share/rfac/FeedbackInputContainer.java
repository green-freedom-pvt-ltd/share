package com.sharesmile.share.rfac;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public class FeedbackInputContainer {

    @BindView(R.id.tv_prompt_text)
    TextView promptTextView;

    @BindView(R.id.et_user_input)
    EditText inputEditText;

    @BindView(R.id.bt_feedback_submit)
    Button submitButton;


    private View containerView;
    private String promptText;
    private String hintText;
    private SubmitListener listener;

    public FeedbackInputContainer(String promptText, String hintText, View container, SubmitListener listener) {
        this.promptText = promptText;
        this.hintText = hintText;
        this.containerView = container;
        this.listener = listener;
        ButterKnife.bind(this, containerView);
        init();
    }

    private void init(){
        promptTextView.setText(promptText);
        inputEditText.setHint(hintText);
    }

    public String getUserInput(){
        return inputEditText.getText().toString();
    }

    @OnClick(R.id.bt_feedback_submit)
    public void onFeedbackSubmit(){
        if (TextUtils.isEmpty(getUserInput())){
            MainApplication.showToast(R.string.please_enter_feedback);
        }else {
            listener.onFeedbackSubmit(getUserInput());
        }
    }

    public interface SubmitListener {
        void onFeedbackSubmit(String feedbackText);
    }

}
