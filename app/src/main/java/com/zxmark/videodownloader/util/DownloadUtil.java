package com.zxmark.videodownloader.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.MainThread;
import android.text.TextUtils;

import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.floatview.FloatViewManager;
import com.zxmark.videodownloader.main.ImageGalleryActivity;
import com.zxmark.videodownloader.main.VideoPlayActivity;
import com.zxmark.videodownloader.service.DownloadService;

import java.io.File;

/**
 * Created by fanlitao on 17/6/7.
 */

public class DownloadUtil {


    public static void startDownload(String url) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_DOWNLOAD_VIDEO_ACTION);
        intent.putExtra(Globals.EXTRAS, url);
        context.startService(intent);
    }

    public static void startRequest(String pageUrl) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, true);
        intent.putExtra(Globals.EXTRAS, pageUrl);
        context.startService(intent);
    }

    public static void downloadThumbnail(String downloadUrl) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.DOWNLOAD_ACTION);
        intent.putExtra(Globals.EXTRAS, downloadUrl);
        context.startService(intent);
    }


    public static File getHomeDirectory() {
        File targetDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DownloadService.DIR);
        return targetDir;
    }

    public static void openVideo(String filePath) {

        Intent intent = new Intent();
        if (filePath.endsWith("mp4")) {
            intent.setClass(MainApplication.getInstance().getApplicationContext(), VideoPlayActivity.class);
            intent.putExtra(Globals.EXTRAS,filePath);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "video/mp4");
        } else {
            intent.setClass(MainApplication.getInstance().getApplicationContext(), ImageGalleryActivity.class);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
        }
        intent.putExtra(Globals.EXTRAS,filePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.getInstance().startActivity(intent);
    }


    public static String getDownloadTargetInfo(String url) {
        File targetDir = DownloadUtil.getHomeDirectory();

        if (targetDir.exists()) {
            return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
        }
        targetDir.mkdir();
        return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
    }

    public static String getFileNameByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        final int lastIndex = url.lastIndexOf("/");
        return url.substring(lastIndex + 1);
    }

    public static void showFloatView() {
        if (!ActivityManagerUtils.isTopActivity(MainApplication.getInstance().getApplicationContext())) {
            FloatViewManager manager = FloatViewManager.getDefault();
            manager.showFloatView();
        }
    }

}
