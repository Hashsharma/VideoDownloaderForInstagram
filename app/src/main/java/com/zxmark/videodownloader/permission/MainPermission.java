package com.zxmark.videodownloader.permission;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;

import java.lang.reflect.Method;

/**
 */
public abstract class MainPermission {

    private static MainPermission sPermission;
    private static StandardPermission sStandardPermission;

    public static MainPermission getPermisson() {
        if (sPermission == null) {
            if (RomUtils.isNonsupport()) {
                sPermission = getStandardPermission();
            } else {
                if (RomUtils.checkIsHuaweiRom()) {
                    sPermission = new HuaweiPermission();
                } else if (RomUtils.checkIsMeizuRom()) {
                    sPermission = new MeizuPermission();
                } else if (RomUtils.checkIsMiuiRom()) {
                    sPermission = new MiUIPermission();
                } else if (RomUtils.checkIs360Rom()) {
                    sPermission = new QikuPermission();
                } else {
                    sPermission = getStandardPermission();
                }
            }
        }
        return sPermission;
    }

    public static StandardPermission getStandardPermission() {
        if (sStandardPermission == null) {
            sStandardPermission = new StandardPermission();
        }
        return sStandardPermission;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public abstract boolean checkFloatWindowPermission(Context context);

    public abstract void applyFloatWindowPermission(Context context);

    public abstract boolean checkCallLogPermission(Context context);

    public abstract void applyCallLogPermission(Context context);

    public abstract boolean checkContactsPermission(Context context);

    public abstract void applyContactsPermission(Context context);

    public abstract boolean checkSMSPermission(Context context);

    public abstract void applySMSPermission(Context context);

}
