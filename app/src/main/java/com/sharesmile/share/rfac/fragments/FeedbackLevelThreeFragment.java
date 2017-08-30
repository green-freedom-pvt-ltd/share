package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.rfac.FeedbackChatContainer;
import com.sharesmile.share.rfac.FeedbackInputContainer;
import com.sharesmile.share.rfac.models.FeedbackNode;
import com.sharesmile.share.rfac.models.UserFeedback;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public abstract class FeedbackLevelThreeFragment extends BaseFeedbackFragment
        implements FeedbackInputContainer.SubmitListener, FeedbackChatContainer.ClickListener{

    private static final String TAG = "FeedbackLevelThreeFragment";

    @BindView(R.id.feedback_user_input)
    LinearLayout userInputLayout;

    @BindView(R.id.feedback_chat_layout)
    View chatLayout;

    @BindView(R.id.feedback_progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.feedback_scroll_contaier)
    View scrollContainer;

    FeedbackInputContainer inputContainer;
    FeedbackChatContainer chatContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), null);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    protected abstract int getLayoutId();
    protected abstract String getPromptText();
    protected abstract String getHintText();

    /**
     * Validates the input provided by user, throws error if input is not correct
     * @param feedbackText
     * @return true if input is correct, false otherwise
     */
    protected abstract boolean validateUserInput(String feedbackText);

    /**
     * Adds tag, subTag and concernedRun details if any
     * @param builder
     * @return completed Feedback object
     */
    protected abstract UserFeedback addToFeedback(UserFeedback.Builder builder);

    protected void init(){
        inputContainer = new FeedbackInputContainer(getPromptText(), getHintText(),
                userInputLayout, this);
        chatContainer = new FeedbackChatContainer(chatLayout, this);
        setupToolbar();
    }

    @Override
    public int getFeedbackLevel() {
        return FeedbackNode.LEVEL_3;
    }

    protected void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressDialog() {
        mProgressBar.setVisibility(View.GONE);
    }

    private UserFeedback constructFeedbackObject(String feedbackText){
        UserFeedback.Builder builder = new UserFeedback.Builder();
        if (TextUtils.isEmpty(feedbackText)){
            builder.chat(true);
        }else {
            builder.chat(false);
            builder.message(feedbackText);
        }
        if (MainApplication.isLogin()){
            builder.userId(MainApplication.getInstance().getUserID());
            builder.email(MainApplication.getInstance().getUserDetails().getEmail());
            builder.phoneNumber(MainApplication.getInstance().getUserDetails().getPhoneNumber());
            builder.appVersion(Utils.getAppVersion());
        }
        return addToFeedback(builder);
    }

    @Override
    public void onFeedbackSubmit(String feedbackText) {
        Logger.d(TAG, "onFeedbackSubmit with: " + feedbackText);
        if (validateUserInput(feedbackText)){
            SyncHelper.pushUserFeedback(constructFeedbackObject(feedbackText));
        }
    }

    @Override
    public void onChatClicked(){
        Logger.d(TAG, "onChatClicked");
        // TODO: Fire Feedback Api to get ticketId and then start ChatActivity
        SyncHelper.pushUserFeedback(constructFeedbackObject(null));
    }

}
