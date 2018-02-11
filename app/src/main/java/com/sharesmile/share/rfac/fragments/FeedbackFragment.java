package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.rfac.models.Run;
import com.sharesmile.share.rfac.models.UserFeedback;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.utils.DateUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.Constants.FEEDBACK_TAG_DRAWER;
import static com.sharesmile.share.core.Constants.FEEDBACK_TAG_FLAGGED_RUN;


/**
 * Created by apurvgandhwani on 3/26/2016.
 */
@Deprecated
public class FeedbackFragment extends BaseFragment implements View.OnClickListener {

    public static final String BUNDLE_CONCERNED_RUN = "bundle_concerned_run";

    @BindView(R.id.btn_feedback)
    Button mSubmitButton;

    @BindView(R.id.et_feedback_text)
    EditText mFeedbackText;
    private InputMethodManager inputMethodManager;

    Run concernedRun;

    public static FeedbackFragment newInstance(Run concernedRun) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_CONCERNED_RUN, concernedRun);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null){
            concernedRun = (Run) arg.getSerializable(BUNDLE_CONCERNED_RUN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer_feedback, null);
        ButterKnife.bind(this, view);
        mSubmitButton.setOnClickListener(this);
        getFragmentController().updateToolBar(getString(R.string.feedback), true);
        return view;
    }

    @Override
    public void onClick(View v) {
        //submit feedback to server
        if (TextUtils.isEmpty(mFeedbackText.getText().toString())) {
            return;
        }
        hideKeyboard(mFeedbackText);
        MainApplication.showToast(R.string.feedback_thanks);
        SyncHelper.pushUserFeedback(constructFeedbackObject());
        getFragmentController().replaceFragment(new HomeScreenFragment(), true);
    }

    private UserFeedback constructFeedbackObject(){
        UserFeedback feedback = new UserFeedback();
        if (MainApplication.isLogin()){
            feedback.setUserId(MainApplication.getInstance().getUserID());
            feedback.setEmail(MainApplication.getInstance().getUserDetails().getEmail());
            feedback.setPhoneNumber(MainApplication.getInstance().getUserDetails().getPhoneNumber());
        }
        String feedbackText = mFeedbackText.getText().toString();
        StringBuilder stringBuilder = new StringBuilder();
        if (concernedRun != null){
            String concernedRunDetails = concernedRun.extractRelevantInfoAsString();
            stringBuilder.append( "Feedback Message: " + feedbackText
                    +"\nTime: " + DateUtil.getCurrentDate()
                    +"\nConcerned " + concernedRunDetails) ;
            feedback.setTag(FEEDBACK_TAG_FLAGGED_RUN);
            if (concernedRun.getId() > 0){
                feedback.setRunId((int)concernedRun.getId());
            }
            feedback.setClientRunId(concernedRun.getClientRunId());
        }else {
            stringBuilder.append("Feedback Message: " + feedbackText) ;
            feedback.setTag(FEEDBACK_TAG_DRAWER);
        }
        feedback.setMessage(stringBuilder.toString());
        return feedback;
    }

    protected void hideKeyboard(View view) {
        if (inputMethodManager == null) {
            inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
        }
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
