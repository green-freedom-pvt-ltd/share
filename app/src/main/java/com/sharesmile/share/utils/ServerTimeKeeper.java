package com.sharesmile.share.utils;

/**
 * Created by ankitmaheshwari on 6/9/17.
 */

import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.network.ServerTimeResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    private ServerTimeKeeper() {
        isInSyncWithServer = new AtomicBoolean(false);
        isFetchingServerTime = new AtomicBoolean(false);
        // Sync with Server
        syncTimerWithServerTime();
    }

    public static void init() {
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
        if (isInSyncWithServer()) {
            long millitimeDiff = System.currentTimeMillis() - getInstance().systemTimeAtSync;
            timeStampToReturn = (getInstance().serverTimeAtSync + millitimeDiff);
        } else {
            //Start sync process and return the Current System time for time being
            getInstance().syncTimerWithServerTime();
            timeStampToReturn = System.currentTimeMillis();
        }
        String syncUnsync = (isInSyncWithServer()) ? "SYNCED" : "UNSYNCED";
        Logger.d(TAG, "getServerTimeStampInMillis: returning " + syncUnsync
                + " timeStamp " + timeStampToReturn);
        return timeStampToReturn;
    }

    public static void setTimerOutOfSync() {
        getInstance().isInSyncWithServer.set(false);
        getInstance().syncTimerWithServerTime();
    }

    public synchronized void syncTimerWithServerTime() {
        if (!isFetchingServerTime.get()) {
            //Start Server time fetching logic if network is connected and server time is not
            // being fetched already
            isFetchingServerTime.set(true);

            NetworkDataProvider.doGetCallAsync(Urls.getServerTimeUrl(), new NetworkAsyncCallback<ServerTimeResponse>() {
                @Override
                public void onNetworkFailure(NetworkException ne) {
                    isFetchingServerTime.set(false);
                    Logger.d(TAG, "Failure while fetching servertime:\n" + ne);
                    ne.printStackTrace();
                }
                @Override
                public void onNetworkSuccess(ServerTimeResponse response) {
                    // Will do nothing as times are already synced in onResponse method of NetworkAsyncCallback
                    isFetchingServerTime.set(false);
                }
            });
        }
    }

    public static boolean isInSyncWithServer() {
        return getInstance().isInSyncWithServer.get();
    }

    public void syncServerAndSystemMilliTime(long serverTimeFetchedInMillis) {
        synchronized (ServerTimeKeeper.class){
            serverTimeAtSync = serverTimeFetchedInMillis;
            systemTimeAtSync = System.currentTimeMillis();
            isInSyncWithServer.set(true);
        }
        Logger.d(TAG, "Syncing Server Time: " + serverTimeAtSync +
                " System Time: " + systemTimeAtSync);
    }

}
