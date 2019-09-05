package com.zxmark.videodownloader;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.data.api.SdkManager;
import com.imobapp.videodownloaderforinstagram.R;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.zxmark.videodownloader.service.TLRequestParserService;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PreferenceUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Proxy;
import java.util.Locale;

/**
 * Created by fanlitao on 17/6/7.
 */

public class MainApplication extends MultiDexApplication {

    private static Context sContext;
    private static MainApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        FileDownloader.setupOnApplicationOnCreate(this);

        createNotificationChannels();
        initDefaultLocale();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel("download-notification", getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH));
        }
    }

    private void initFileDownloader() {
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                        .proxy(Proxy.NO_PROXY) // set proxy
                ))
                .commit();

    }


    private void initDefaultLocale() {

        String currentLanguage = PreferenceUtils.getCurrentLanguage();
        LogUtil.e("app", "currentLanguage:" + currentLanguage);
        if (TextUtils.isEmpty(currentLanguage)) {

        } else {
            Resources resources = getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();
            String array[] = currentLanguage.split("-");
            config.locale = array.length == 1 ? new Locale(array[0], "") : new Locale(array[0], array[1]);
            resources.updateConfiguration(config, dm);
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;
    }

    public static MainApplication getInstance() {
        return sApplication;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.e("config", "onConfigurationChanged:" + newConfig);
    }

    private static String TOOLBOX_AD_CONFIG = "dxtoolbox.json";

    private String getConfigJSON(Context context) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bis = new BufferedInputStream(context.getAssets().open(
                    TOOLBOX_AD_CONFIG));
            byte[] buffer = new byte[4096];
            int readLen = -1;
            while ((readLen = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, readLen);
            }
        } catch (IOException e) {
            Log.e("", "IOException :" + e.getMessage());
        } finally {
            closeQuietly(bis);
        }

        return bos.toString();
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            // empty
        }
    }


}
