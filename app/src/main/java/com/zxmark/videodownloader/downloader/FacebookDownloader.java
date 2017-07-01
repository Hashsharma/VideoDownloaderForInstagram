package com.zxmark.videodownloader.downloader;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.spider.HttpRequestSpider;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 6/26/17.
 * <p>
 * <p>
 * 这个资源链接需要涉及到一些转义要注意&
 */

public class FacebookDownloader extends BaseDownloader {



    @Override
    public String getVideoUrl(String content) {

        String regex;
        String videoUrl = null;
        regex = "\\[\\{video:\\[\\{url:\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        while (ma.find()) {
            Log.v("fan2", "" + ma.group());
            videoUrl = ma.group(1);
            if (!TextUtils.isEmpty(videoUrl)) {
                videoUrl = Utils.replaceEscapteSequence(videoUrl);
                LogUtil.e("facebook", "encode=videoUrl=" + videoUrl);
            }
            LogUtil.e("facebook", "videoUrl=" + videoUrl);
        }
        return videoUrl;
    }

    public String getImageURL(String content, DownloadContentItem item) {
        String regex;
        String imageUrl = null;
        regex = "data-ploi=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            imageUrl = ma.group(1);
            LogUtil.e("facebook", "imageUrl=" + imageUrl);
            if (!TextUtils.isEmpty(imageUrl)) {
                String url = Utils.replaceEscapteSequence(imageUrl);
                LogUtil.e("facebook", "encode=imageUrl=" + url);
                item.addImage(imageUrl);
            }
        }
        return imageUrl;
    }

    public String getVideoUrl(String content, DownloadContentItem item) {

        String regex;
        String videoUrl = null;
        regex = "\\[\\{video:\\[\\{url:\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        while (ma.find()) {
            Log.v("fan2", "" + ma.group());
            videoUrl = ma.group(1);
            if (!TextUtils.isEmpty(videoUrl)) {
                videoUrl = Utils.replaceEscapteSequence(videoUrl);
                item.addVideo(videoUrl);
                LogUtil.e("facebook", "encode=videoUrl=" + videoUrl);
            }
            LogUtil.e("facebook", "videoUrl=" + videoUrl);
        }
        return videoUrl;
    }

    public String getVideoUrl2(String content, DownloadContentItem item) {

        String regex;
        String videoUrl = null;
        regex = "href=\"/video_redirect/\\?src=(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        LogUtil.e("facebook", "ma=" + ma);

        while (ma.find()) {
            LogUtil.e("facebook", "" + ma.group());
            videoUrl = ma.group(1);
            if (!TextUtils.isEmpty(videoUrl)) {
                if(videoUrl.startsWith("http")) {
                    try {
                        videoUrl =  URLDecoder.decode(videoUrl,"utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();

                    }
                }
                videoUrl = Utils.replaceEscapteSequence(videoUrl);
                item.addVideo(videoUrl);
            }
            LogUtil.e("facebook", "videoUrl=" + videoUrl);
        }
        return videoUrl;
    }



    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        LogUtil.e("facebook", "startSpideThePage:" + htmlUrl);
        String content = startRequest(htmlUrl);
        LogUtil.e("facebook", "content:" + content);
        DownloadContentItem data = new DownloadContentItem();
        getVideoUrl2(content, data);
        getImageURL(content, data);
        data.pageURL = htmlUrl;
        data.pageTitle = DownloadUtil.getFileNameByUrl(htmlUrl);
        data.pageTags = "FaceBook";
        data.pageThumb = getVideoThumbnail(content);
        return data;
    }


    private String getVideoThumbnail(String content) {
        String regex;
        String videoUrl = null;
        regex = "<div class=\"bl\"><img src=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        LogUtil.e("facebook", "ma=" + ma);

        while (ma.find()) {
            LogUtil.e("facebook", "" + ma.group());
            videoUrl = ma.group(1);
            if (!TextUtils.isEmpty(videoUrl)) {
                if(videoUrl.startsWith("http")) {
                    try {
                        videoUrl =  URLDecoder.decode(videoUrl,"utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();

                    }
                }
                videoUrl = Utils.replaceEscapteSequence(videoUrl);
            }
            LogUtil.e("facebook", "thumbnail=" + videoUrl);
        }
        return videoUrl;
    }

}
