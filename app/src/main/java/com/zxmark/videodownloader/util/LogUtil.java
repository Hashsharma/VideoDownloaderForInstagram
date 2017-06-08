package com.zxmark.videodownloader.util;

import android.util.Log;

/**
 * Created by fanlitao on 17/6/8.
 */

public class LogUtil {


    public static void v(String tag, String message) {
        Log.v(tag, message);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }
}
