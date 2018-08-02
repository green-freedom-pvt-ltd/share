package com.sharesmile.share.network;

import android.os.Handler;
import android.os.Looper;

import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.timekeeping.ServerTimeKeeper;


import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 Used to make async network calls using OkHttp.
 Provides two callbacks, first <code>onNetworkFailure</code> for all the failure scenarios
 including request failure, IO failure, non success response, error in parsing response; second
 <code>onNetworkSuccess</code> when the response is succes and is successfully parsed into
 desired object. Both of them are fired on the main thread.
 Created by ankitmaheshwari1 on 22/12/15.
 */
public abstract class NetworkAsyncCallback<Wrapper extends UnObfuscable> implements Callback {

    private static final String TAG = "NetworkAsyncCallback";

    private final Class<Wrapper> mWrapperClass;
    private boolean isCancelled = false;
    private static Handler mainThreadHandler;

    protected NetworkAsyncCallback() {
        super();
        this.mWrapperClass = (Class) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public static Handler getMainThreadHandler(){
        if (mainThreadHandler == null){
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return mainThreadHandler;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Logger.d(TAG, "onFailure");
        if (isCancelled()){
            // Callback was cancelled, do nothing with the request
            return;
        }
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                onNetworkFailure(NetworkUtils.wrapIOException(call.request(), e));
            }
        });
    }
    @Override
    public final void onResponse(Call call,final Response response) throws IOException {
        Logger.d(TAG, "onResponse");
        if (isCancelled()){
            // Callback was cancelled, do nothing with the response
            return;
        }
        try {
            if (!ServerTimeKeeper.getInstance().isInSyncWithServer() &&
                    response.headers().getDate("Date") != null){
                ServerTimeKeeper.getInstance().syncServerAndSystemMilliTime
                        (response.headers().getDate("Date").getTime());
            }
            final Wrapper responseObject = NetworkUtils.handleResponse(response, mWrapperClass);
            getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    onNetworkSuccess(responseObject);
                }
            });
        } catch (final NetworkException ne) {
            getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    Logger.e(TAG, "NetworkException: " + ne.getMessage(), ne);
                    onNetworkFailure(ne);
                    if (ne.getFailureType() == FailureType.TOKEN_EXPIRED){
                        onNetworkTokenExpired(ne);
                    }
                }
            });
        }
    }

    public void cancelCallback(){
        synchronized (this){
            isCancelled = true;
        }
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                onCancelCallback();
            }
        });
    }

    public synchronized boolean isCancelled(){
        return isCancelled;
    }

    public abstract void onNetworkFailure(NetworkException ne);

    public abstract void onNetworkSuccess(Wrapper wrapper);

    public void onCancelCallback(){
        //Do nothing, caller will override this if it needs to handle cancellation
    }

    public void onNetworkTokenExpired(NetworkException ne) {
        //TODO: Login Auth Token Expired, initiate Logout Flow.
        // Classes extending NetworkAsyncCallback might not necessarily implement this method

    }
}
