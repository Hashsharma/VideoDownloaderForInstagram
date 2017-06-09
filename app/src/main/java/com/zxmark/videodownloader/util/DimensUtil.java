/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.zxmark.videodownloader.util;

import com.zxmark.videodownloader.MainApplication;

import android.content.Context;

import java.lang.reflect.Field;

public final class DimensUtil {

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2dip(float density, float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int dip2px(float density, float dipValue) {
        return (int) (dipValue * density + 0.5f);
    }

    public static int dip2px(float dipValue) {
        final float scale = MainApplication.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int sp2px(float scaledDensity, float spValue) {
        return (int) (spValue * scaledDensity + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = MainApplication.getInstance().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getStatusBarHeight(Context ctx) {
        int statusBarHeight = 0;
        try {
            /**
             * 通过反射机制获取StatusBar高度
             */
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int height = Integer.parseInt(field.get(object).toString());
            /**
             * 设置StatusBar高度
             */
            statusBarHeight = ctx.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statusBarHeight;
    }

}
