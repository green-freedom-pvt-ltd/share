package com.sharesmile.share.helpcenter.levelthree.qna.model;

import com.sharesmile.share.helpcenter.FeedbackNode;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackQna extends FeedbackNode {

    List<Qna> qnaList;

    private String askQuestionText;

    private String hintText;

    public FeedbackQna(List<Qna> qnaList, String askQuestionText, String hintText) {
        super(LEVEL_3, "qna", Type.QNA);
        this.qnaList = qnaList;
        this.askQuestionText = askQuestionText;
        this.hintText = hintText;
    }

    public List<Qna> getQnaList() {
        return qnaList;
    }

    public String getAskQuestionText() {
        return askQuestionText;
    }

    public String getHintText() {
        return hintText;
    }
}
