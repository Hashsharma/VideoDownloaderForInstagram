package com.zxmark.videodownloader;

import java.io.File;

/**
 * Created by fanlitao on 17/6/8.
 */

public class DownloaderBean {
    public File file;
    public int progress;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloaderBean) {
            DownloaderBean right = (DownloaderBean) obj;

            return file.getAbsolutePath().equals(right.file.getAbsolutePath());
        }
        return false;
    }
}
