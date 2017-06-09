/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.zxmark.videodownloader.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.zxmark.videodownloader.MainApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Created by fanlitao on 16/7/26.
 * <p/>
 * 设备相关接口：
 * 屏幕分辨率
 * 内存
 * 手机型号
 * CPU型号
 */
public final class DeviceUtil {
    private static final String DEFAULT_IMEI = "000000000000000";

    private static Point screenSize;
    private static int sScreenHeight;
    private static int sScreenWidth;
    private static int sStatusBarHeight;
    private static String sPhoneType;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Point getScreenSize() {
        if (null == screenSize) {
            Context context = MainApplication.getInstance()
                    .getApplicationContext();
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            screenSize = new Point();
            if (wm != null) {
                Display display = wm.getDefaultDisplay();
                if (display != null) {
                    if (Build.VERSION.SDK_INT > 12) {
                        display.getSize(screenSize);
                    } else {
                        screenSize.x = display.getWidth();
                        screenSize.y = display.getHeight();
                    }
                }
            }

        }

        return screenSize;
    }

    public static int getScreenWidth() {
        if (sScreenWidth == 0) {
            sScreenWidth = getScreenSize().x;
        }
        return sScreenWidth;
    }

    public static int getScreenHeight() {
        if (sScreenHeight == 0) {
            sScreenHeight = getScreenSize().y;
        }
        return sScreenHeight;
    }

    public static float getScreenDensity() {
        Context context = MainApplication.getInstance()
                .getApplicationContext();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        return density;
    }

    public static String getCarrier(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telManager.getNetworkOperatorName();
    }

    public static String getOSVersion() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public static String getMacAddress(Context context) {
        String macAddress = "000000000000";

        try {
            WifiManager wifiMgr = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifiMgr ? null : wifiMgr
                    .getConnectionInfo());
            if (null != info) {
                if (info.getMacAddress() != null
                        && info.getMacAddress().length() != 0) {
                    macAddress = info.getMacAddress().replace(":", "");
                } else {
                    return macAddress;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return macAddress;
    }

    public static String getIMEI(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = "";
        deviceId = telManager.getDeviceId();

        if (deviceId == null || DEFAULT_IMEI.equalsIgnoreCase(deviceId)) {
            deviceId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (deviceId == null || deviceId.equalsIgnoreCase(DEFAULT_IMEI)) {
                deviceId = DEFAULT_IMEI;
            }
        }

        return deviceId;
    }

    public static String getActiveNetworkType(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return "default";
        }

        NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
        if (activeNetworkInfo != null
                && activeNetworkInfo.isConnectedOrConnecting()) {
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return activeNetworkInfo.getTypeName();
            } else {
                return activeNetworkInfo.getSubtypeName();
            }
        }
        return "default";
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return null != netInfo && netInfo.isAvailable();
    }

    public static String getProduct() {
        return Build.PRODUCT;
    }

    public static String getAppChannel(Context context) {
        // return context.getApplicationInfo().metaData.getString("tn");
        //return Const.Channel;
        return null;
    }

    public static String getLocation(Context context) {
        if (context != null) {
            final Resources resource = context.getResources();
            final Configuration configuration = resource.getConfiguration();
            if (configuration != null) {
                final Locale locale = configuration.locale;
                final String country = locale.getCountry();
                if (!TextUtils.isEmpty(country)) {
                    return country.toLowerCase();
                }
            }
        }

        return "unknow";
    }

    public static String getBaiduChannel() {
        MainApplication application = MainApplication.getInstance();
        PackageManager packageManager = application.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = application.getPackageManager().getApplicationInfo(
                    application.getPackageName(), PackageManager.GET_META_DATA);
            PackageInfo packInfo = packageManager.getPackageInfo(
                    application.getPackageName(), 0);
            return appInfo.metaData.getString("Baidu_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";

    }

    public static String[] getCpuInfo() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""}; // 1-cpu型号 //2-cpu频率
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return cpuInfo;
    }

    /**
     * 判断屏幕是不是灭的
     *
     * @return
     */
    public static boolean isScreenOff() {
        Context context = MainApplication.getInstance().getApplicationContext();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return !pm.isScreenOn();
    }

    public static int getStatusBarHeight(Context ctx) {
        if (sStatusBarHeight == 0) {
            try {
                /**
                 * 通过反射机制获取StatusBar高度
                 */
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object object = clazz.newInstance();
                Field field = clazz.getField("status_bar_height");
                int height = Integer.parseInt(field.get(object).toString());
                /**
                 * 设置StatusBar高度
                 */
                sStatusBarHeight = ctx.getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sStatusBarHeight;

    }

    /**
     * 判断设备是手机
     *
     * @param context
     * @return
     */
    public static boolean isPhone(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    public static String getPhoneType() {
        try {
            if (sPhoneType == null) {
                sPhoneType = Build.MANUFACTURER + "+" + Build.MODEL;
            }
            return sPhoneType;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isSamsungPhone() {
        return "samsung".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isVivoX3t() {
        return "BBK+vivo X3t".equals(getPhoneType());
    }
}
