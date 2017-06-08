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

/**
 * Created by fanlitao on 17/6/7.
 */

public class MarkService extends Service {

    public static final String URL_FORMAT = "http://api.tumblr.com/v2/blog/beyoncescock.tumblr.com/posts?id=%s&api_key=pJQg227oDPuOaNQVHnYKeewBoSr4FjOyIPR1f5dbwCHJZBJZsz";
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(MarkService.this,"VideoDownloader start download for you",Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("fan3","MarkService.onCreate");
       final ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cb.setPrimaryClip(ClipData.newPlainText("", ""));
        cb.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

            @Override
            public void onPrimaryClipChanged() {
// 具体实现
                Log.v("fan3","onPrimaryClipChanged:" + cb.getText());

                startDownload(cb.getText().toString());

            }
        });

    }

    private void startDownload(final String url) {

        if(TextUtils.isEmpty(url)) {
            return;
        }

        if(url.contains("www.instagram.com")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(0);
                    InstagramDownloader downloader = new InstagramDownloader();
                    String downloadUrl = downloader.getDownloadFileUrl(url);
                    if (!TextUtils.isEmpty(downloadUrl)) {
                        DownloadUtil.startDownload(downloadUrl);
                    }
                }
            }).start();
        } else if(url.contains("www.gifshow.com")) {
            Log.v("fan","startDownload kuaishou video");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(0);
                    BaseDownloader downloader = new KuaiVideoDownloader();
                    String downloadUrl = downloader.getDownloadFileUrl(url);
                    Log.v("fan5","kuaishou.videoUrl:" + downloadUrl);
                    if (!TextUtils.isEmpty(downloadUrl)) {
                        DownloadUtil.startDownload(downloadUrl);
                    }
                }
            }).start();
        } else {
            Log.v("fan","startDownload tumblr video");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(0);
                    BaseDownloader downloader = new TumblrVideoDownloader();
                    getTumblrPostId(url);
                    String targetUrl = String.format(URL_FORMAT,getTumblrPostId(url));
                    Log.v("fan5","tumblr.content:" + targetUrl);
                   // String downloadUrl = downloader.startRequest(url);
//                    if (!TextUtils.isEmpty(downloadUrl)) {
//                        DownloadUtil.startDownload(downloadUrl);
//                    }
                }
            }).start();
        }

    }

    public String getTumblrPostId(String url) {
       return url.substring(url.lastIndexOf("/"));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
