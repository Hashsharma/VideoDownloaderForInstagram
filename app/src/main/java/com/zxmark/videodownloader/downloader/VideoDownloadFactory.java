package com.zxmark.videodownloader.downloader;

import android.os.Looper;
import android.provider.MediaStore;

import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.URLMatcher;
import com.zxmark.videodownloader.util.Utils;

/**
 * Created by fanlitao on 17/6/8.
 */

public final class VideoDownloadFactory {


    private BaseDownloader mDownloader;


    private static VideoDownloadFactory sInstance = new VideoDownloadFactory();


    public static VideoDownloadFactory getInstance() {
        return sInstance;
    }

    /**
     * 下载视频工厂入口
     *
     * @param url
     */
    public DownloadContentItem request(String url) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("video download cannt start from main thread");
        }

        String handledUrl = URLMatcher.getHttpURL(url);

        BaseDownloader downloader = getSpecDownloader(handledUrl);

        if (downloader == null) {
            LogUtil.v("fan", "The tools dont support downloading this video");
        }

        if (downloader != null) {
            return downloader.startSpideThePage(url);
        }

        return null;
    }


    private BaseDownloader getSpecDownloader(String url) {
        BaseDownloader downloader;

        if (url.contains("www.instagram.com")) {
            //TODO:匹配instagram的视频下载器
            return new InstagramDownloader();
        }
        //返回tumblr视频下载器
        if (url.contains(".tumblr.")) {
            return new TumblrVideoDownloader();
        }
        //快手视频下载器
        if (url.contains("www.gifshow.com") || url.contains("www.kwai.com")) {
            return new KuaiVideoDownloader();
        }

        if (url.contains(Utils.HOST_FACEBOOK)) {
            return new FacebookDownloader();
        }

        if (url.contains(Utils.HOST_91)) {
            return new Nine1VideoDownloader();
        }

        if (url.contains(Utils.HOST_XVIDEOS)) {
            return new XVideosDownloader();
        }

        if (url.contains(Utils.HOST_YOUJI)) {
            return new YoujiVideoDownloader();
        }

        if (url.contains(Utils.HOST_YG)) {
            return new TouVideoDownloader();
        }

        return null;
    }

    public boolean isSupportWeb(String url) {
        if (url.contains("www.instagram.com")) {
            return true;
        }
        //返回tumblr视频下载器
        if (url.contains(".tumblr.")) {
            return true;
        }
        //快手视频下载器
        if (url.contains("www.gifshow.com") || url.contains("www.kwai.com")) {
            return true;
        }

        for (String hostKey : Utils.EXPIRE_SUFFIX_ARRAY) {
            if (url.contains(hostKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean needShowPasteBtn() {
        String normalURL = Utils.getTextFromClipboard();
        LogUtil.e("main", "needShowPasteBtn====" + normalURL);
        if (DownloaderDBHelper.SINGLETON.isExistPageURL(normalURL)) {
            return false;
        }
        return VideoDownloadFactory.getInstance().isSupportWeb(normalURL);
    }


}
