package com.zxmark.videodownloader.bean;

/**
 * Created by fanlitao on 17/6/13.
 */

public class VideoBean {

    public String pageTitle;
    public String sharedUrl;
    public String appPageUrl;
    public String thumbnailUrl;
    public String downloadVideoUrl;
    public String videoPath;

    public int progress;


    @Override
    public boolean equals(Object obj) {


        if (obj instanceof VideoBean) {
            VideoBean bean = (VideoBean) obj;
            return videoPath.equals(bean.videoPath);
        }
        return super.equals(obj);


    }
}