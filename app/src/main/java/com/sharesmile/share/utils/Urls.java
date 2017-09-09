package com.sharesmile.share.utils;

import android.text.TextUtils;

/**
 * Created by Shine on 01/05/16.
 */
public class Urls {

    private static final String BASE_URL = "http://dev.impactrun.com";

    private static final String CAUSE_LIST_URL = "/api/causesv2.json";
    private static final String RUN_URL = "/api/runs/";

    private static final String RUN_LOCATIONS_URL = "/api/runLocations/";

    private static final String CLIENT_CONFIG_URL = "/api/runconfig/";

    private static final String UPDATE_RUN_URL = "/api/updaterun/";

    private static final String LOGIN_URL = "/api/users/";
    private static final String FEEDBACK_URL = "/api/userFeedback/";
    private static final String FAQ_URL = "/api/faq/";
    private static final String MESSAGE_URL = "/api/messageCenter/";
    private static final String LEADERBOARD_URL = "/api/leaderboardv2/";
    private static final String FRAUDSTERS_URL = "/api/fraudsters/";

    private static final String ABOUT_US_URL = "http://impactrun.com/#/AboutUs";

    private static final String GOOGLE_BASE_URL = "https://www.googleapis.com";
    private static final String GOOGLE_CONVERT_TOKEN_URL = "/oauth2/v4/token";
    private static final String CAMPAIGN_URL = "/api/campaign/";

    private static final String LEAGUE_URL = "/api/employeetoteam/";
    private static final String LEAGUEBOARD_URL = "/api/teamboardv2/";
    private static final String TEAMLEADERBOARD_URL = "/api/teamleaderboardv2/";
    private static final String SERVER_TIME_URL = "/api/servertime/";

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

    public static String getRunLocationsUrl() {
        String url = getBaseUrl() + RUN_LOCATIONS_URL;
        return url;
    }

    public static String getUpdateRunUrl() {
        String url = getBaseUrl() + UPDATE_RUN_URL;
        return url;
    }

    public static String getFraudstersUrl(){
        return getBaseUrl() + FRAUDSTERS_URL;
    }

    public static String getFlaggedRunUrl(boolean fetch_flagged_run) {
        String url = getBaseUrl() + RUN_URL + "?is_flag=" + (fetch_flagged_run ? "true" : "all");
        return url;
    }

    public static String getSyncRunUrl(long clientVersion) {
        String url = getBaseUrl() + RUN_URL + "?client_version=" + clientVersion;
        return url;
    }

    public static String getClientConfigUrl() {
        String url = getBaseUrl() + CLIENT_CONFIG_URL;
        return url;
    }

    public static String getLoginUrl(String email) {
        String url = getBaseUrl() + LOGIN_URL;
        if (!TextUtils.isEmpty(email)){
            url = url + "?" + email;
        }
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

    public static String getLeaderboardUrl(String interval) {
        String url = getBaseUrl() + LEADERBOARD_URL + "?interval=" + interval + "&orderby=amount";
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

    public static String getServerTimeUrl() {
        String url = getBaseUrl() + SERVER_TIME_URL;
        return url;
    }

    public static String getLeagueBoardUrl() {
        return getBaseUrl() + LEAGUEBOARD_URL;
    }

    public static String getTeamLeaderBoardUrl() {
        return getBaseUrl() + TEAMLEADERBOARD_URL;
    }
}
