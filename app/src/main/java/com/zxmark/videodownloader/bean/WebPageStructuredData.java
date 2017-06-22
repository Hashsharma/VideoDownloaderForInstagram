package com.zxmark.videodownloader.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanlitao on 17/6/9.
 */

public class WebPageStructuredData {

    public String pageTitle;
    public String pageDesc;
    public String appPageUrl;
    public String hashTags;
    public String videoThumbnailUrl;
    public List<String> futureVideoList;
    public List<String> futureImageList;


    public void addVideo(String path) {
        if (futureVideoList == null) {
            futureVideoList = new ArrayList<>();
        }
        if (!futureVideoList.contains(path)) {
            futureVideoList.add(path);
        }
    }

    public void addImage(String path) {
        if (futureImageList == null) {
            futureImageList = new ArrayList<>();
        }

        if (videoThumbnailUrl != null && videoThumbnailUrl.equals(path)) {
            return;
        }
        if (!futureImageList.contains(path)) {
            futureImageList.add(path);
        }
    }


}
