package com.zxmark.videodownloader.floatview;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.zxmark.videodownloader.MainApplication;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.component.FloatNotificationView;
import com.zxmark.videodownloader.util.DeviceUtil;
import com.zxmark.videodownloader.util.DimensUtil;
import com.zxmark.videodownloader.util.LogUtil;

import java.nio.charset.MalformedInputException;

/**
 * Created by fanlitao on 17/6/9.
 */

public class FloatViewManager {


    private FloatNotificationView mFloatView;
    private TextView mProgressInfoTv;
    private Context mContext;

    private WindowManager mWindowManager;

    private static final FloatViewManager sInstance = new FloatViewManager();

    public static FloatViewManager getDefault() {
        return sInstance;
    }

    private FloatViewManager() {
        mContext = MainApplication.getInstance().getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mFloatView = new FloatNotificationView(mContext);
    }


    public void showFloatView() {
        if (mFloatView != null && mFloatView.getParent() != null) {
            return;
        }
        try {
            boolean canShowFloatView = true;
            if (Build.VERSION.SDK_INT >= 23) {
                canShowFloatView = false;
                if (Settings.canDrawOverlays(mContext)) {
                    canShowFloatView = true;
                }
            }

            if (canShowFloatView) {
                LogUtil.e("float", "showFloatView");
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                if (Build.VERSION.SDK_INT >= 26) {
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else if (Build.VERSION.SDK_INT >= 24) {
                    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                } else if (Build.VERSION.SDK_INT >= 19) {
                    params.type = WindowManager.LayoutParams.TYPE_TOAST;
                    try {
                        String obj = Build.MODEL;
                        if (!TextUtils.isEmpty(obj) && obj.toLowerCase().contains("vivo") && Build.VERSION.SDK_INT > 19 && Build.VERSION.SDK_INT < 23) {
                            params.type = WindowManager.LayoutParams.TYPE_PHONE;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                params.format = PixelFormat.RGBA_8888;
                params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                params.dimAmount = 0.5f;
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.x = DeviceUtil.getScreenWidth() - DimensUtil.dip2px(100);
                params.y = DeviceUtil.getScreenHeight() - DimensUtil.dip2px(200);
                mFloatView.setWindowManager(mWindowManager);
                mFloatView.setLayoutParams(params);
                mWindowManager.addView(mFloatView, params);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.e("float", "showFloatView.ex:" + ex.getMessage());
        }

    }

    private int mProgress;

//    public void setProgress(int progress) {
//        if (mFloatView != null) {
//            if (mProgress != progress) {
//                mFloatView.setProgress(progress);
//                mProgress = progress;
//            }
//
//        }
//    }

    public void dismissFloatView() {
        if (mFloatView != null && mFloatView.getParent() != null) {
            mWindowManager.removeViewImmediate(mFloatView);
        }
    }
}
