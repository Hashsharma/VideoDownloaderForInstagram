package com.zxmark.videodownloader.permission;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.util.Log;


import java.lang.reflect.Method;

public class QikuPermission extends MainPermission {

    public static final String TAG = QikuPermission.class.getSimpleName();

    @Override
    public boolean checkContactsPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return MainPermission.getStandardPermission().checkContactsPermission(context);
        } else if (version >= 19) {
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
        if (version >= 23) {
            return MainPermission.getStandardPermission().checkCallLogPermission(context);
        } else if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);

                boolean isHasRead = AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, 6, Binder.getCallingUid(), context.getPackageName());
                boolean isHasWrite = AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, 7, Binder.getCallingUid(), context.getPackageName());
                return isHasRead && isHasWrite;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    public boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return MainPermission.getStandardPermission().checkFloatWindowPermission(context);
        } else if (version >= 19) {
            return checkOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }

    @Override
    public void applyFloatWindowPermission(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$OverlaySettingsActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
