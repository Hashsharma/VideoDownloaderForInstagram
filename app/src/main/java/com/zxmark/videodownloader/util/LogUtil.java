package com.zxmark.videodownloader.util;

import android.util.Log;

import com.imobapp.videodownloaderforinstagram.BuildConfig;

/**
 * Created by fanlitao on 17/6/8.
 */

public class LogUtil {


    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static void v(String tag, String message) {
        if (DEBUG) {
            Log.v(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }
}
