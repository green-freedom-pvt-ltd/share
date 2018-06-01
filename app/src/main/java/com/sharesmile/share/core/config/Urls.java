package com.sharesmile.share.core.config;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by Shine on 01/05/16.
 */
public class Urls {

    private static final String BASE_URL = "http://dev.impactrun.com";
    private static final String NODE_BASE_URL = "http://139.59.243.245:8000";
    private static final String APP_VERSION = "/app/v0";
    private static final String CAUSE_LIST_URL = "/causes";
    private static final String RUN_URL = "/runs";

    private static final String RUN_LOCATIONS_URL = "/runLocations";

    private static final String CLIENT_CONFIG_URL = "/runconfig";

    private static final String UPDATE_RUN_URL = "/updaterun/";

    private static final String LOGIN_URL = "/users/";
    private static final String FEEDBACK_URL = "/userFeedback/";
    private static final String FAQ_URL = "/faq";
    private static final String MESSAGE_URL = "/messageCenter";
    private static final String LEADERBOARD_URL = "/leaderboard/";
    private static final String FRAUDSTERS_URL = "/fraudsters/";

    private static final String ABOUT_US_URL = "http://impactrun.com/#/AboutUs";

    private static final String GOOGLE_BASE_URL = "https://www.googleapis.com";
    private static final String GOOGLE_CONVERT_TOKEN_URL = "/oauth2/v4/token";
    private static final String CAMPAIGN_URL = "/api/campaign/";

    private static final String LEAGUE_REGISTRATION_URL = "/employeetoteam/";
    private static final String LEAGUEBOARD_URL = "/teamboard/";
    private static final String TEAMLEADERBOARD_URL = "/teamleaderboard/";
    private static final String SERVER_TIME_URL = "/servertime/";

    private static final String BLOG_BASE_URL = "http://blog.impactapp.in";
    private static final String BLOG_LATEST_ARTICLE_URL = "/articles/latest";
    private static final String BLOG_HOW_IT_WORKS_CONTENT_URL = "/articles/howitworks";

    private static final String IMPACT_ASSETS_S3_BUCKET_URL = "https://s3.ap-south-1.amazonaws.com/impact-image-assets";

    private static final String HOW_IT_WORKS_S3_FOLDER = "/howitworks/";




    public static String getBaseUrl() {
        return BASE_URL;
    }
    public static String getNodeBaseUrl() {
        return NODE_BASE_URL + APP_VERSION;
    }

    public static String getCauseListUrl() {
        String url = getNodeBaseUrl() +CAUSE_LIST_URL;
        return url;
    }

    public static String getRunUrl() {
        String url = getNodeBaseUrl() + RUN_URL;
        return url;
    }

    public static String getRunLocationsUrl() {
        String url = getNodeBaseUrl() + RUN_LOCATIONS_URL;
        return url;
    }

    public static String getUpdateRunUrl() {
        String url = getNodeBaseUrl() + UPDATE_RUN_URL;
        return url;
    }

    public static String getFraudstersUrl(){
        return getNodeBaseUrl() + FRAUDSTERS_URL;
    }

    public static String getFlaggedRunUrl(boolean fetch_flagged_run) {
        String url = getNodeBaseUrl() + RUN_URL + "?is_flag=" + (fetch_flagged_run ? "true" : "all");
        return url;
    }

    public static String getSyncRunUrl(long clientVersion) {
        String url = getNodeBaseUrl() + RUN_URL + "?client_version=" + clientVersion;
        return url;
    }

    public static String getClientConfigUrl() {
        String url = getNodeBaseUrl() + CLIENT_CONFIG_URL;
        return url;
    }

    public static String getLoginUrl() {
        String url = getNodeBaseUrl() + LOGIN_URL;
        return url;
    }

    public static String getFeedBackUrl() {
        String url = getNodeBaseUrl() + FEEDBACK_URL;
        return url;
    }

    public static String getUserUrl(int user_id) {
        String url = getNodeBaseUrl() + LOGIN_URL + user_id + "/";
        return url;
    }

    public static String getFaqUrl() {
        String url = getNodeBaseUrl() + FAQ_URL;
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
        String url = getNodeBaseUrl() + MESSAGE_URL;
        return url;
    }

    public static String getLeaderboardUrl(String interval) {
        String url = getNodeBaseUrl() + LEADERBOARD_URL + "?interval=" + interval + "&orderby=amount";
        return url;
    }

    public static String getCampaignUrl() {
        String url = getBaseUrl() + CAMPAIGN_URL;
        return url;
    }

    public static String getLeagueRegistrationUrl() {
        String url = getNodeBaseUrl() + LEAGUE_REGISTRATION_URL;
        return url;
    }

    public static String getServerTimeUrl() {
        String url = getNodeBaseUrl() + SERVER_TIME_URL;
        return url;
    }

    public static String getLeagueBoardUrl() {
        return getNodeBaseUrl() + LEAGUEBOARD_URL;
    }

    public static String getTeamLeaderBoardUrl() {
        return getNodeBaseUrl() + TEAMLEADERBOARD_URL;
    }

    public static String getFeedLatestArticleUrl(){
        return BLOG_BASE_URL + BLOG_LATEST_ARTICLE_URL;
    }

    public static String getFeedUrl(){
        return BLOG_BASE_URL;
    }

    public static boolean isFeedArticlesListUrl(String url){
        if (TextUtils.isEmpty(url)){
            return false;
        }
        final Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (Uri.parse(BLOG_BASE_URL).getHost().equals(host)){
            String lastpathSegment = uri.getLastPathSegment();
            return TextUtils.isEmpty(lastpathSegment) || "articles".equals(lastpathSegment);
        }
        return false;
    }

    public static boolean isFeedArticleDetailUrl(String url){
        if (TextUtils.isEmpty(url)){
            return false;
        }
        final Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (Uri.parse(BLOG_BASE_URL).getHost().equals(host)){
            List<String> pathSegments = uri.getPathSegments();
            int size = pathSegments.size();
            if (size > 1){
                return "articles".equals(pathSegments.get(size-2));
            }
        }
        return false;
    }

    public static String getHowItWorksIllustration(int index){
        return IMPACT_ASSETS_S3_BUCKET_URL + HOW_IT_WORKS_S3_FOLDER + index + ".png";
    }

    public static String getHowItWorksContentUrl(){
        return BLOG_BASE_URL + BLOG_HOW_IT_WORKS_CONTENT_URL;
    }
}
