package com.zxmark.videodownloader.downloader;

import android.util.Log;

import com.zxmark.videodownloader.spider.HttpRequestSpider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 17/6/8.
 */

public class TumblrVideoDownloader extends BaseDownloader {

    public static final String URL_FORMAT = "http://api.tumblr.com/v2/blog/%s.tumblr.com/posts?id=%s&api_key=fIujCUAeD0ZlUEPtwVeMSdLmX0MdCjGpSSNTN0qbc6f63EBsa5";

    @Override
    public String startRequest(String htmlUrl) {
        return HttpRequestSpider.getInstance().request(htmlUrl);
    }

    @Override
    public String getVideoUrl(String content) {
        String regex;
        String videoUrl = null;
        regex = "\"video_url\":\"(.*?)\",";
        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            videoUrl = ma.group(1);
        }
        Log.v("fan2", "" + videoUrl);
        return videoUrl;
    }

    @Override
    public String getDownloadFileUrl(String htmlUrl) {
        String targetUrl = String.format(URL_FORMAT, getTumblrBlogId(htmlUrl), getTumblrPostId(htmlUrl));
        String content = startRequest(targetUrl);
        return getVideoUrl(content);
    }

    public String getTumblrPostId(String url) {
        int startIndex = url.indexOf("post/") + 5;
        int endIndex = url.lastIndexOf("/");
        if (startIndex < endIndex) {
            return url.substring(startIndex, endIndex);
        } else {
            return url.substring(startIndex);
        }

    }

    public String getTumblrBlogId(String url) {
        int startIndex = url.indexOf("//") + 2;
        int endIndex = url.indexOf(".tumblr.com");
        if (startIndex < endIndex) {
            return url.substring(startIndex, endIndex);
        } else {
            return url.substring(startIndex);
        }
    }
}
