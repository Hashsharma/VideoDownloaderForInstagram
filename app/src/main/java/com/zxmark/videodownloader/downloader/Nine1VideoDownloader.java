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

public class Nine1VideoDownloader extends BaseDownloader {


    public String getVideoUrl(String content) {
        String regex;
        String videoUrl = null;
        regex = "<source src=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            videoUrl = ma.group(1);
            LogUtil.e("91", "videoUrl=" + videoUrl);
        }

        return videoUrl;
    }

    public String getThumbnailURL(String content) {
        String regex;
        String videoUrl = null;
        regex = "<video.*poster=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        LogUtil.e("91", "getThumbnailURL=" + ma);
        if (ma.find()) {
            videoUrl = ma.group(1);
            LogUtil.e("91", "getThumbnailURL=" + videoUrl);
        }

        return videoUrl;
    }

    public String getPageTitle(String content) {
        String regex;
        String pageTitle = null;
        regex = "<meta name=\"title\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        if (ma.find()) {
            pageTitle = ma.group(1);
        }

        return pageTitle;
    }


    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        String content = startRequest(htmlUrl);
        Utils.writeFile(content);
        DownloadContentItem downloadContentItem = new DownloadContentItem();
        downloadContentItem.pageURL = htmlUrl;
        final String videoURL = getVideoUrl(content);
        if(!TextUtils.isEmpty(videoURL)) {
            downloadContentItem.addVideo(videoURL);
            downloadContentItem.pageThumb = getThumbnailURL(content);
            downloadContentItem.pageTitle = getPageTitle(content);
        }

        return downloadContentItem;
    }

}
