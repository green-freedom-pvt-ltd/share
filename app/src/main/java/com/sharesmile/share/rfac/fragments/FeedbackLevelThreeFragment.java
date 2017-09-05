package com.sharesmile.share.rfac.fragments;

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

import com.google.gson.Gson;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.ExpoBackoffTask;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.rfac.FeedbackChatContainer;
import com.sharesmile.share.rfac.FeedbackInputContainer;
import com.sharesmile.share.rfac.activities.ChatActivity;
import com.sharesmile.share.rfac.activities.LoginActivity;
import com.sharesmile.share.rfac.activities.MainActivity;
import com.sharesmile.share.rfac.models.FeedbackNode;
import com.sharesmile.share.rfac.models.UserFeedback;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

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
            pendingFeedback = constructFeedbackObject(feedbackText);
            if (MainApplication.isLogin()){
                pushFeedbackAndExit(pendingFeedback);
                pendingFeedback = null;
            }else {
                startLoginActivityForResult();
            }
        }
    }

    private void startLoginActivityForResult() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    @Override
    public void onChatClicked(){
        Logger.d(TAG, "onChatClicked");
        // TODO: Fire Feedback Api to get ticketId and then start ChatActivity
        pendingFeedback = constructFeedbackObject(null);
        if (MainApplication.isLogin()){
            pushChatFeedbackNow(pendingFeedback);
            startChat();
            pendingFeedback = null;
        }else {
            startLoginActivityForResult();
        }
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
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startChat(){
//        Conversation conversation = Smooch.getConversation();
//        if (conversation != null){
//            Logger.d(TAG, "onChatClicked, will send Hello World");
//            Map<String, Object> map = new HashMap<>();
//            map.put("name", "Ankit");
//            map.put("age", 29);
//            Message message = new Message("Hello World","My payload",map);
//            conversation.sendMessage(message);
//        }
        ChatActivity.show(getContext());
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
