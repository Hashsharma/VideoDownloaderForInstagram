package com.zxmark.videodownloader;

import com.facebook.ads.NativeAd;
import com.zxmark.videodownloader.adapter.MainDownloadingRecyclerAdapter;

import java.io.File;

/**
 * Created by fanlitao on 17/6/8.
 */

public class DownloaderBean {
    public File file;
    public int progress;
    public int type;
    public NativeAd facebookNativeAd;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloaderBean) {
            DownloaderBean right = (DownloaderBean) obj;
            if(type == MainDownloadingRecyclerAdapter.VIEW_TYPE_AD) {
                return false;
            }
            if(file == null || right.file == null) {
                return false;
            }
            return file.getAbsolutePath().equals(right.file.getAbsolutePath());
        }
        return false;
    }
}
