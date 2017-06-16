package com.zxmark.videodownloader.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.imobapp.videodownloaderforinstagram.R;

/**
 * Created by fanlitao on 6/16/17.
 */

public final class IToast {

    private Toast mToast;

    private IToast(Context context, CharSequence text, int duration) {
        View v = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        TextView textView = (TextView) v.findViewById(R.id.toast_text);
        textView.setText(text);
        mToast = new Toast(context);
        mToast.setDuration(duration);
        mToast.setView(v);
    }

    public static IToast makeText(Context context, CharSequence text, int duration) {
        return new IToast(context, text, duration);
    }

    public static IToast makeText(Context context, int textId, int duration) {
        return new IToast(context, context.getResources().getString(textId), duration);
    }

    public void show() {
        if (mToast != null) {
            mToast.show();
        }
    }

    public void setGravity(int gravity, int xOffset, int yOffset) {
        if (mToast != null) {
            mToast.setGravity(gravity, xOffset, yOffset);
        }
    }

}
