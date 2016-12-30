package com.sharesmile.share;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.sharesmile.share.utils.Logger;

public class AppLifecycleHelper implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "AppLifecycleHelper";

    private int created;
    private int started;
    private int resumed;
    private int paused;
    private int stopped;
    private int destroyed;

    private LifeCycleCallbackListener listener;

    public AppLifecycleHelper(LifeCycleCallbackListener listener) {
        this.listener = listener;
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        created++;
        Logger.d(TAG, "onActivityCreated: " + activity.getClass().getSimpleName()
                + ", createdCount = " + created);
    }

    public void onActivityDestroyed(Activity activity) {
        destroyed++;
        Logger.d(TAG, "onActivityDestroyed: " + activity.getClass().getSimpleName()
                + ", destroyedCount = " + destroyed);
        if (areAllActivitiesDestroyed()){
//            SessionManager.getInstance().onApplicationDestroyed();
//            Analytics.getInstance().onApplicationDestroyed();
        }
    }

    public void onActivityResumed(Activity activity) {
        if (!isApplicationInForeground()){
            listener.onResume();
        }
        ++resumed;
        Logger.d(TAG, "ForegroundCheck: onActivityResumed: " + activity.getClass().getSimpleName());
        Logger.i(TAG, "ForegroundCheck: Is Application Backgrounded? " + (resumed == stopped)
                + ", stoppedCount = " + stopped + ", resumedCount = " + resumed);
    }



    public void onActivityPaused(Activity activity) {
        ++paused;
        Logger.d(TAG, "onActivityPaused: " + activity.getClass().getSimpleName());
        if (!isApplicationInForeground()){
            listener.onPause();
        }
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityStarted(Activity activity) {
        if (!isApplicationVisible()){
            // started and stopped both are 0,
            // that means it is the first activity to come on display
            // i.e. App is launched from outside the app

            // And this is the very first activity which is coming into foreground
            listener.onStart();
//            Logger.d(TAG, "AppVisibilityCheck: first activity to become visible LAUNCH_APP will be triggered");
//            AnalyticsEvent.Builder builder = new AnalyticsEvent
//                    .Builder(activity.getApplicationContext())
//                    .event(Event.LAUNCH_APP)
//                    .eventCategory(AnalyticsEvent.CATEGORY_APP_LAUNCH);
//            if (activity instanceof MvpView){
//                builder.put(Properties.SCREEN_NAME, ((MvpView) activity).getScreenId());
//            }
//            builder.build().dispatch();
        }
        ++started;
        Logger.d(TAG, "onActivityStarted: " + activity.getClass().getSimpleName());
    }

    public void onActivityStopped(Activity activity) {
        ++stopped;
        Logger.d(TAG, "AppVisibilityCheck: onActivityStopped: " + activity.getClass().getSimpleName());
        Logger.i(TAG, "AppVisibilityCheck: Is Application Backgrounded? " + (resumed == stopped)
                + ", stoppedCount = " + stopped + ", resumedCount = " + resumed);
        if (!isApplicationVisible()){
            // started and stopped both are 0
            // That means there is NO Activity in visible state when this activity stopped
            // i.e. User came out of the App, perform all Application wide persistence tasks over here
            listener.onStop();
        }
    }

    /**
     * Checks whether App is visible to the user or not
     * @return true if visible and false otherwise
     */
    public boolean isApplicationInForeground() {
        return resumed > paused;
    }

    public boolean areAllActivitiesDestroyed(){
        return created <= destroyed;
    }

    public boolean isApplicationVisible(){
        return started > stopped;
    }

    interface LifeCycleCallbackListener {
        void onStart();
        void onResume();
        void onPause();
        void onStop();
    }

}

