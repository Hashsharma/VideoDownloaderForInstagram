package com.zxmark.videodownloader.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.spider.HttpRequestSpider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 17/6/8.
 */

public class KuaiVideoDownloader extends BaseDownloader {
    @Override
    public String startRequest(String htmlUrl) {
        return HttpRequestSpider.getInstance().request(htmlUrl);
    }

    @Override
    public String getVideoUrl(String content) {
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


    @Override
    public WebPageStructuredData startSpideThePage(String htmlUrl) {

        String videoUrl = getVideoUrl(startRequest(htmlUrl));
        WebPageStructuredData data = new WebPageStructuredData();

        if (!TextUtils.isEmpty(videoUrl)) {
            data.addVideo(videoUrl);
        }

        return data;

    }
}
