package com.zxmark.videodownloader.downloader;

import android.os.Looper;
import android.provider.MediaStore;

import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.URLMatcher;

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
    public String request(String url) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("video download cannt start from main thread");
        }

        String handledUrl = URLMatcher.getHttpURL(url);

        BaseDownloader downloader = getSpecDownloader(handledUrl);

        if (downloader == null) {
            LogUtil.v("fan", "The tools dont support downloading this video");
        }


        String fileUrl = downloader.getDownloadFileUrl(url);

        if (fileUrl != null) {
            LogUtil.v("TL", "start download :" + fileUrl);


        }

        return fileUrl;
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

        return null;
    }


}
