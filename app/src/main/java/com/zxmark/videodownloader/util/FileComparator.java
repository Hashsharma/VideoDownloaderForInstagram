package com.zxmark.videodownloader.util;

import com.zxmark.videodownloader.DownloaderBean;

import java.util.Comparator;

/**
 * Created by fanlitao on 17/6/8.
 */

public class FileComparator implements Comparator<DownloaderBean> {


    @Override
    public int compare(DownloaderBean o1, DownloaderBean o2) {
        return (int) (o2.file.lastModified() - o1.file.lastModified());
    }
}
