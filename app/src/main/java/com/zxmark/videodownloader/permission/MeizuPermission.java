package com.zxmark.videodownloader.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class MeizuPermission extends MainPermission {

    public static final String TAG = MeizuPermission.class.getSimpleName();

    @Override
    public boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }

    @Override
    public boolean checkContactsPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            boolean isHasRead = checkOp(context, 4);
            boolean isHasWrite = checkOp(context, 5);
            return isHasRead && isHasWrite;
        } else {
            Log.e(TAG, "Below API 19 cannot invoke!");
        }
        return true;
    }

    @Override
    public void applyContactsPermission(Context context) {
        applyFloatWindowPermission(context);
    }

    @Override
    public boolean checkSMSPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 14);
        }
        return true;
    }

    @Override
    public void applySMSPermission(Context context) {
        applyCallLogPermission(context);
    }

    @Override
    public boolean checkCallLogPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            boolean isHasRead = checkOp(context, 6);
            boolean isHasWrite = checkOp(context, 7);
            return isHasRead && isHasWrite;
        } else {
            Log.e(TAG, "Below API 19 cannot invoke!");
        }
        return true;
    }

    @Override
    public void applyCallLogPermission(Context context) {
        applyFloatWindowPermission(context);
    }

    @Override
    public void applyFloatWindowPermission(Context context) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
            intent.putExtra("packageName", context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings");
                intent.putExtra("packageName", context.getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
