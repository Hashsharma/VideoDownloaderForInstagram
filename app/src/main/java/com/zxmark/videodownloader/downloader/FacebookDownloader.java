package com.zxmark.videodownloader.downloader;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.spider.HttpRequestSpider;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    protected String startRequest(String htmlUrl) {
        return HttpRequestSpider.getInstance().request(htmlUrl);
    }

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
                videoUrl = replaceEscapteSequence(videoUrl);
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
                String url = replaceEscapteSequence(imageUrl);
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
                videoUrl = replaceEscapteSequence(videoUrl);
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
        regex = "&quot;(.*?).mp4&quot;";
        Pattern pa = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        while (ma.find()) {
            Log.v("fan2", "" + ma.group());
            videoUrl = ma.group(1);
            if (!TextUtils.isEmpty(videoUrl)) {
                videoUrl = replaceEscapteSequence(videoUrl);
                item.addVideo(videoUrl);
                LogUtil.e("facebook", "encode=videoUrl=" + videoUrl);
            }
            LogUtil.e("facebook", "videoUrl=" + videoUrl);
        }
        return videoUrl;
    }


    public String replaceEscapteSequence(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) {
            return rawUrl;
        }
        return rawUrl.replace("&amp;", "&");
    }

    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        LogUtil.e("facebook", "startSpideThePage:" + htmlUrl);
        String content = startRequest(htmlUrl);

        writeFile(content);
        LogUtil.e("facebook", "content:" + content);
        DownloadContentItem data = new DownloadContentItem();
        getVideoUrl2(content, data);
        getImageURL(content, data);
        data.pageTitle = DownloadUtil.getFileNameByUrl(htmlUrl);
        data.pageTags = "FaceBook";
        return data;
    }

    private void writeFile(String content) {
        File writename = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "facebook.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件
        try {
            writename.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            out.write("我会写入文件啦\r\n"); // \r\n即为换行
            out.flush(); // 把缓存区内容压入文件
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
