package com.zxmark.videodownloader.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by fanlitao on 17/6/14.
 */

public class ActivityManagerUtils {
    public static boolean isTopActivity(Context mContext) {
        String topPackageName = "";
        ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = am
                .getRunningTasks(1);
        if (runningTasks != null && !runningTasks.isEmpty()) {
            ActivityManager.RunningTaskInfo taskInfo = runningTasks.get(0);
            topPackageName = taskInfo.topActivity.getPackageName();
        }

        LogUtil.e("main","topPackageName:" + topPackageName);
        return TextUtils.isEmpty(topPackageName) ? false : topPackageName.equals(mContext.getPackageName());

    }
}
