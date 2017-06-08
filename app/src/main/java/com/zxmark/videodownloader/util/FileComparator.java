package com.zxmark.videodownloader.util;

import java.io.File;
import java.util.Comparator;

/**
 * Created by fanlitao on 17/6/8.
 */

public class FileComparator implements Comparator<File> {


    @Override
    public int compare(File o1, File o2) {
        return (int) (o2.lastModified() - o1.lastModified());
    }
}
