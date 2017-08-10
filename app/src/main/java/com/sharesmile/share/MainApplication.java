package com.sharesmile.share;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.ClientConfig;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.DbWrapper;
import com.sharesmile.share.core.NotificationActionReceiver;
import com.sharesmile.share.gps.GoogleLocationTracker;
import com.sharesmile.share.gps.WorkoutService;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.activityrecognition.ActivityDetector;
import com.sharesmile.share.pushNotification.NotificationConsts;
import com.sharesmile.share.pushNotification.NotificationHandler;
import com.sharesmile.share.rfac.activities.LoginActivity;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.CauseList;
import com.sharesmile.share.rfac.models.UserDetails;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.ServerTimeKeeper;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.sharesmile.share.core.Constants.PREF_APP_VERSION;
import static com.sharesmile.share.core.Constants.PREF_DISABLE_ALERTS;
import static com.sharesmile.share.core.Constants.PREF_SCHEDULE_WALK_ENGAGEMENT_NOTIF_AFTER;
import static com.sharesmile.share.core.Constants.PREF_USER_DETAILS;
import static com.sharesmile.share.core.Constants.PREF_USER_ID;
import static com.sharesmile.share.core.NotificationActionReceiver.NOTIFICATION_ID;
import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_STILL_ID;
import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_USAIN_BOLT_FORCE_EXIT_ID;
import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_USAIN_BOLT_ID;
import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_WALK_ENGAGEMENT;


/**
 * Created by ankitmaheshwari1 on 30/12/15.
 */
public class MainApplication extends MultiDexApplication implements AppLifecycleHelper.LifeCycleCallbackListener{

    private static final String TAG = "MainApplication";

    private static MainApplication instance;
    public static final long MINUTE_INTEVAL = 60000;
    private boolean isModelShown = false;
    private int visibleActiviesCount = 0;
    private DbWrapper mDbWrapper;

    private AppLifecycleHelper lifecycleHelper;

    private CauseList causeList;

    //generally for singleton class constructor is made private but since this class is registered
    //in manifest and extends Application constructor is public so OS can instantiate it
    //Note: Developers should not call constructor. Should use getInstance method instead
    public MainApplication() {
        instance = this;
    }

