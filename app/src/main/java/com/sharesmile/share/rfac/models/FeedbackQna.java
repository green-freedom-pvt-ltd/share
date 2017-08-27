package com.sharesmile.share.rfac.models;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackQna extends FeedbackNode {

    List<Faq> faqList;

    private String askQuestionText;

    private String hintText;

    public FeedbackQna(List<Faq> faqList, String askQuestionText, String hintText) {
        super(LEVEL_3, "qna", Type.QNA);
        this.faqList = faqList;
        this.askQuestionText = askQuestionText;
        this.hintText = hintText;
    }

    public List<Faq> getFaqList() {
        return faqList;
    }

    public String getAskQuestionText() {
        return askQuestionText;
    }

    public String getHintText() {
        return hintText;
    }
}
