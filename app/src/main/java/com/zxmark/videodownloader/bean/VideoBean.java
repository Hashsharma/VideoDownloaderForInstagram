package com.zxmark.videodownloader.bean;

import com.facebook.ads.NativeAd;
import com.zxmark.videodownloader.adapter.MainDownloadingRecyclerAdapter;

import java.io.File;

/**
 * Created by fanlitao on 17/6/13.
 */

public class VideoBean {

    public static final int TYPE_AD = 1;
    public String pageTitle;
    public String sharedUrl;
    public String appPageUrl;
    public String thumbnailUrl;
    public String downloadVideoUrl;
    public String videoPath;

    public int progress;

    public int type;

    public File file;

    public NativeAd facebookNativeAd;


    @Override
    public boolean equals(Object obj) {

        if (obj instanceof VideoBean) {
            VideoBean bean = (VideoBean) obj;
            if (type == MainDownloadingRecyclerAdapter.VIEW_TYPE_NORMAL) {
                return videoPath.equals(bean.videoPath);
            } else {
                if (type == MainDownloadingRecyclerAdapter.VIEW_TYPE_AD) {
                    return false;
                }
                if (type == MainDownloadingRecyclerAdapter.VIEW_TYPE_HOW_TO) {
                    if(type == bean.type) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return super.equals(obj);


    }
}
