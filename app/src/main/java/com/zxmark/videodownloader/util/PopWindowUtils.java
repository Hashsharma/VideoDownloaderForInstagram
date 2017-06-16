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

/**
 * Created by fanlitao on 17/6/14.
 */

public class PopWindowUtils {


    public static void showVideoMoreOptionWindow(View trigerView, final MainListRecyclerAdapter.IPopWindowClickCallback callback) {
        Context context = MainApplication.getInstance().getApplicationContext();
        View contentView = LayoutInflater.from(context).inflate(R.layout.more_option, null);
        View deleteView = contentView.findViewById(R.id.delete);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                DimensUtil.dip2px(120), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onDelete();
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
        popupWindow.showAsDropDown(trigerView, -DimensUtil.dip2px(120) + trigerView.getWidth() / 2, 0);
    }
}
