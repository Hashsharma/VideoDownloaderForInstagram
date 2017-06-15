package com.zxmark.videodownloader.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zxmark.videodownloader.downloader.BaseDownloader;
import com.zxmark.videodownloader.downloader.InstagramDownloader;
import com.zxmark.videodownloader.downloader.KuaiVideoDownloader;
import com.zxmark.videodownloader.downloader.TumblrVideoDownloader;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;

/**
 * Created by fanlitao on 17/6/7.
 */

public class TLRequestParserService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        final ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cb.setPrimaryClip(ClipData.newPlainText("", ""));
        cb.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

            @Override
            public void onPrimaryClipChanged() {
                LogUtil.v("fan3", "onPrimaryClipChanged:" + cb.getText());
                DownloadUtil.startRequest(cb.getText().toString());
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}