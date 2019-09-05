package com.zxmark.videodownloader.downloader;

import android.text.TextUtils;

import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 7/1/17.
 */

public class XVideosDownloader extends BaseDownloader {
    @Override
    public String getVideoUrl(String content) {

        String regex;
        String videoUrl = null;
        regex = "html5player.setVideoUrlHigh\\(\'(.*?)\'\\)";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            videoUrl = ma.group(1);
            LogUtil.e("xv", "videoUrl=" + videoUrl);
        }

        return videoUrl;
    }

    public String getThumbnailByPageURL(String content) {
        String regex;
        String videoUrl = null;
        regex = "html5player.setThumbUrl\\(\'(.*?)\'\\)";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            videoUrl = ma.group(1);
            LogUtil.e("xv", "videoUrl=" + videoUrl);
        }

        return videoUrl;
    }

    public String getPageTitle(String content) {
        //<meta name="description" content="XVIDEOS Pinay tight pussy creampy Full-mangpopoy.com 免費的" />
        String regex;
        String videoUrl = null;
        regex = "<meta name=\"description\" content=\"(.*?)\" />";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            videoUrl = ma.group(1);
        }
        return videoUrl;

    }

    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        LogUtil.e("xv", "url=" + htmlUrl);
        String content = startRequest(htmlUrl);
        if (!TextUtils.isEmpty(content)) {
            DownloadContentItem downloadContentItem = new DownloadContentItem();
            downloadContentItem.pageURL = htmlUrl;
            String video = getVideoUrl(content);

            if (downloadContentItem.getVideoCount() <= 0) {
                downloadContentItem.addVideo(video);
            }
            downloadContentItem.pageThumb = getThumbnailByPageURL(content);
            downloadContentItem.pageTitle = getPageTitle(content);
            downloadContentItem.homeDirectory = "xvideos";
            return downloadContentItem;
        }
        LogUtil.e("xv", "page=" + content);
        // Utils.writeFile(content);

        return null;
    }
}
