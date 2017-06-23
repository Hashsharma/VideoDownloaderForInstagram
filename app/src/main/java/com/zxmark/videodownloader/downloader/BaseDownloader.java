package com.zxmark.videodownloader.downloader;

import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DownloadContentItem;

/**
 * Created by fanlitao on 17/6/8.
 */

public abstract class BaseDownloader {

    protected abstract String startRequest(String htmlUrl);
    public abstract String getVideoUrl(String content) ;
    public abstract DownloadContentItem startSpideThePage(String htmlUrl);
}
