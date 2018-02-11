package com.sharesmile.share.helpcenter.levelthree;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.base.ExpoBackoffTask;
import com.sharesmile.share.core.sync.SyncService;
import com.sharesmile.share.helpcenter.BaseFeedbackFragment;
import com.sharesmile.share.helpcenter.FeedbackNode;
import com.sharesmile.share.helpcenter.model.UserFeedback;
import com.sharesmile.share.helpcenter.levelthree.chat.FeedbackChatContainer;
import com.sharesmile.share.login.LoginActivity;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.utils.Utils;

import base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.smooch.core.Smooch;
import io.smooch.ui.ConversationActivity;

import static com.sharesmile.share.core.Constants.REQUEST_CODE_LOGIN;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public abstract class FeedbackLevelThreeFragment extends BaseFeedbackFragment
        implements FeedbackInputContainer.SubmitListener, FeedbackChatContainer.ClickListener{

    private static final String TAG = "FeedbackLevelThreeFragment";

    @BindView(R.id.feedback_user_input)
    LinearLayout userInputLayout;

    @BindView(R.id.feedback_chat_layout)
    protected View chatLayout;

    @BindView(R.id.feedback_progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.feedback_scroll_contaier)
    protected View scrollContainer;

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
            if (MainApplication.isLogin()){
                UserDetails userDetails = MainApplication.getInstance().getUserDetails();
                if (TextUtils.isEmpty(userDetails.getEmail())){
                    // Need to ask the user for her email
                    showTakeEmailDialog(feedbackText);
                }else {
                    UserFeedback feedback = constructFeedbackObject(feedbackText);
                    pushFeedbackAndExit(feedback);
                }
            }else {
                pendingFeedback = constructFeedbackObject(feedbackText);
                startLoginActivityForResult();
            }
        }
        AnalyticsEvent.create(Event.ON_CLICK_FEEDBACK_SUBMIT)
                .buildAndDispatch();
    }

    private void showTakeEmailDialog(final String feedbackText){
        BaseDialog emailDialog = new TakeEmailDialog(getActivity(), R.style.BackgroundDimDialog);
        emailDialog.setListener(new BaseDialog.Listener() {
            @Override
            public void onPrimaryClick(BaseDialog dialog) {
                // Submit clicked
                UserFeedback feedback = constructFeedbackObject(feedbackText);
                pushFeedbackAndExit(feedback);
            }

            @Override
            public void onSecondaryClick(BaseDialog dialog) {
                // Will not be clicked
            }
        });
        emailDialog.show();
    }

    private void startLoginActivityForResult() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    @Override
    public void onChatClicked(){
        Logger.d(TAG, "onChatClicked");
        pendingFeedback = constructFeedbackObject(null);
        if (MainApplication.isLogin()){
            pushChatFeedbackNow(pendingFeedback);
            startChat();
            pendingFeedback = null;
        }else {
            startLoginActivityForResult();
        }
        AnalyticsEvent.create(Event.ON_CLICK_FEEDBACK_CHAT)
                .buildAndDispatch();
    }

    private void pushChatFeedbackNow(final UserFeedback chatFeedback){
        ExpoBackoffTask backoffTask = new ExpoBackoffTask() {
            @Override
            public int performtask() {
                Gson gson = new Gson();
                String feedbackJson = gson.toJson(chatFeedback);
                return SyncService.pushUserFeedback(feedbackJson);
            }
        };
        backoffTask.run();
    }

    UserFeedback pendingFeedback;

    private void pushFeedbackAndExit(UserFeedback feedbackObject){
        SyncHelper.pushUserFeedback(feedbackObject);
        MainApplication.showToast(R.string.feedback_submitted_successfully);
        openHomeActivityAndFinish();
        AnalyticsEvent.create(Event.ON_SUBMIT_FEEDBACK)
                .put("tag", feedbackObject.getTag())
                .put("sub_tag", feedbackObject.getSubTag())
                .buildAndDispatch();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d(TAG, "onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK){
            if (isAttachedToActivity() && pendingFeedback != null){
                if (pendingFeedback.isChat()){
                    pushChatFeedbackNow(pendingFeedback);
                    startChat();
                }else {
                    pushFeedbackAndExit(pendingFeedback);
                }
                pendingFeedback = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startChat(){
        String fcmToken = FirebaseInstanceId.getInstance().getToken();
        if (!TextUtils.isEmpty(fcmToken)){
            Logger.d(TAG, "Will send FCM Token to Smooch: " + fcmToken);
            Smooch.setFirebaseCloudMessagingToken(FirebaseInstanceId.getInstance().getToken());
        }
        ConversationActivity.show(getContext());
    }

    private void openHomeActivityAndFinish(){
        if (getActivity() != null){
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Constants.BUNDLE_SHOW_RUN_STATS, true);
            startActivity(intent);
            getActivity().finish();
        }
    }

}
