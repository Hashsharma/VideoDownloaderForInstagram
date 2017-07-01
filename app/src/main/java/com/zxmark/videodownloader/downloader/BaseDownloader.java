package com.zxmark.videodownloader.downloader;

import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.spider.HttpRequestSpider;

/**
 * Created by fanlitao on 17/6/8.
 */

public abstract class BaseDownloader {

    protected String startRequest(String htmlUrl) {
        return HttpRequestSpider.getInstance().request(htmlUrl);
    }

    public abstract String getVideoUrl(String content);

    public abstract DownloadContentItem startSpideThePage(String htmlUrl);
}
