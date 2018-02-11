package com.sharesmile.share.helpcenter.levelthree.qna;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.helpcenter.FeedbackNode;
import com.sharesmile.share.helpcenter.model.UserFeedback;
import com.sharesmile.share.helpcenter.category.FeedbackCategory;
import com.sharesmile.share.helpcenter.levelthree.FeedbackLevelThreeFragment;
import com.sharesmile.share.helpcenter.levelthree.qna.model.FeedbackQna;
import com.sharesmile.share.helpcenter.levelthree.qna.model.Qna;

import java.util.List;

import butterknife.BindView;

/**
 * Created by ankitmaheshwari on 8/27/17.
 */

public class FeedbackQnaFragment extends FeedbackLevelThreeFragment {

    private static final String TAG = "FeedbackQnaFragment";

    @BindView(R.id.rv_feedback_qna)
    RecyclerView recyclerView;

    LinearLayoutManager layoutManager;

    FeedbackQna feedbackQna;

    private FeedbackQnaAdapter qnaAdapter;

    public static final String FEEDBACK_QNA_ARGS = "feedback_qna_args";

    public static FeedbackQnaFragment newInstance(FeedbackQna feedbackQna) {
        Bundle args = new Bundle();
        args.putSerializable(FEEDBACK_QNA_ARGS, feedbackQna);
        FeedbackQnaFragment fragment = new FeedbackQnaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedbackQna = (FeedbackQna) getArguments().getSerializable(FEEDBACK_QNA_ARGS);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_feedback_qna;
    }

    @Override
    protected String getPromptText() {
        return feedbackQna.getAskQuestionText();
    }

    @Override
    protected String getHintText() {
        return feedbackQna.getHintText();
    }

    @Override
    protected boolean validateUserInput(String feedbackText) {
        if (TextUtils.isEmpty(feedbackText) || feedbackText.length() < 10){
            MainApplication.showToast(R.string.enter_atleast_10_chars);
            return false;
        }else {
            return true;
        }
    }

    @Override
    protected UserFeedback addToFeedback(UserFeedback.Builder builder) {
        // Hardcoding tag as questions
        builder.tag(FeedbackCategory.QUESTIONS.getValue());
        return builder.build();
    }

    @Override
    protected void init(){
        super.init();
        // Step: setup Recyclerview
        qnaAdapter = new FeedbackQnaAdapter();
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(qnaAdapter);
        loadQna();

    }

    private void loadQna(){
        if (feedbackQna.getQnaList() == null || feedbackQna.getQnaList().isEmpty()){
            showProgressDialog();
            scrollContainer.setVisibility(View.INVISIBLE);
            chatLayout.setVisibility(View.INVISIBLE);
            QnaProvider provider = new FaqProvider();
            provider.fetchQnas(new QnaProvider.QnaLoadedCallback() {
                @Override
                public void onLoadSuccess(List<Qna> qnas) {
                    renderQnas(qnas);
                    hideProgressDialog();
                    scrollContainer.setVisibility(View.VISIBLE);
                    chatLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadFailure() {
                    renderQnas(null);
                    hideProgressDialog();
                    scrollContainer.setVisibility(View.VISIBLE);
                    chatLayout.setVisibility(View.VISIBLE);
                }
            });
        }else {
            renderQnas(feedbackQna.getQnaList());
        }
    }

    private void renderQnas(List<Qna> qnalist){
        Logger.d(TAG, "renderQnas");
        if (qnalist == null || qnalist.isEmpty()){
            // hide recycler view
            Logger.d(TAG, "renderQnas, hiding recyclerview");
            recyclerView.setVisibility(View.GONE);
        }else {
            Logger.d(TAG, "renderQnas, showing recyclerview");
            recyclerView.setVisibility(View.VISIBLE);
            qnaAdapter.setData(qnalist);
        }
    }

    @Override
    public FeedbackNode.Type getFeedbackNodeType() {
        return FeedbackNode.Type.QNA;
    }

    @Override
    protected void setupToolbar() {
        setToolbarTitle(getString(R.string.questions));
    }

    @Override
    public String getScreenName() {
        return "Questions";
    }


}
