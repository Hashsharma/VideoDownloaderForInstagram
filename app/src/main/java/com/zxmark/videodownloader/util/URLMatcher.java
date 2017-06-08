package com.zxmark.videodownloader.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 17/6/8.
 */

public class URLMatcher {

    public static final String getHttpURL(String text) {
        Pattern pattern = Pattern
                .compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");
        // 空格结束
        Matcher matcher = pattern
                .matcher(text);

        while (matcher.find()) {
            return matcher.group(0);
        }

        return text;
    }
}
