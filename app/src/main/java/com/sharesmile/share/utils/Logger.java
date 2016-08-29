package com.sharesmile.share.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * This class has a private constructor, and is final. It is meant to be used with static methods
 * only, and not as an instance at all
 */
public final class Logger {

    private static final String TAG = "Logger";

    private Logger() {

    }

    public static void d(String tag, String text) {
        d(tag, text, null);
    }

    public static void d(String tag, String text, Throwable th) {
        if (true) {
            if (!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(text)) {
                if (th == null) {
                    Log.d(tag, text);
                } else {
                    Log.d(tag, text, th);
                }
            }
        }
    }

    public static void i(String tag, String text) {
        i(tag, text, null);
    }

    public static void i(String tag, String text, Throwable th) {
        if (!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(text)) {
            if (th == null) {
                Log.i(tag, text);
            } else {
                Log.i(tag, text, th);
            }
        }
    }

    public static void e(String tag, String text) {
        e(tag, text, null);
    }

    public static void e(String tag, String text, Throwable th) {
        if (!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(text)) {
            if (th == null) {
                Log.e(tag, text);
            } else {
                Log.e(tag, text, th);
            }
        }
    }
}