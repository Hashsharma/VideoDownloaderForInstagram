package com.zxmark.videodownloader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.duapps.ad.base.DuAdNetwork;
import com.facebook.ads.AdSettings;
import com.imobapp.videodownloaderforinstagram.BuildConfig;
import com.zxmark.videodownloader.service.TLRequestParserService;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PreferenceUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by fanlitao on 17/6/7.
 */

public class MainApplication extends Application {


    private static Context sContext;
    private static MainApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        DuAdNetwork.init(this,getConfigJSON(this));
        sApplication = this;
        initDefaultLocale();
        init();
    }

    private void init() {
        Intent intent = new Intent(this, TLRequestParserService.class);
        startService(intent);
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

    /*** 从assets中读取txt*/
    private String getConfigJSON(Context context) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bis = new BufferedInputStream(context.getAssets().open("ad.json"));
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
