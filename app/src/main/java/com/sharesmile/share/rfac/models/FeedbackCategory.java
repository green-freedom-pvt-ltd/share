package com.sharesmile.share.rfac.models;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackCategory extends FeedbackNode {

    // Level_1
    public static final FeedbackCategory PAST_WORKOUT = new FeedbackCategory(LEVEL_1, "pastworkout", "I have an issue with past workout");
    public static final FeedbackCategory QUESTIONS = new FeedbackCategory(LEVEL_1, "question", "I have a question");
    public static final FeedbackCategory FEEDBACK = new FeedbackCategory(LEVEL_1, "feedback", "I have a suggestion");
    public static final FeedbackCategory SOMETHING_ELSE = new FeedbackCategory(LEVEL_1, "else", "My issue isn't listed here");
    public static final FeedbackCategory FLAGGED_RUN_HISTORY = new FeedbackCategory(LEVEL_1, "flag", "Workout flagged in history");
    public static final FeedbackCategory POST_RUN_SAD = new FeedbackCategory(LEVEL_1, "sad", "Not good");
    public static final FeedbackCategory POST_RUN_HAPPY = new FeedbackCategory(LEVEL_1, "happy", "I loved it");


    // Level_2
    public static FeedbackCategory LESS_DISTANCE = new FeedbackCategory(LEVEL_2, "less", "Less distance recorded");
    public static FeedbackCategory MORE_DISTANCE = new FeedbackCategory(LEVEL_2, "more", "More distance recorded");
    public static FeedbackCategory FLAGGED_RUN = new FeedbackCategory(LEVEL_2, "scratched", "Why is it scratched off");
    public static FeedbackCategory NOT_IN_VEHICLE = new FeedbackCategory(LEVEL_2, "notvehicle", "I wasn't in a vehicle");
    public static FeedbackCategory IMPACT_MISSING_LEADERBOARD = new FeedbackCategory(LEVEL_2, "leaderboardadd", "Impact missing in Leaderboard");
    public static FeedbackCategory STILL_SOMETHING_ELSE = new FeedbackCategory(LEVEL_2, "stillelse", "Something else");

    public static FeedbackCategory DISTANCE_NOT_ACCURATE = new FeedbackCategory(LEVEL_2, "notaccurate", "Distance not accurate");
    public static FeedbackCategory WORKOUT_MISSING_HISTORY = new FeedbackCategory(LEVEL_2, "workoutmissing", "Workout missing from history");
    public static FeedbackCategory GPS_ISSUE = new FeedbackCategory(LEVEL_2, "gpsissue", "Issue with GPS");
    public static FeedbackCategory ZERO_DISTANCE = new FeedbackCategory(LEVEL_2, "zerodistance", "Zero distance recorded");

    private String label;

    public FeedbackCategory(int level, String value, String label) {
        super(level, value, Type.CATEGORY);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public FeedbackCategory copy(){
        FeedbackCategory copy = new FeedbackCategory(getLevel(), getValue(), getLabel());
        copy.setParent(getParent());
        return copy;
    }

}
