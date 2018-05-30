package com.sharesmile.share.home.homescreen;

import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.SharedPrefsManager;

/**
 * Created by ankitmaheshwari on 2/1/18.
 */

public enum OnboardingOverlay {

    SWIPE_CAUSE(1, 4000, 0, "pref_did_swipe_cause"),
    LETS_GO(1, 2000, 0, "pref_did_use_lets_go_overlay"),
    DRAWER(3, 3000, 5, "pref_did_use_hamburger"),
    FEED(7, 3000, 5, "pref_did_use_feed"),
    OVERALL_IMAPACT(10, 3000, 5, "pref_did_see_impact_so_far"),
    HELP_CENTER(8, 250, 5, "pref_did_use_help_center"),
    STREAK_COUNT(2, 0, 0, "pref_did_open_profile"),
    MY_STATS(3, 0, 1, "pref_did_see_my_stats");

    private int minLaunchCount;
    private int delayInMillis;
    private long maxWorkoutCount;
    private String didUsePrefKey;

    OnboardingOverlay(int minLaunchCount, int delayInMillis, long maxWorkoutCount, String didUsePrefKey) {
        this.minLaunchCount = minLaunchCount;
        this.delayInMillis = delayInMillis;
        this.maxWorkoutCount = maxWorkoutCount;
        this.didUsePrefKey = didUsePrefKey;
    }

    public int getMinLaunchCount() {
        return minLaunchCount;
    }

    public int getDelayInMillis() {
        return delayInMillis;
    }

    public long getMaxWorkoutCount() {
        return maxWorkoutCount;
    }

    private String getDidUsePrefKey() {
        return didUsePrefKey;
    }

    public String getTitle() {
        switch (this) {
            case SWIPE_CAUSE:
                return MainApplication.getContext().getString(R.string.swipe_cause_onboarding_title);
            case LETS_GO:
                return MainApplication.getContext().getString(R.string.lets_go_onboarding_title);
            case DRAWER:
                return MainApplication.getContext().getString(R.string.drawer_onboarding_title);
            case FEED:
                return MainApplication.getContext().getString(R.string.feed_onboarding_title);
            case OVERALL_IMAPACT:
                return MainApplication.getContext().getString(R.string.overall_impact_onboarding_title);
            case HELP_CENTER:
                return MainApplication.getContext().getString(R.string.help_center_onboarding_title);
            case STREAK_COUNT:
                return MainApplication.getContext().getString(R.string.streak_count_onboarding_title);
            case MY_STATS:
                return MainApplication.getContext().getString(R.string.my_stats_onboarding_title);
            default:
                return null;
        }
    }

    public String getDescription() {
        switch (this) {
            case SWIPE_CAUSE:
                return MainApplication.getContext().getString(R.string.swipe_cause_onboarding_description);
            case LETS_GO:
                return MainApplication.getContext().getString(R.string.lets_go_onboarding_description);
            case DRAWER:
                return MainApplication.getContext().getString(R.string.drawer_onboarding_description);
            case FEED:
                return MainApplication.getContext().getString(R.string.feed_onboarding_description);
            case OVERALL_IMAPACT:
                return MainApplication.getContext().getString(R.string.overall_impact_onboarding_description);
            case HELP_CENTER:
                return MainApplication.getContext().getString(R.string.help_center_onboarding_description);
            case STREAK_COUNT:
                return MainApplication.getContext().getString(R.string.streak_count_onboarding_description);
            case MY_STATS:
                return MainApplication.getContext().getString(R.string.my_stats_onboarding_description);
            default:
                return null;
        }
    }

    public boolean isEligibleForDisplay(int screenLaunchCount, long workoutCount) {
        boolean b = SharedPrefsManager.getInstance().getBoolean(getDidUsePrefKey());
        return !b
                && screenLaunchCount >= getMinLaunchCount()
                && workoutCount <= getMaxWorkoutCount();
    }

    public void registerUseOfOverlay() {
        SharedPrefsManager.getInstance().setBoolean(getDidUsePrefKey(), true);
    }
}
