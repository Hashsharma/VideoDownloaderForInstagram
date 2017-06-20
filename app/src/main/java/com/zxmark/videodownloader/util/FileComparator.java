package com.zxmark.videodownloader.util;

import com.zxmark.videodownloader.DownloaderBean;
import com.zxmark.videodownloader.bean.VideoBean;

import java.util.Comparator;

/**
 * Created by fanlitao on 17/6/8.
 */

public class FileComparator implements Comparator<VideoBean> {


    @Override
    public int compare(VideoBean o1, VideoBean o2) {

        return (int) (o2.file.lastModified() - o1.file.lastModified());
    }
}
