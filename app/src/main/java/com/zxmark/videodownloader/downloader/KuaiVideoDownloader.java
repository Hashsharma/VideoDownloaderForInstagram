package com.zxmark.videodownloader.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.spider.HttpRequestSpider;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 17/6/8.
 */

public class KuaiVideoDownloader extends BaseDownloader {

    @Override
    public String getVideoUrl(String content) {
        Log.e("fan2","getVideoURL");
        String regex;
        String videoUrl = null;
        regex = "<meta property=\"og:video:url\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            videoUrl = ma.group(1);
        }

        return videoUrl;
    }


    public String getImageUrl(String content) {
        String regex;
        String imageUrl = null;
        regex = "<meta property=\"og:image\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            imageUrl = ma.group(1);
        }

        return imageUrl;
    }


    public String getPageTitle(String content) {
        String regex;
        String originTitle = null;
        regex = "<meta property=\"og:description\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            originTitle = ma.group(1);
            originTitle = originTitle.replace("“", "");
            originTitle = originTitle.replace("”", "");
            return originTitle;
        }

        return null;
    }

    /**
     * =null就是解析失败
     *
     * @param htmlUrl
     * @return
     */
    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        String content = startRequest(htmlUrl);
        String videoUrl = getVideoUrl(content);
        String imageUrl = getImageUrl(content);
        String pageTitle = getPageTitle(content);
        DownloadContentItem data = new DownloadContentItem();

        if (!TextUtils.isEmpty(videoUrl)) {
            LogUtil.e("kw", "videoURL=" + videoUrl);
            data.addVideo(videoUrl);
            data.pageThumb = imageUrl;
            data.pageTitle = pageTitle;
            data.pageURL = htmlUrl;
        }

        if (data.futureVideoList == null && data.futureImageList == null) {
            return null;
        }

        data.homeDirectory = "kwai";
        return data;

    }
}
