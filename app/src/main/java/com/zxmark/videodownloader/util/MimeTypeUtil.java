package com.zxmark.videodownloader.util;

import java.util.HashMap;

/**
 * Created by fanlitao on 17/6/15.
 */

public class MimeTypeUtil {

    public static HashMap<String, String> mimeTypeCache;

    static {

        mimeTypeCache = new HashMap<>();
        mimeTypeCache.put("mp4", "video/mp4");
        mimeTypeCache.put("jpg", "image/*");
        mimeTypeCache.put("png", "image/*");
        mimeTypeCache.put("jpeg", "image/*");
        mimeTypeCache.put("gif", "image/*");
    }

    public static String getMimeTypeBySuffixName(String fileName) {
        return mimeTypeCache.get(fileName);
    }

    public static String getSuffixByName(String name) {
        LogUtil.v("name", "getSuffixByName:" + name);
        String array[] = name.split("\\.");
        return array[array.length - 1];
    }

    public static String getMimeTypeByFileName(String fileName) {
        return getMimeTypeBySuffixName(getSuffixByName(fileName));
    }

    public static boolean isVideoType(String fileName) {
        String array[] = fileName.split("\\.");
        String suffixName = array[array.length - 1];
        return suffixName.equals("mp4");
    }
}
