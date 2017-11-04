package com.zxmark.videodownloader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zxmark.videodownloader.util.EventUtil;
import com.zxmark.videodownloader.util.LogUtil;

/**
 * Created by fanlitao on 9/18/17.
 */

public class GaTrackerReceiver extends BroadcastReceiver {


    public static final String TAG = GaTrackerReceiver.class.getSimpleName();
    private static final String EXTRA_REFERRER = "referrer";

    private static String lastReferrer;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String referrerValue = intent.getStringExtra(EXTRA_REFERRER);
            LogUtil.v(TAG, "referrerValue=" + referrerValue);
            if (referrerValue == null || !referrerValue.equals(lastReferrer)) {
                lastReferrer = referrerValue;
                EventUtil.getDefault().onEvent("referer", referrerValue);
            }
        }
    }
}
