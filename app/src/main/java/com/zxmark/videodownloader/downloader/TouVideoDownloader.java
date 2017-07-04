package com.zxmark.videodownloader.downloader;

import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.Utils;

/**
 * Created by fanlitao on 7/4/17.
 */

public class TouVideoDownloader extends BaseDownloader {


    @Override
    public String getVideoUrl(String content) {
        return null;
    }

    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        LogUtil.e("tou","startSpideThePaage:" + htmlUrl);
        String content = startRequest(htmlUrl);
        LogUtil.e("tou","content=" + content);
        Utils.writeFile(content);
        return null;
    }


}
