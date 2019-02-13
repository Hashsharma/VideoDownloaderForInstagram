package com.zxmark.videodownloader.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;

import com.zxmark.videodownloader.util.LogUtil;

import java.lang.reflect.Method;

/**
 * Created by liyanju on 2016/12/12.
 */
public class StandardPermission extends MainPermission {

    public static final String TAG = StandardPermission.class.getSimpleName();

    private static final String[] READ_CALLLOG = {
            Manifest.permission.READ_CALL_LOG
    };

    private static final String[] WRITE_CALLLOG = {
            Manifest.permission.WRITE_CALL_LOG
    };

    private static final String[] READ_CONTACTS = {
            Manifest.permission.READ_CONTACTS
    };

    private static final String[] WRITE_CONTACTS = {
            Manifest.permission.WRITE_CONTACTS
    };

    private static final String[] SMS = {
            Manifest.permission.READ_SMS,
    };

    @Override
    public boolean checkContactsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method method = clazz.getDeclaredMethod("isCallingPackageAllowedToPerformAppOpsProtectedOperation", Context.class,
                        int.class, String.class, boolean.class, int.class, String[].class, boolean.class);
                boolean isHasRead = (Boolean) method.invoke(null, context, Binder.getCallingUid(),
                        context.getPackageName(), true, 4, READ_CONTACTS, false);
                boolean isHasWrite = (Boolean) method.invoke(null, context, Binder.getCallingUid(),
                        context.getPackageName(), true, 5, WRITE_CONTACTS, false);
                return isHasRead && isHasWrite;
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getCause() instanceof SecurityException) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void applyContactsPermission(Context context) {
        applyCallLogPermission(context);
    }

    @Override
    public boolean checkSMSPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method method = clazz.getDeclaredMethod("isCallingPackageAllowedToPerformAppOpsProtectedOperation", Context.class,
                        int.class, String.class, boolean.class, int.class, String[].class, boolean.class);
                boolean isHasRead = (Boolean) method.invoke(null, context, Binder.getCallingUid(),
                        context.getPackageName(), true, 14, SMS, false);
                return isHasRead;
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getCause() instanceof SecurityException) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void applySMSPermission(Context context) {
        applyCallLogPermission(context);
    }

    @Override
    public boolean checkCallLogPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method method = clazz.getDeclaredMethod("isCallingPackageAllowedToPerformAppOpsProtectedOperation", Context.class,
                        int.class, String.class, boolean.class, int.class, String[].class, boolean.class);
                boolean isHasRead = (Boolean) method.invoke(null, context, Binder.getCallingUid(),
                        context.getPackageName(), true, 6, WRITE_CALLLOG, false);
                boolean isHasWrite = (Boolean) method.invoke(null, context, Binder.getCallingUid(),
                        context.getPackageName(), true, 7, READ_CALLLOG, false);
                return isHasRead && isHasWrite;
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getCause() instanceof SecurityException) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void applyCallLogPermission(Context context) {
        if (context instanceof Activity) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            ((Activity) context).startActivityForResult(intent, 1);
        }
    }

    @Override
    public boolean checkFloatWindowPermission(Context context) {
        Boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            try {

                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                result = (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogUtil.e("float","checkFloatWindowPermission:" + result);
        return result;
    }

    @Override
    public void applyFloatWindowPermission(Context context) {
        if (context instanceof Activity) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ((Activity) context).startActivityForResult(intent, 1);
        }
    }
}
