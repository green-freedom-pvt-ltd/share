package com.sharesmile.share.helpcenter;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class Qna implements UnObfuscable, Serializable{

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
