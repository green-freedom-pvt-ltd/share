package com.sharesmile.share.rfac.models;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackResolution extends FeedbackNode {

    private String descriptionText;

    private String promptText;

    private String hintText;

    public FeedbackResolution(String descriptionText, String promptText, String hintText) {
        super(LEVEL_3, "resolution", Type.RESOLUTION);
        this.descriptionText = descriptionText;
        this.promptText = promptText;
        this.hintText = hintText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public String getPromptText() {
        return promptText;
    }

    public String getHintText() {
        return hintText;
    }
}
