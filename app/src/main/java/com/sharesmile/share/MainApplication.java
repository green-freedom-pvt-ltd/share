package com.sharesmile.share;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.DbWrapper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import io.fabric.sdk.android.Fabric;

/**
 * Created by ankitmaheshwari1 on 30/12/15.
 */
public class MainApplication extends Application {

    private static final String TAG = "MainApplication";

    private static MainApplication instance;
    private static Handler sMainThreadHandler;
    public static final long MINUTE_INTEVAL = 60000;
    private int visibleActiviesCount = 0;
    private DbWrapper mDbWrapper;
    private String mToken;
    private int mUserId = 0;

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
        showToast(message, Toast.LENGTH_LONG);
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
        super.onCreate();
        //Initialization code
        SharedPrefsManager.initialize(getApplicationContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_comsumer_key), getString(R.string.twitter_comsumer_secret));
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer()/*, new Crashlytics()*/);
        mDbWrapper = new DbWrapper(this);
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


    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }


}

