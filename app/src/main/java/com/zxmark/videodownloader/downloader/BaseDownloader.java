package com.zxmark.videodownloader.downloader;

/**
 * Created by fanlitao on 17/6/8.
 */

public abstract class BaseDownloader {


    public abstract String startRequest(String htmlUrl);
    public abstract String getVideoUrl(String content) ;
    public abstract String getDownloadFileUrl(String htmlUrl);
}
