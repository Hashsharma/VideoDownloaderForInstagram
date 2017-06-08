package com.zxmark.videodownloader.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.MainThread;

import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.service.DownloadService;

import java.io.File;

/**
 * Created by fanlitao on 17/6/7.
 */

public class DownloadUtil {


    public static void startDownload(String url) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.DOWNLOAD_ACTION);
        intent.putExtra(DownloadService.DOWNLOAD_URL, url);
        context.startService(intent);
    }

    public static void startRequest(String pageUrl) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(Globals.EXTRAS, pageUrl);
        context.startService(intent);
    }


    public static File getHomeDirectory() {
        File targetDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DownloadService.DIR);
        return targetDir;
    }

    public static void openVideo(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(file.getName().endsWith("mp4")) {
            intent.setDataAndType(Uri.fromFile(file), "video/mp4");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "image/*");
        }
        MainApplication.getInstance().startActivity(intent);
    }
}
