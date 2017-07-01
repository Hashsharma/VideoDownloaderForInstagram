package com.zxmark.videodownloader.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.zxmark.videodownloader.MainApplication;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.adapter.MainListRecyclerAdapter;
import com.zxmark.videodownloader.db.DownloadContentItem;

/**
 * Created by fanlitao on 17/6/14.
 */

public class PopWindowUtils {


    public static void showVideoMoreOptionWindow(View trigerView, boolean showRedownloadBtn, final MainListRecyclerAdapter.IPopWindowClickCallback callback) {
        Context context = MainApplication.getInstance().getApplicationContext();
        View contentView = LayoutInflater.from(context).inflate(R.layout.more_option, null);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                DimensUtil.dip2px(100), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        contentView.findViewById(R.id.copy_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCopyAll();
                }

                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.copy_hashtags).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCopyHashTags();
                }

                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.launch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.launchAppByUrl();
                }

                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onPasteSharedUrl();
                }

                popupWindow.dismiss();
            }
        });
        if (showRedownloadBtn) {
            contentView.findViewById(R.id.redownload).setVisibility(View.VISIBLE);
        }
        contentView.findViewById(R.id.redownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onStartDownload();
                }

                popupWindow.dismiss();
            }
        });
        int windowPos[] = calculatePopWindowPos(trigerView, contentView);
        popupWindow.showAsDropDown(trigerView, -DimensUtil.dip2px(100) + trigerView.getWidth() / 2, windowPos[1]);
    }


    public static void showPlayVideoMorePopWindow(View trigerView, final IPopWindowCallback callback) {
        Context context = MainApplication.getInstance().getApplicationContext();
        View contentView = LayoutInflater.from(context).inflate(R.layout.videoplay_more_option, null);
        View deleteView = contentView.findViewById(R.id.share);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                DimensUtil.dip2px(100), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        contentView.findViewById(R.id.repost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onRepost();
                }

                popupWindow.dismiss();

            }
        });
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onShare();
                }

                popupWindow.dismiss();

            }
        });

        contentView.findViewById(R.id.launch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.launchInstagram();
                }

                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onPastePageUrl();
                }

                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onDelete();
                }

                popupWindow.dismiss();
            }
        });
        popupWindow.showAsDropDown(trigerView, -DimensUtil.dip2px(100) + trigerView.getWidth() / 2, -DimensUtil.dip2px(15));
    }


    /**
     * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
     * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
     *
     * @param anchorView  呼出window的view
     * @param contentView window的内容布局
     * @return window显示的左上角的xOff, yOff坐标
     */
    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // 获取屏幕的高宽
        final int screenHeight = DeviceUtil.getScreenHeight();
        final int screenWidth = DeviceUtil.getScreenWidth();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        final boolean isNeedShowUp = (anchorLoc[1] + windowHeight > screenHeight);
        if (isNeedShowUp) {
            windowPos[1] = (int) anchorView.getY() - windowHeight - anchorView.getHeight();
        } else {
            windowPos[1] = -DimensUtil.dip2px(10);
        }
        windowPos[0] = (int) anchorView.getX() - windowWidth - windowWidth;
        return windowPos;
    }


    public interface IPopWindowCallback {
        void onRepost();

        void onShare();

        void launchInstagram();

        void onPastePageUrl();

        void onDelete();
    }
}
