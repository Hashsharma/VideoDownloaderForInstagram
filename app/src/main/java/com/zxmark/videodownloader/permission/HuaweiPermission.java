package com.zxmark.videodownloader.permission;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by liyanju on 2016/12/12.
 */
public class HuaweiPermission extends MainPermission {

    public static final String TAG = HuaweiPermission.class.getSimpleName();

    @Override
    public boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                return (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (version >= 19) {
            return checkOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }

    @Override
    public boolean checkCallLogPermission(Context context) {
        return true;
    }

    @Override
    public void applyCallLogPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//悬浮窗管理页面
            intent.setComponent(comp);
            intent.setAction("huawei.intent.action.HSM_PERMISSION_MANAGER");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        applyCallLogPermission(context);
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
    public void applyFloatWindowPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");//悬浮窗管理页面
            intent.setComponent(comp);
            if (RomUtils.getEmuiVersion() == 3.1) {
                //emui 3.1 的适配
                context.startActivity(intent);
            } else {
                //emui 3.0 的适配
                comp = new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity");//悬浮窗管理页面
                intent.setComponent(comp);
                context.startActivity(intent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理，跳转到本app的权限管理页面,这个需要华为接口权限，未解决
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            /**
             * 手机管家版本较低 HUAWEI SC-UL10
             */
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.Android.settings", "com.android.settings.permission.TabItem");//权限管理页面 android4.4
            intent.setComponent(comp);
            context.startActivity(intent);
            e.printStackTrace();
        } catch (Exception e) {
            //抛出异常时提示信息
            e.printStackTrace();
        }
    }
}
