package com.zxmark.videodownloader.floatview;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
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
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        params.format = PixelFormat.TRANSPARENT;
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
