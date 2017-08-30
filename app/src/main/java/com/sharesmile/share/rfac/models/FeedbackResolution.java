package com.sharesmile.share.rfac.models;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackResolution extends FeedbackNode {

    private String explanationText;

    private String promptText;

    private String hintText;

    public FeedbackResolution(String descriptionText, String promptText, String hintText) {
        super(LEVEL_3, "resolution", Type.RESOLUTION);
        this.explanationText = descriptionText;
        this.promptText = promptText;
        this.hintText = hintText;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public String getPromptText() {
        return promptText;
    }

    public String getHintText() {
        return hintText;
    }
}
