package com.zxmark.videodownloader.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.MainThread;
import android.text.TextUtils;

import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.floatview.FloatViewManager;
import com.zxmark.videodownloader.main.GalleryPagerActivity;
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
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void startResumeDownload(String url) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, false);
        intent.putExtra(Globals.EXTRAS, url);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void startForceDownload(String pageURL) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, false);
        intent.putExtra(DownloadService.EXTRAS_FORCE_DOWNLOAD,true);
        intent.putExtra(Globals.EXTRAS, pageURL);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void startRequest(String pageUrl) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, true);
        intent.putExtra(Globals.EXTRAS, pageUrl);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void downloadThumbnail(String pageURL, String downloadUrl) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.DOWNLOAD_ACTION);
        intent.putExtra(DownloadService.DOWNLOAD_PAGE_URL, pageURL);
        intent.putExtra(Globals.EXTRAS, downloadUrl);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }


    public static File getHomeDirectory() {
        File targetDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DownloadService.DIR);
        return targetDir;
    }

    public static void openVideo(String filePath) {
        Intent intent = new Intent();
        if (filePath.endsWith("mp4") || filePath.endsWith("mov")) {
            intent.setClass(MainApplication.getInstance().getApplicationContext(), VideoPlayActivity.class);
            intent.putExtra(Globals.EXTRAS, filePath);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), MimeTypeUtil.getMimeTypeByFileName(filePath));
        } else {
            intent.setClass(MainApplication.getInstance().getApplicationContext(), ImageGalleryActivity.class);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
        }
        intent.putExtra(Globals.EXTRAS, filePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.getInstance().startActivity(intent);
    }

    public static void openFileList(String fileDirectory) {
        LogUtil.e("open", "openFileLIst:" + fileDirectory);
        Intent intent = new Intent(MainApplication.getInstance().getApplicationContext(), GalleryPagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Globals.EXTRAS, fileDirectory);
        MainApplication.getInstance().getApplicationContext().startActivity(intent);
    }


    public static String getDownloadTargetInfo(String url) {
        File targetDir = DownloadUtil.getHomeDirectory();

        if (targetDir.exists()) {
            return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
        }
        targetDir.mkdir();
        return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
    }

    public static void checkDownloadBaseHomeDirectory() {
        if(!DownloadUtil.getHomeDirectory().exists()) {
            DownloadUtil.getHomeDirectory().mkdir();
        }
    }

    /**
     * 组成文件下载目录
     *
     * @param parent
     * @param fileName
     * @return
     */
    public static String getDownloadTargetDir(String parent, String fileName) {
        File targetDir = new File(parent, fileName);
        return targetDir.getAbsolutePath();
    }

    public static String getDownloadItemDirectory(boolean create) {
        File homeDirectory = DownloadUtil.getHomeDirectory();
        if (create) {
            if (!homeDirectory.exists()) {
                homeDirectory.mkdir();
            }
        }
        File itemDirectory = new File(homeDirectory, String.valueOf(System.currentTimeMillis()));
        if (create) {
            if (!itemDirectory.exists()) {
                itemDirectory.mkdir();
            }
        }
        return itemDirectory.getAbsolutePath();
    }

    public static String getFileNameByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String array[] = url.split("/");
        return array[array.length - 1];
    }

    public static void showFloatView() {
        FloatViewManager manager = FloatViewManager.getDefault();
        manager.showFloatView();
    }

}
