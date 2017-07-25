package com.sharesmile.share.core;

import android.os.Handler;
import android.os.Looper;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.utils.Logger;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by ankitmaheshwari on 7/25/17.
 */

public abstract class ExpoBackoffTask implements Runnable {

    private static final String TAG = "ExpoBackoffTask";
    public static final int DEFAULT_DELAY = 250; // in millisecs

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_RESCHEDULE = 1;
    public static final int RESULT_FAILURE = 2;

    private int delayMs;

    public ExpoBackoffTask(int initialdelayMs){
        this.delayMs = initialdelayMs;
    }

    public ExpoBackoffTask(){
        this.delayMs = DEFAULT_DELAY;
    }

    @Override
    public void run() {

        if (Looper.getMainLooper() == Looper.myLooper()){
            // On Main thread, need to switch to background thread
            AsyncTask.execute(this);
        }else {
            // On background thread, can carry on
            if (NetworkUtils.isNetworkConnected(MainApplication.getContext())){
                if (performtask() == RESULT_RESCHEDULE){
                    reschedule();
                }
            }else {
                // Internet not available right now, reschedule
                reschedule();
            }
        }

    }

    private void reschedule(){
        delayMs *= 2;
        Logger.d(TAG, "Re-scheduling after " + delayMs + " milli secs");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this, delayMs);
    }

    public abstract int performtask();

}
