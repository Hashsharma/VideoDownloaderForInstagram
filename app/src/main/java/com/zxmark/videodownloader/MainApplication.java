package com.zxmark.videodownloader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.zxmark.videodownloader.service.TLRequestParserService;

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
        init();
    }

    private void init() {
        Intent intent = new Intent(this, TLRequestParserService.class);
        startService(intent);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;
    }

    public static MainApplication getInstance() {
        return sApplication;
    }
}
