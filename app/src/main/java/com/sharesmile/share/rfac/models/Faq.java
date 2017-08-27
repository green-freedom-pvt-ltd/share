package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class Faq {

    @SerializedName("question")
    private String question;

    @SerializedName("answer")
    private String answer;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
