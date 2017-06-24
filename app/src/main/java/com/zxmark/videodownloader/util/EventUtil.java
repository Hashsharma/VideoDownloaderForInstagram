package com.zxmark.videodownloader.util;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.zxmark.videodownloader.MainApplication;

/**
 * Created by fanlitao on 6/24/17.
 */

public class EventUtil {

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final EventUtil sInstance = new EventUtil();

    public static EventUtil getDefault() {
        return sInstance;
    }

    private EventUtil() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MainApplication.getInstance().getApplicationContext());
    }

    public void onEvent(String key,String tag) {
        Bundle params = new Bundle();
        params.putString("tag", tag);
        mFirebaseAnalytics.logEvent(key, params);
    }
}
