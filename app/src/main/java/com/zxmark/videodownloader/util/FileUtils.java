package com.zxmark.videodownloader.util;

import java.io.File;

/**
 * Created by fanlitao on 6/21/17.
 */

public class FileUtils {


    public static String getFileNameByPath(String path) {
        String[] separtArray = path.split(File.separator);
        if (separtArray != null && separtArray.length > 1) {
            return separtArray[separtArray.length - 1];
        }
        return path;
    }
}
