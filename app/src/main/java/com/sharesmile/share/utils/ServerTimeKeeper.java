package com.sharesmile.share.utils;

/**
 * Created by ankitmaheshwari on 6/9/17.
 */

import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;

import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.network.ServerTimeResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ankitm on 09/04/16.
 */
public class ServerTimeKeeper {

    private static final String TAG = "ServerTimeKeeper";

    private static ServerTimeKeeper instance;
    private AtomicBoolean isInSyncWithServer;
    private AtomicBoolean isFetchingServerTime;
    private Long serverTimeAtSync;// in millisecs
    private Long systemTimeAtSync;// in millisecs
    private Long realTimeAtSync;// in millisecs
    private Handler handler;

    public static final long INITIAL_DELAY = 250; // in millisecs
    public static final long THRESHOLD_DELTA = 150; // in millisecs

    private ServerTimeKeeper() {
        Logger.i(TAG, "constructor called");
        isInSyncWithServer = new AtomicBoolean(false);
        isFetchingServerTime = new AtomicBoolean(false);
        handler = new Handler();
        serverTimeAtSync = 0L;
        systemTimeAtSync = 0L;
        realTimeAtSync = 0L;
        // Sync with Server
        syncTimerWithServerTime();
    }

    public static void init() {
        Logger.i(TAG, "init");
        if (instance == null) {
            synchronized (ServerTimeKeeper.class) {
                if (instance == null) {
                    instance = new ServerTimeKeeper();
                }
            }
        }
    }

    public static ServerTimeKeeper getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    public static final String getServerTimeStampAsString(){
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyy HH:mm:ss", Locale.ENGLISH);//dd/MM/yyyy
        Date t = new Date(getServerTimeStampInMillis());
        String strDate = sdfDate.format(t);
        return strDate;
    }

    /**
     * Non blocking method to return current server time epoch in millis
     *
     * @return server's current time_stamp in milli secs (Epoch timeStamp)
     */
    public static long getServerTimeStampInMillis() {
        long timeStampToReturn;
        if (!getInstance().isInSyncWithServer()) {
            //Start sync process
            getInstance().syncTimerWithServerTime();
        }
        //return currentServerTimeStamp in millis
        long millitimeDiff = System.currentTimeMillis() - getInstance().systemTimeAtSync;
        timeStampToReturn = (getInstance().serverTimeAtSync + millitimeDiff);
        return timeStampToReturn;
    }

    public void checkIfTimerIsOutOfSync(){
        if (!isInSyncWithServer()){
            // If timer is not in sync yet then simply go for sync
            syncTimerWithServerTime();
        }else {
            synchronized (ServerTimeKeeper.class){
                // calculate delta between realTime and systemTime at last sync
                long deltaAtSync = systemTimeAtSync - realTimeAtSync;
                // calculate delta between realTime and systemTime now
                long deltaNow = System.currentTimeMillis() - SystemClock.elapsedRealtime();

                if (Math.abs(deltaAtSync - deltaNow) > THRESHOLD_DELTA){
                    // Time difference is substantial, this is most likely a manual update in System clock
                    // setTimerOutOfSync and Go for a sync
                    setTimerOutOfSync();
                }
            }
        }
    }

    private void setTimerOutOfSync() {
        Logger.i(TAG, "setTimerOutOfSync");
        isInSyncWithServer.set(false);
        syncTimerWithServerTime();
    }

    public synchronized void syncTimerWithServerTime() {
        Logger.d(TAG, "syncTimerWithServerTime");
        if (isInSyncWithServer()){
            Logger.d(TAG, "Already in sync with server, won't do anything");
            return;
        }
        if (!isFetchingServerTime.get()) {
            //Start Server time fetching logic if network is connected and server time is not
            // being fetched already
            if (NetworkUtils.isNetworkConnected(MainApplication.getContext())){
                executeSyncCallAsynchronously();
            }else {
                // Network not present shall retry after some time, expBackoff
                expBackoffRetry();
            }
        }
    }

    long expBackoffDelay = INITIAL_DELAY;

    private void expBackoffRetry(){
        expBackoffDelay *= 2;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                syncTimerWithServerTime();
            }
        }, expBackoffDelay);
    }

    private void executeSyncCallAsynchronously(){
        isFetchingServerTime.set(true);
        Logger.d(TAG, "Will invoke servertime GET call");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ServerTimeResponse> list =
                            NetworkDataProvider.doGetCall(Urls.getServerTimeUrl(),
                                    new TypeToken<List<ServerTimeResponse>>(){}.getType());
                    if (list.size() > 0){
                        long currentServerTimeMillis = list.get(0).getTimeEpoch();
                        Logger.d(TAG, "Successfully fetched serverTime: " + currentServerTimeMillis);
                        syncServerAndSystemMilliTime(currentServerTimeMillis);
                    }else {
                        Logger.e(TAG, "Failure fetching servertime, response list empty");
                    }
                }catch (NetworkException e) {
                    Logger.e(TAG, "Failure while fetching servertime:\n" + e);
                    e.printStackTrace();
                }finally {
                    isFetchingServerTime.set(false);
                    if (!isInSyncWithServer()){
                        // Still not synced, will retry
                        expBackoffRetry();
                    }
                }
            }
        });
    }

    public boolean isInSyncWithServer() {
        return isInSyncWithServer.get();
    }

    public void syncServerAndSystemMilliTime(long serverTimeFetchedInMillis) {
        synchronized (ServerTimeKeeper.class){
            serverTimeAtSync = serverTimeFetchedInMillis;
            systemTimeAtSync = System.currentTimeMillis();
            realTimeAtSync = SystemClock.elapsedRealtime();
            isInSyncWithServer.set(true);
        }
        Logger.d(TAG, "Syncing Server Time: " + serverTimeAtSync +
                " System Time: " + systemTimeAtSync);
    }

}
