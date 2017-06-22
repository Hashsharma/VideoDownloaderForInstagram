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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 17/6/7.
 */

public class InstagramDownloader extends BaseDownloader {


    public static final String IMAGE_SUFFIX = "https://scontent-arn2-1.cdninstagram.com";
    public static final String REPLACE_SUFFIX = "https://ig-s-a-a.akamaihd.net/hphotos-ak-xpa1";

    public static final String CDN_IMAGE_SUFFIX = "cdninstagram.com";

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

            if (imageUrl.contains(CDN_IMAGE_SUFFIX)) {
                String tempArray[] = imageUrl.split(CDN_IMAGE_SUFFIX);
                imageUrl = REPLACE_SUFFIX + tempArray[tempArray.length - 1];
            }

            LogUtil.e("image", "imageUrl=" + imageUrl);
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
                String originTitle = array[array.length - 1];
                originTitle = originTitle.replace("“", "");
                originTitle = originTitle.replace("”", "");
                return originTitle;
            }
        }
        return null;
    }

    public String getDescription(String content) {

        String regex;
        String pageDesc = "";
        regex = "\"node\": \\{\"text\": \"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            pageDesc = ma.group(1);
            LogUtil.e("ins", "pageDescritpin:" + pageDesc);
        }

        if (!TextUtils.isEmpty(pageDesc)) {
            String array[] = pageDesc.split("Instagram:");
            if (array != null) {
                String originTitle = array[array.length - 1];
                originTitle = originTitle.replace("“", "");
                originTitle = originTitle.replace("”", "");
                return originTitle;
            }
        }
        return null;
    }

    public String getPageHashTags(String content) {
        String regex;
        String hashTags = "";
        regex = "<meta property=\"instapp:hashtags\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        StringBuilder hashTagsBuilder = new StringBuilder();
        while (ma.find()) {
            Log.v("fan2", "" + ma.group());
            hashTags = ma.group(1);
            LogUtil.e("ins", "hashTags=" + hashTags);
            hashTagsBuilder.append("#");
            hashTagsBuilder.append(hashTags);

        }

        return hashTagsBuilder.toString();
    }

    public void getImageUrlFromJs(String content, WebPageStructuredData data) {
        String regex;
        String imageUrl = "";
        regex = "\"display_url\": \"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        while (ma.find()) {
            imageUrl = ma.group(1);
            if (!TextUtils.isEmpty(imageUrl)) {
                if (imageUrl.contains(CDN_IMAGE_SUFFIX)) {
                    String tempArray[] = imageUrl.split(CDN_IMAGE_SUFFIX);
                    imageUrl = REPLACE_SUFFIX + tempArray[tempArray.length - 1];
                    LogUtil.e("ins", "display_url=" + imageUrl);
                    data.addImage(imageUrl);
                } else {
                    LogUtil.e("ins", "display_url=" + imageUrl);
                    data.addImage(imageUrl);
                }
            }
        }
    }

    public void getVideoUrlFromJs(String content, WebPageStructuredData data) {
        String regex;
        String imageUrl = "";
        regex = "\"video_url\": \"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        while (ma.find()) {
            imageUrl = ma.group(1);
            data.addVideo(imageUrl);
        }

    }

    public WebPageStructuredData startSpideThePage(String htmlUrl) {
        String content = startRequest(htmlUrl);
        WebPageStructuredData data = new WebPageStructuredData();
        getVideoUrlFromJs(content, data);
        if (data.futureVideoList != null) {
            data.videoThumbnailUrl = getImageUrl(content);
        }
        getImageUrlFromJs(content, data);

        String title = getPageTitle(content);
        data.pageTitle = title;
        data.pageDesc = getDescription(content);
        data.appPageUrl = getLaunchInstagramUrl(content);
        data.hashTags = getPageHashTags(content);
        if (data.futureImageList == null && data.futureVideoList == null) {
            return null;
        }
        return data;
    }

    public String getLaunchInstagramUrl(String content) {
        String regex;
        String instagramUrl = "";
        regex = "<meta property=\"al:android:url\" content=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);

        if (ma.find()) {
            instagramUrl = ma.group(1);
        }
        return instagramUrl;
    }
}
