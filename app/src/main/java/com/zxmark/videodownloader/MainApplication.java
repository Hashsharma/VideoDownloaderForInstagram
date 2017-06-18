package com.zxmark.videodownloader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.zxmark.videodownloader.service.TLRequestParserService;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PreferenceUtils;

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
        LogUtil.e("app","currentLanguage:" + currentLanguage);
        if (TextUtils.isEmpty(currentLanguage)) {

        } else {
            Resources resources = getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();
            String array[] = currentLanguage.split("-");
            config.locale = array.length == 1 ? new Locale(array[0],"") : new Locale(array[0], array[1]);
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
        LogUtil.e("config","onConfigurationChanged:" + newConfig);
    }


}
