package com.sharesmile.share.rfac.models;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackCategory extends FeedbackNode {

    // Level_1
    public static final FeedbackCategory PAST_WORKOUT = new FeedbackCategory(LEVEL_1, "pastworkout", "Issue with past workout");
    public static final FeedbackCategory QUESTIONS = new FeedbackCategory(LEVEL_1, "question", "Questions");
    public static final FeedbackCategory FEEDBACK = new FeedbackCategory(LEVEL_1, "feedback", "Feedback/Suggestions");
    public static final FeedbackCategory SOMETHING_ELSE = new FeedbackCategory(LEVEL_1, "else", "Something else");


    // Level_2
    public static final FeedbackCategory LESS_DISTANCE = new FeedbackCategory(LEVEL_2, "less", "Less distance recorded");
    public static final FeedbackCategory MORE_DISTANCE = new FeedbackCategory(LEVEL_2, "more", "More distance recorded");
    public static final FeedbackCategory FLAGGED_RUN = new FeedbackCategory(LEVEL_2, "scratched", "Why is it scratched off");
    public static final FeedbackCategory NOT_IN_VEHICLE = new FeedbackCategory(LEVEL_2, "notvehicle", "I wasn't in a vehicle");
    public static final FeedbackCategory IMPACT_MISSING_LEADERBOARD = new FeedbackCategory(LEVEL_2, "leaderboardadd", "Impact missing in Leaderboard");
    public static final FeedbackCategory STILL_SOMETHING_ELSE = new FeedbackCategory(LEVEL_2, "else", "Something else");

    public static final FeedbackCategory DISTANCE_NOT_ACCURATE = new FeedbackCategory(LEVEL_2, "notaccurate", "Distance not accurate");
    public static final FeedbackCategory WORKOUT_MISSING_HISTORY = new FeedbackCategory(LEVEL_2, "workoutmissing", "Workout missing from history");
    public static final FeedbackCategory GPS_ISSUE = new FeedbackCategory(LEVEL_2, "gpsissue", "Issue with GPS");
    public static final FeedbackCategory ZERO_DISTANCE = new FeedbackCategory(LEVEL_2, "zerodistance", "Zero distance recorded");

    private String label;

    public FeedbackCategory(int level, String value, String label) {
        super(level, value, Type.CATEGORY);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }


}
