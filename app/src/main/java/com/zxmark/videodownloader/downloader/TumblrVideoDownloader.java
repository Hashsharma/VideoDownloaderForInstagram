package com.zxmark.videodownloader.downloader;

import android.util.Log;

import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.spider.HttpRequestSpider;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
//        String regex;
//        String videoUrl = null;
//        regex = "\"video_url\":\"(.*?)\",";
//        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
//        Matcher ma = pa.matcher(content);
//
//        if (ma.find()) {
//            videoUrl = ma.group(1);
//        }

//        LogUtil.v("json","pageTitle:" + pageTitle);
//        if(rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).has("video_url")) {
//            String videoUrl = rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).getString("video_url");
//            LogUtil.v("json","videoUrl:" + videoUrl);
//        }
//        LogUtil.e("fan2", "tumblr.video=" + videoUrl);
//        return videoUrl;

        return null;
    }


    public String gePageTitle() {
        return null;
    }

    public String getImageUrl(String content) {
        try {
            JSONObject rootJsonObj = new JSONObject(content);
            String pageTitle = rootJsonObj.getJSONObject("response").getJSONObject("blog").getString("title");
            LogUtil.v("json", "pageTitle:" + pageTitle);
            if (rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).has("video_url")) {
                String videoUrl = rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).getString("video_url");
                LogUtil.v("json", "videoUrl:" + videoUrl);
            }


            if (rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).has("photos")) {
                JSONArray photoJsonArray = rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).getJSONArray("photos");
                int length = photoJsonArray.length();
                for (int index = 0; index < length; index++) {
                    String url = photoJsonArray.getJSONObject(index).getJSONObject("original_size").getString("url");
                    LogUtil.v("json", "imageUrl:" + url);
                }
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public DownloadContentItem startSpideThePage(String htmlUrl) {
        String targetUrl = String.format(URL_FORMAT, getTumblrBlogId(htmlUrl), getTumblrPostId(htmlUrl));
        LogUtil.e("fan2", "tumblr.blog.api:" + targetUrl);
        String content = startRequest(targetUrl);
        String videoUrl = null;
        LogUtil.e("tumblr", "content:" + content);
        DownloadContentItem data = new DownloadContentItem();
        try {

            JSONObject rootJsonObj = new JSONObject(content);
            String pageTitle = rootJsonObj.getJSONObject("response").getJSONObject("blog").getString("title");
            data.pageURL = htmlUrl;
            data.pageTitle = pageTitle;
            data.pageDesc = rootJsonObj.getJSONObject("response").getJSONObject("blog").getString("description");
            int fileCount = 0;
            if (rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).has("video_url")) {
                videoUrl = rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).getString("video_url");
                String videoThumbnailUrl = rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).getString("thumbnail_url");
                data.pageTitle = rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).getString("summary");
                LogUtil.e("tumblr", "videoUrl:" + videoUrl);
                data.pageThumb = videoThumbnailUrl;
                data.addVideo(videoUrl);
            }


            if (rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).has("photos")) {
                JSONArray photoJsonArray = rootJsonObj.getJSONObject("response").getJSONArray("posts").getJSONObject(0).getJSONArray("photos");
                int length = photoJsonArray.length();
                for (int index = 0; index < length; index++) {
                    String url = photoJsonArray.getJSONObject(index).getJSONObject("original_size").getString("url");
                    LogUtil.v("json", "imageUrl:" + url);
                    data.addImage(url);
                }

                if (data.futureImageList != null && data.futureImageList.size() > 0) {
                    data.pageThumb = data.futureImageList.get(0);
                }
            }
            data.pageHOME = DownloadUtil.getDownloadItemDirectory(htmlUrl);
        } catch (JSONException ex) {
            ex.printStackTrace();
            data.futureVideoList = null;
            data.futureImageList = null;
            data = null;
        }

        return data;
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
