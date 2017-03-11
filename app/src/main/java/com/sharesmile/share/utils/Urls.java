package com.sharesmile.share.utils;

/**
 * Created by Shine on 01/05/16.
 */
public class Urls {

    private static final String BASE_URL = "http://139.59.243.247";

    private static final String CAUSE_LIST_URL = "/api/causes.json";
    private static final String RUN_URL = "/api/runs/";

    private static final String LOGIN_URL = "/api/users/";
    private static final String FEEDBACK_URL = "/api/userFeedback/";
    private static final String FAQ_URL = "/api/faq/";
    private static final String MESSAGE_URL = "/api/messageCenter/";
    private static final String LEADERBOARD_URL = "/api/leaderBoard/";

    private static final String ABOUT_US_URL = "http://impactrun.com/#/AboutUs";

    private static final String GOOGLE_BASE_URL = "https://www.googleapis.com";
    private static final String GOOGLE_CONVERT_TOKEN_URL = "/oauth2/v4/token";
    private static final String CAMPAIGN_URL = "/api/campaign/";

    private static final String LEAGUE_URL = "/api/employeetoteam/";
    private static final String TEAMBOARD_URL = "/api/teamboard/";
    private static final String TEAMLEADERBOARD_URL = "/api/teamleaderboard/";

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getCauseListUrl() {
        String url = getBaseUrl() + CAUSE_LIST_URL;
        return url;
    }

    public static String getRunUrl() {
        String url = getBaseUrl() + RUN_URL;
        return url;
    }

    public static String getFlaggedRunUrl() {
        String url = getBaseUrl() + RUN_URL+"?is_flag=all";
        return url;
    }

    public static String getLoginUrl(String email) {
        String url = getBaseUrl() + LOGIN_URL;
        url = url + "?" + email;
        return url;
    }

    public static String getFeedBackUrl() {
        String url = getBaseUrl() + FEEDBACK_URL;
        return url;
    }

    public static String getUserUrl(int user_id) {
        String url = getBaseUrl() + LOGIN_URL + user_id + "/";
        return url;
    }

    public static String getFaqUrl() {
        String url = getBaseUrl() + FAQ_URL;
        return url;
    }

    public static String getAboutUsUrl() {
        return ABOUT_US_URL;
    }

    public static String getGoogleConvertTokenUrl() {
        String url = GOOGLE_BASE_URL + GOOGLE_CONVERT_TOKEN_URL + "/";
        return url;
    }

    public static String getMessageUrl() {
        String url = getBaseUrl() + MESSAGE_URL;
        return url;
    }

    public static String getLeaderboardUrl() {
        String url = getBaseUrl() + LEADERBOARD_URL;
        return url;
    }

    public static String getCampaignUrl() {
        String url = getBaseUrl() + CAMPAIGN_URL;
        return url;
    }

    public static String getLeagueUrl() {
        String url = getBaseUrl() + LEAGUE_URL;
        return url;
    }

    public static String getTeamBoardUrl() {
        return getBaseUrl() + TEAMBOARD_URL;
    }

    public static String getTeamLeaderBoardUrl() {
        return getBaseUrl() + TEAMLEADERBOARD_URL;
    }
}
