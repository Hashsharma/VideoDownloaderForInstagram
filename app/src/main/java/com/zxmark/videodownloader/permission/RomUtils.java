package com.zxmark.videodownloader.permission;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * Created by liyanju on 2016/12/12.
 */
public class RomUtils {
    private static final String TAG = "RomUtils";

    /**
     * 获取 emui 版本号
     *
     * @return
     */
    public static double getEmuiVersion() {
        try {
            String emuiVersion = getSystemProperty("ro.build.version.emui");
            String version = emuiVersion.substring(emuiVersion.indexOf("_") + 1);
            return Double.parseDouble(version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4.0;
    }


    public static String getSystemProperty(String propName) {
        String line = "";
        BufferedReader input = null;
        Process process = null;
        BufferedReader error = null;
        try {
            process = Runtime.getRuntime().exec("getprop " + propName);
            // TODO: 16/12/26 增加错误流处理
            error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            input = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            line = input.readLine();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop " + propName, ex);
            return null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (error != null) {
                    error.close();
                }
                // TODO: 16/12/26 销毁进程
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception while closing InputStream", e);
            }
        }
        return line;
    }

    public static boolean checkIsHuaweiRom() {
        return Build.MANUFACTURER.contains("HUAWEI");
    }

    /**
     * check if is miui ROM
     */
    public static boolean checkIsMiuiRom() {
        if (!Build.MANUFACTURER.contains("Xiaomi")) {
            return false;
        }
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static boolean checkIsMeizuRom() {
        try {
            // Invoke Build.hasSmartBar()
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }

        //return Build.MANUFACTURER.contains("Meizu");
//        String meizuFlymeOSFlag = getSystemProperty("ro.build.display.id");
//        if (TextUtils.isEmpty(meizuFlymeOSFlag)) {
//            return false;
//        } else if (meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains("flyme")) {
//            return true;
//        } else {
//            return false;
//        }
    }

    public static boolean checkIs360Rom() {
        return Build.MANUFACTURER.contains("QiKU");
    }

    public static boolean checkIsSamsung() {
        return Build.MANUFACTURER.contains("samsung");
    }

    public static boolean checkIsLGE() {
        return Build.MANUFACTURER.contains("LGE");
    }

    public static boolean isNonsupport() {
        if (Build.MANUFACTURER.contains("LENOVO") || Build.MANUFACTURER.contains("LGE")) {
            return true;
        }
        return false;
    }

    public static boolean ischeck() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

}
