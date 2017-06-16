/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.zxmark.videodownloader.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.zxmark.videodownloader.MainApplication;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetWorkUtil {
    /**
     * @param context
     * @return
     * @author 获取当前的网络状态 -1：没有网络 1：WIFI网络2：wap网络3：net网络
     */
    public static int getAPNType(Context context) {
        int netType = -1;
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null) {
                return netType;
            }
            int nType = networkInfo.getType();
            if (nType == ConnectivityManager.TYPE_MOBILE) {
                if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
                    netType = 3;
                } else {
                    netType = 2;
                }
            } else if (nType == ConnectivityManager.TYPE_WIFI) {
                netType = 1;
            }
        } catch (Exception e) {
            return netType;
        }

        return netType;
    }

    /**
     * 判断WiFi网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null && mWiFiNetworkInfo.isConnected()) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断数据流量是否可用
     *
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null && mMobileNetworkInfo.isConnected()) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断是否有网络
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return networkINfo.isAvailable();

        }
        return false;
    }

    public static boolean is3G(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            if (networkINfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS || networkINfo.getSubtype() ==
                    TelephonyManager.NETWORK_TYPE_EDGE || networkINfo.getType() == TelephonyManager
                    .NETWORK_TYPE_CDMA) {
                return false;
            }
            return networkINfo.isAvailable();
        }
        return false;
    }

    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager mgrConn = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return ((mgrConn.getActiveNetworkInfo() != null && mgrConn
                .getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel
                .getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断是否可以请求网络
     * WIFI 4G 3G 2G
     *
     * @return
     */
    public static boolean canExecuteRequest() {
        final Context context = MainApplication.getInstance().getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null) {
            if (networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return networkINfo.isAvailable();
            }

            if (networkINfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (networkINfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS || networkINfo.getSubtype() ==
                        TelephonyManager.NETWORK_TYPE_EDGE || networkINfo.getType() == TelephonyManager
                        .NETWORK_TYPE_CDMA) {
                    return false;
                } else {
                    return networkINfo.isAvailable();
                }
            }
        }

        return false;
    }

    /**
     * 3G
     * WIFI
     *
     * @return
     */
    public static String getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) MainApplication.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return "WIFI";

        } else {
            if (networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (networkINfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS || networkINfo.getSubtype() ==
                        TelephonyManager.NETWORK_TYPE_EDGE || networkINfo.getType() == TelephonyManager
                        .NETWORK_TYPE_CDMA) {
                    return "2G";
                }
                return "3G";
            }
        }

        return "unknow";
    }

    public static final String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        LogUtil.v("fan12", "hostAddress:" + inetAddress.getHostAddress());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}
