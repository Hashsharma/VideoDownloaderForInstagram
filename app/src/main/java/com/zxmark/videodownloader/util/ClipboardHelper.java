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

    public static synchronized String getClipboardLabel(Context context) {
        String str;
        try {
            CharSequence label = ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).
                    getPrimaryClip().getDescription().getLabel();
            if (label == null) {
                str = "";
            } else {
                str = label.toString();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
        return str;
    }

}
