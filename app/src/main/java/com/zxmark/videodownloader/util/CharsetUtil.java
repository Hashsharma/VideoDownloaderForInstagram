package com.zxmark.videodownloader.util;

import java.io.UnsupportedEncodingException;

/**
 * Created by fanlitao on 6/23/17.
 */

public class CharsetUtil {


    public static final String UTF_8 = "UTF-8";
    public static String changeCharset(String str, String newCharset) {

        try {
            if (str != null) {
                //用默认字符编码解码字符串。
                byte[] bs = str.getBytes();
                //用新的字符编码生成字符串
                return new String(bs, newCharset);
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return str;
    }


    public static String formatUTF8(String content) {
        return changeCharset(content,UTF_8);
    }

    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }

}
