package com.zxmark.videodownloader.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.spider.HttpRequestSpider;
import com.zxmark.videodownloader.util.LogUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 17/6/7.
 */

public class InstagramDownloader extends BaseDownloader {


    public static final String IMAGE_SUFFIX = "https://scontent-arn2-1.cdninstagram.com";
    public static final String REPLACE_SUFFIX = "https://ig-s-a-a.akamaihd.net/hphotos-ak-xpa1";


    public String startRequest(String htmlUrl) {
        return HttpRequestSpider.getInstance().request(htmlUrl);
    }

    public String getVideoUrl(String content) {
        String regex;
        String videoUrl = null;
        regex = "<meta property=\"og:video\" content=\"(.*?)\" />";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            videoUrl = ma.group(1);
        }

        String thumbnail = getImageUrl(content);
        Log.v("fan3", "thumbnail:" + thumbnail);
        return videoUrl;
    }

    public String getImageUrl(String content) {
        String regex;
        String imageUrl = "";
        regex = "<meta property=\"og:image\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            imageUrl = ma.group(1);
        }


        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.startsWith(IMAGE_SUFFIX)) {
                imageUrl = REPLACE_SUFFIX + imageUrl.substring(IMAGE_SUFFIX.length());
            }
        }
        return imageUrl;
    }

    public String getPageTitle(String content) {
        String regex;
        String pageDesc = "";
        regex = "<meta property=\"og:description\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            pageDesc = ma.group(1);
        }

        if (!TextUtils.isEmpty(pageDesc)) {
            String array[] = pageDesc.split("Instagram:");
            if (array != null) {
                String originTitle = array[array.length-1];
                originTitle = originTitle.replace("“","");
                originTitle = originTitle.replace("”","");
                return originTitle;
            }
        }
        return null;
    }

    public WebPageStructuredData startSpideThePage(String htmlUrl) {
        String content = startRequest(htmlUrl);
        String videoUrl = getVideoUrl(content);
        WebPageStructuredData data = new WebPageStructuredData();
        if (TextUtils.isEmpty(videoUrl)) {
            String imageUrl = getImageUrl(content);
            data.addVideo(imageUrl);
        } else {
            data.addVideo(videoUrl);
            String imageUrl = getImageUrl(content);
            data.addImage(imageUrl);
        }

        String title = getPageTitle(content);
        data.pageTitle = title;
        data.appPageUrl = getLaunchInstagramUrl(content);
        LogUtil.e("fan", "title:" + title);
        return data;
    }

    public String getLaunchInstagramUrl(String content) {
        String regex;
        String instagramUrl = "";
        regex = "<meta property=\"al:android:url\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            instagramUrl = ma.group(1);
        }
        return instagramUrl;
    }
}
