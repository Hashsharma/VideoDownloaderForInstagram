package com.zxmark.videodownloader.util;

import android.content.ClipboardManager;
import android.content.Context;

import com.zxmark.videodownloader.MainApplication;

/**
 * Created by fanlitao on 6/16/17.
 */

public class ClipboardHelper {


    public static String getCopyContent() {
        final ClipboardManager cb = (ClipboardManager) MainApplication.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        return cb.getText().toString();
    }
}