    public static MainApplication getInstance() {
        if (instance == null) {
            Logger.e(TAG, "Main application instance should never be null");
        }
        return instance;
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread.
     */
    public static void showToast(final String message, final int duration) {
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(message) == false) {
                    if (getInstance().appToast == null){
                        getInstance().appToast = Toast.makeText(getContext(), message, duration);
                    }else {
                        getInstance().appToast.setDuration(duration);
                        getInstance().appToast.setText(message);
                    }
                    getInstance().appToast.show();
                }
            }
        });
    }

    private Toast appToast;

    /**
     * A thread safe way to show a Toast. Can be called from any thread. uses resource id of the
     * message string
     */
    public static void showToast(int stringId) {
        showToast(getContext().getResources().getString(stringId));
    }

    /**
     *
     * @param notifTitle
     * @param notificationId
     * @param notifText
     * @param args
     * @return true if the notification was actually shown, false otherwise
     */
    public static boolean showRunNotification(String notifTitle, int notificationId, String notifText, String... args){

        if (notificationId == WORKOUT_NOTIFICATION_WALK_ENGAGEMENT || notificationId == WORKOUT_NOTIFICATION_STILL_ID){
            if (SharedPrefsManager.getInstance().getBoolean(PREF_DISABLE_ALERTS)){
                Logger.i(TAG, "Won't show notification with id:" + notificationId + ", as alerts are disabled");
                return false;
            }
            if (notificationId == WORKOUT_NOTIFICATION_WALK_ENGAGEMENT){
                long currentTsMillis = DateUtil.getServerTimeInMillis();
                long scheduleWalkEngagementAfter = SharedPrefsManager.getInstance().getLong(PREF_SCHEDULE_WALK_ENGAGEMENT_NOTIF_AFTER);
                if (currentTsMillis < scheduleWalkEngagementAfter){
                    // Won't show walk engagement notif, can only show it after "scheduleWalkEngagementAfter" time
                    Logger.i(TAG, "Won't show walk engagement notif, can only show it after " + scheduleWalkEngagementAfter + " time");
                    return false;
                }
            }
            if (notificationId == WORKOUT_NOTIFICATION_STILL_ID){
                if (!WorkoutSingleton.getInstance().isRunning()){
                    // Don't show STILL notification if workout is paused
                    Logger.i(TAG, "Won't still notif, because user has paused the workout");
                    return false;
                }
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());

        long[] vibratePattern;
        if (notificationId == WORKOUT_NOTIFICATION_WALK_ENGAGEMENT){
            // Long vibration for walk engagement notification
            vibratePattern = new long[]{0, 800, 500, 2500}; // It's a { delay, vibrate, sleep, vibrate, sleep } pattern
        }else {
            vibratePattern = new long[]{0, 200, 100, 400}; // It's a { delay, vibrate, sleep, vibrate, sleep } pattern
        }

        builder.setContentText( notifText)
                .setSmallIcon(getNotificationIcon())
                .setColor(ContextCompat.getColor(getContext(), R.color.denim_blue))
                .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher))
                .setContentTitle(notifTitle)
                .setVibrate(vibratePattern)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notifText))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH){
            for (String action : args){
                Logger.i(TAG, "Setting pendingIntent for " + action);
                if (getContext().getString(R.string.notification_action_pause).equals(action)){
                    builder.addAction(R.drawable.ic_pause_black_24px, "Pause",
                            createNotificationActionReceiverPendingIntent(action, notificationId));
                }else if (getContext().getString(R.string.notification_action_resume).equals(action)){
                    builder.addAction(R.drawable.ic_play_arrow_black_24px, "Resume",
                            createNotificationActionReceiverPendingIntent(action, notificationId));
                }else if (getContext().getString(R.string.notification_action_stop).equals(action)){
                    builder.addAction(R.drawable.ic_stop_black_24px, "Stop", getInstance().createStopRunIntent());
                }else if (getContext().getString(R.string.notification_action_start).equals(action)){
                    builder.addAction(R.drawable.ic_play_arrow_black_24px, "Start", getInstance().createAppIntent());
                }else if (getContext().getString(R.string.notification_action_cancel).equals(action)){
                    builder.addAction(R.drawable.ic_close_black_24dp, "Cancel",
                            createNotificationActionReceiverPendingIntent(action, notificationId));
                }
            }
        }
        builder.setDeleteIntent(getDeleteIntent(notificationId));
        builder.setContentIntent(getInstance().createAppIntent());
        NotificationManagerCompat.from(getContext()).notify(notificationId, builder.build());
        return true;
    }

    private static PendingIntent createNotificationActionReceiverPendingIntent(String action, int notifId){
        Intent intent = new Intent(getInstance(), NotificationActionReceiver.class);
        intent.setAction(action);
        intent.putExtra(NOTIFICATION_ID, notifId);
        return PendingIntent.getBroadcast(getContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private static PendingIntent getDeleteIntent(int notifId)
    {
        Intent intent = new Intent(getInstance(), NotificationActionReceiver.class);
        intent.setAction(getContext().getString(R.string.notification_action_dismiss));
        intent.putExtra(NOTIFICATION_ID, notifId);
        return PendingIntent.getBroadcast(getContext(), 101, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static void showRunNotification(int notificationId, String notifText, String... args){
        showRunNotification(getContext().getResources().getString(R.string.app_name), notificationId, notifText, args);
    }

    private static int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_stat_onesignal_default : R.mipmap.ic_launcher;
    }


    public static Context getContext() {
        return instance.getApplicationContext();
    }

    /**
     * @return a {@link Handler} tied to the main thread.
     */
    public static Handler getMainThreadHandler() {
        return new Handler(Looper.getMainLooper());
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread.
     */
    public static void showToast(final String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }


    public static void displayTimedToast(int stringId, int timeInMillis) {

        final Toast toast = Toast.makeText(getContext(), stringId, Toast.LENGTH_LONG);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, timeInMillis);
    }

    public static void displayTimedToast(String message, int timeInMillis) {

        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, timeInMillis);
    }

    public static RefWatcher getRefWatcher(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        Logger.i(TAG, "onCreate");
        CleverTapAPI.setDebugLevel(1277182231);
        ActivityLifecycleCallback.register(this);

        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Normal app init code...

        //Initialization code
        SharedPrefsManager.initialize(getApplicationContext());

        ServerTimeKeeper.init();

        lifecycleHelper = new AppLifecycleHelper(this);
        registerActivityLifecycleCallbacks(lifecycleHelper);

        Analytics.initialize(this);
        Fabric.with(this, new Crashlytics());
        initOneSignal();
        mDbWrapper = new DbWrapper(this);
        GoogleLocationTracker.initialize(this);
        WorkoutSingleton.initialize(getApplicationContext());
        ActivityDetector.initialize(this);
        LeaderBoardDataStore.initialize(this);
        startSyncTasks();
        checkForFirstLaunchAfterInstall();

        causeList = SharedPrefsManager.getInstance().getObject(Constants.KEY_CAUSE_LIST, CauseList.class);

        updateAppVersionInPrefs();
        AnalyticsEvent.create(Event.ON_APPLICATION_CREATE)
                .buildAndDispatch();

    }

    public void updateAppVersionInPrefs(){
        try {
            String currentVersion = "";
            PackageInfo pInfo = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            currentVersion = pInfo.versionName;

            String storedVersion = SharedPrefsManager.getInstance().getString(PREF_APP_VERSION);

            SharedPrefsManager.getInstance().setString(PREF_APP_VERSION, currentVersion);

            if (!TextUtils.isEmpty(storedVersion) && !storedVersion.equals(currentVersion)){
                // App update just happened, send app_update event
                AnalyticsEvent.create(Event.ON_APP_UPDATE)
                        .buildAndDispatch();
            }

        }catch (Exception e){
            Logger.e(TAG, e.getMessage());
        }
    }

    public void updateCauseList(CauseList updated){
        this.causeList = updated;
        SharedPrefsManager.getInstance().setObject(Constants.KEY_CAUSE_LIST, updated);
        if (causeList != null){
            for (CauseData causeData : causeList.getCauses()) {
                if (causeData.getApplicationUpdate() != null) {
                    int latestVersion = SharedPrefsManager.getInstance().getInt(Constants.PREF_LATEST_APP_VERSION, 0);
                    if (latestVersion < causeData.getApplicationUpdate().app_version && causeData.getApplicationUpdate().app_version > BuildConfig.VERSION_CODE) {
                        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_APP_UPDATE_DIALOG, true);
                        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_FORCE_UPDATE, causeData.getApplicationUpdate().force_update);
                        SharedPrefsManager.getInstance().setInt(Constants.PREF_LATEST_APP_VERSION, causeData.getApplicationUpdate().app_version);
                        SharedPrefsManager.getInstance().setString(Constants.PREF_APP_UPDATE_MESSAGE, causeData.getApplicationUpdate().message);
                    }
                }
            }
        }
    }

    public CauseList getCauseList(){
        return causeList;
    }

    public List<CauseData> getCausesToShow(){
        List<CauseData> activeCauses = new ArrayList<CauseData>();
        if (causeList != null){
            for (CauseData causeData : causeList.getCauses()) {
                if (causeData.isActive()) {
                    activeCauses.add(causeData);
                }else if (causeData.isCompleted()){
                    activeCauses.add(causeData);
                }
            }
        }
        return activeCauses;
    }

    public PendingIntent createAppIntent(){
        Intent resultIntent = new Intent(getInstance().getApplicationContext(), LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return resultPendingIntent;
    }

    public PendingIntent createStopRunIntent(){
        Intent resultIntent = new Intent(getInstance().getApplicationContext(), LoginActivity.class);
        resultIntent.putExtra(LoginActivity.INTENT_STOP_RUN, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
        return resultPendingIntent;
    }

    private void initOneSignal() {
        OneSignal.startInit(this).setNotificationOpenedHandler(new NotificationHandler()).init();
        String email = SharedPrefsManager.getInstance().getString(Constants.PREF_USER_EMAIL);
        if (!TextUtils.isEmpty(email)) {
            OneSignal.syncHashedEmail(email);
        }

        int total_runs = SharedPrefsManager.getInstance().getInt(Constants.PREF_TOTAL_RUN, 0);
        OneSignal.sendTag(NotificationConsts.UserTag.RUN_COUNT, String.valueOf(total_runs));
    }

    public void startSyncTasks() {
        Logger.d(TAG, "startSyncTasks");
        SyncHelper.syncUserFromDB();
        ClientConfig.sync();
        SyncHelper.scheduleDataSync(this);
        SyncHelper.syncMessageCenterData(this);
        boolean isWorkoutDataUpToDate = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_WORKOUT_DATA_UP_TO_DATE_IN_DB, false);
        Logger.d(TAG, "startSyncTasks: isWorkoutDataUpToDate = " + isWorkoutDataUpToDate);
        if (!isWorkoutDataUpToDate){
            // Need to forcefully refresh workout data now
            SyncHelper.forceRefreshEntireWorkoutHistory();
        }
    }


    public DbWrapper getDbWrapper() {
        return mDbWrapper;
    }

    public String getToken() {
        return SharedPrefsManager.getInstance().getString(Constants.PREF_AUTH_TOKEN);
    }

    public int getUserID() {
        return SharedPrefsManager.getInstance().getInt(PREF_USER_ID);
    }

    /**
     * @return UserDetails object if user is logged in, NULL otherwise
     */
    public UserDetails getUserDetails(){
        return SharedPrefsManager.getInstance().getObject(PREF_USER_DETAILS, UserDetails.class);
    }

    public float getBodyWeight(){
        if (isLogin() && getUserDetails() != null){
            return getUserDetails().getBodyWeight();
        }
        return 0;
    }

    public void setBodyWeight(float bodyWeight){
        UserDetails details = getUserDetails();
        if (details != null){
            details.setBodyWeight(bodyWeight);
            SharedPrefsManager.getInstance().setObject(PREF_USER_DETAILS, details);
            Analytics.getInstance().setUserProperty("body_weight", bodyWeight);
            AnalyticsEvent.create(Event.ON_SET_BODY_WEIGHT)
                    .put("body_weight", bodyWeight)
                    .buildAndDispatch();
        }
    }

    public void setUserDetails(UserDetails details){
        Gson gson = new Gson();
        Logger.d(TAG, "setUserDetails as: " + gson.toJson(details));
        SharedPrefsManager.getInstance().setObject(PREF_USER_DETAILS, details);

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();

        if (!TextUtils.isEmpty(details.getFirstName())){
            Analytics.getInstance().setUserName(details.getFullName());
        }

        if (!TextUtils.isEmpty(details.getLastName())){
            Analytics.getInstance().setUserName(details.getFullName());
        }

        if (!TextUtils.isEmpty(details.getEmail())){
            prefsManager.setString(Constants.PREF_USER_EMAIL, details.getEmail());
            Analytics.getInstance().setUserEmail(details.getEmail());
        }

        prefsManager.setBoolean(Constants.PREF_IS_LOGIN, true);

        prefsManager.setInt(Constants.PREF_USER_ID, details.getUserId());
        Analytics.getInstance().setUserId(details.getUserId());

        prefsManager.setString(Constants.PREF_AUTH_TOKEN, details.getAuthToken());
        if (!TextUtils.isEmpty(details.getPhoneNumber())){
            Analytics.getInstance().setUserPhone(details.getPhoneNumber());
        }

        if (!TextUtils.isEmpty(details.getGenderUser())){
            Analytics.getInstance().setUserGender(details.getGenderUser());
        }

        prefsManager.setBoolean(Constants.PREF_IS_SIGN_UP_USER, details.isSignUp());
        Logger.d(TAG, "setUserDetails: myTeamId stored = " + LeaderBoardDataStore.getInstance().getMyTeamId()
                + ", and myTeamId in user details = " + details.getTeamId());
        if (LeaderBoardDataStore.getInstance().getMyTeamId() != details.getTeamId()){
            LeaderBoardDataStore.getInstance().setLeagueTeamId(details.getTeamId());
            Analytics.getInstance().setUserImpactLeagueTeamCode(details.getTeamId());
        }
    }

    public static boolean isLogin() {
        return SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN, false);
    }

    public void setModelShown() {
        isModelShown = true;
    }

    public boolean isModelShown() {
        return isModelShown;
    }

    public static boolean isApplicationInForeground(){
        return getInstance().lifecycleHelper.isApplicationInForeground();
    }

    public static boolean isApplicationVisible(){
        return getInstance().lifecycleHelper.isApplicationVisible();
    }

    private void checkForFirstLaunchAfterInstall(){
        if (!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_FIRST_LAUNCH_EVENT_SENT)){
            AnalyticsEvent.create(Event.FIRST_LAUNCH_AFTER_INSTALL)
                    .build()
                    .dispatch();
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_FIRST_LAUNCH_EVENT_SENT, true);
        }
    }

    @Override
    public void onStart() {
        Logger.i(TAG, "onStart");
        if (!ServerTimeKeeper.getInstance().isInSyncWithServer()){
            ServerTimeKeeper.getInstance().syncTimerWithServerTime();
        }
        GoogleLocationTracker.getInstance().startLocationTracking(false);
        AnalyticsEvent.create(Event.LAUNCH_APP).buildAndDispatch();

        Logger.d(TAG, "Cancelling certain notifications");
        ActivityDetector.getInstance().cancelWalkEngagementNotif();
        WorkoutService.cancelWorkoutNotification(WORKOUT_NOTIFICATION_USAIN_BOLT_ID);
        WorkoutService.cancelWorkoutNotification(WORKOUT_NOTIFICATION_USAIN_BOLT_FORCE_EXIT_ID);
        WorkoutService.cancelWorkoutNotification(WORKOUT_NOTIFICATION_STILL_ID);
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
        Logger.i(TAG, "onStop");
        GoogleLocationTracker.getInstance().stopLocationTracking();
    }
}

