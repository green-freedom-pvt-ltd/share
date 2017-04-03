package com.sharesmile.share;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.bugfender.sdk.Bugfender;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.crashlytics.android.Crashlytics;
import com.onesignal.OneSignal;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.DbWrapper;
import com.sharesmile.share.core.NotificationActionReceiver;
import com.sharesmile.share.gps.GoogleLocationTracker;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.activityrecognition.ActivityDetector;
import com.sharesmile.share.pushNotification.NotificationConsts;
import com.sharesmile.share.pushNotification.NotificationHandler;
import com.sharesmile.share.rfac.activities.LoginActivity;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import io.fabric.sdk.android.Fabric;

import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_ID;


/**
 * Created by ankitmaheshwari1 on 30/12/15.
 */
public class MainApplication extends Application implements AppLifecycleHelper.LifeCycleCallbackListener{

    private static final String TAG = "MainApplication";

    private static MainApplication instance;
    private static Handler sMainThreadHandler;
    public static final long MINUTE_INTEVAL = 60000;
    private boolean isModelShown = false;
    private int visibleActiviesCount = 0;
    private DbWrapper mDbWrapper;
    private String mToken;
    private int mUserId = 0;

    private AppLifecycleHelper lifecycleHelper;

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
     * A thread safe way to show a Toast. Can be called from any thread. uses resource id of the
     * message string
     */
    public static void showToast(int stringId, final int duration) {
        showToast(getContext().getResources().getString(stringId), duration);
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread.
     */
    public static void showToast(final String message, final int duration) {
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(message) == false) {
                    Toast.makeText(getContext(), message, duration).show();
                }
            }
        });
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread. uses resource id of the
     * message string
     */
    public static void showToast(int stringId) {
        showToast(getContext().getResources().getString(stringId));
    }

    public static void showRunNotification(String notifText, String... args){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setContentText( notifText )
                .setSmallIcon(getNotificationIcon())
                .setColor(ContextCompat.getColor(getContext(), R.color.denim_blue))
                .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getContext().getResources().getString(R.string.app_name))
                .setVibrate(new long[]{500, 500, 500, 500})
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notifText));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH){
            for (String action : args){
                Logger.i(TAG, "Setting pendingIntent for " + action);
                Intent intent = new Intent(getInstance(), NotificationActionReceiver.class);
                intent.setAction(action);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 100, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                if (getContext().getString(R.string.notification_action_pause).equals(action)){
                    builder.addAction(R.drawable.ic_pause_black_24px, "Pause", pendingIntent);
                }else if (getContext().getString(R.string.notification_action_resume).equals(action)){
                    builder.addAction(R.drawable.ic_play_arrow_black_24px, "Resume", pendingIntent);
                }else if (getContext().getString(R.string.notification_action_stop).equals(action)){
                    builder.addAction(R.drawable.ic_stop_black_24px, "Stop", pendingIntent);
                }
            }
        }
        builder.setContentIntent(getInstance().createAppIntent());

        NotificationManagerCompat.from(getContext()).notify(WORKOUT_NOTIFICATION_ID, builder.build());
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
        if (sMainThreadHandler == null) {
            // No need to synchronize -- it's okay to create an extra Handler,
            // which will be used only once and then thrown away.
            sMainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return sMainThreadHandler;
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

    @Override
    public void onCreate() {
        CleverTapAPI.setDebugLevel(1277182231);
        ActivityLifecycleCallback.register(this);
        super.onCreate();
        //Initialization code
        SharedPrefsManager.initialize(getApplicationContext());

        lifecycleHelper = new AppLifecycleHelper(this);
        registerActivityLifecycleCallbacks(lifecycleHelper);

        Analytics.initialize(this);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_comsumer_key), getString(R.string.twitter_comsumer_secret));
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer(), new Crashlytics());
        initOneSignal();
        mDbWrapper = new DbWrapper(this);
        GoogleLocationTracker.initialize(this);
        ActivityDetector.initialize(this);
        startSyncTasks();
        checkForFirstLaunchAfterInstall();

        WorkoutSingleton.initialize(getApplicationContext());

        Bugfender.init(this, "tCMmpKrIgAqf4ZgfOA6Z1x00P7pugWna", BuildConfig.DEBUG);
        Bugfender.enableLogcatLogging();
        Bugfender.enableUIEventLogging(this);
    }

    public PendingIntent createAppIntent(){
        Intent resultIntent = new Intent(getInstance().getApplicationContext(), LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
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

    private void startSyncTasks() {
        SyncHelper.syncRunData();
        SyncHelper.syncMessageCenterData(this);
        SyncHelper.syncLeaderBoardData(this);
    }

    public DbWrapper getDbWrapper() {
        return mDbWrapper;
    }

    public String getToken() {
        if (mToken == null) {
            mToken = SharedPrefsManager.getInstance().getString(Constants.PREF_AUTH_TOKEN);
        }
        return mToken;
    }

    public int getUserID() {
        if (mUserId == 0) {
            mUserId = SharedPrefsManager.getInstance().getInt(Constants.PREF_USER_ID);

        }
        return mUserId;
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
        GoogleLocationTracker.getInstance().startLocationTracking(false);
        AnalyticsEvent.create(Event.LAUNCH_APP).buildAndDispatch();
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

