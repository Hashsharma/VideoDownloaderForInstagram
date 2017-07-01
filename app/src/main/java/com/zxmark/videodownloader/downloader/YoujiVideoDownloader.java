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

public class YoujiVideoDownloader extends BaseDownloader {


    private String httpSchema = null;

    public String getVideoUrl(String content) {
        String regex;
        String videoUrl = null;
        regex = "<source src=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            videoUrl = ma.group(1);
            if (!videoUrl.startsWith("http")) {
                videoUrl = httpSchema + videoUrl;
            }

            videoUrl = Utils.replaceEscapteSequence(videoUrl);
            LogUtil.e("you", "videoUrl=" + videoUrl);
        }

        return videoUrl;
    }

    public String getPageTitle(String content) {

        String regex;
        String videoUrl = null;
        regex = "<title>(.*?)</title>";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            videoUrl = ma.group(1);
            LogUtil.e("you", "pageTitle=" + videoUrl);
        }

        return videoUrl;
    }

    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        httpSchema = getHttpSchema(htmlUrl);
        String content = startRequest(htmlUrl);
        LogUtil.e("you", "content=" + content);
        if (!TextUtils.isEmpty(content)) {
            String videoUrl = getVideoUrl(content);
            if (!TextUtils.isEmpty(videoUrl)) {
                DownloadContentItem item = new DownloadContentItem();
                item.pageURL = htmlUrl;
                item.pageTitle = getPageTitle(content);
                item.addVideo(videoUrl);
                return item;
            }
        }

        return null;
    }

    private String getHttpSchema(String htmlURL) {
        if (htmlURL.contains("://")) {
            return htmlURL.split("://")[0] + ":";
        }
        if (htmlURL.startsWith("https:")) {
            return "https:";
        }

        if (htmlURL.startsWith("http:")) {
            return "http:";
        }

        return "http:";
    }
}
