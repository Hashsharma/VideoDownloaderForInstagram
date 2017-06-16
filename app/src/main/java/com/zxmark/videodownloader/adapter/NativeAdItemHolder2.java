package com.zxmark.videodownloader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imobapp.videodownloaderforinstagram.R;

/**
 * Created by fanlitao on 6/16/17.
 */

public class NativeAdItemHolder2 extends RecyclerView.ViewHolder {


    public ImageView adCoverView;
    public LinearLayout adChoiceView;
    public TextView adBtn;
    public TextView adTitle;


    public NativeAdItemHolder2(View itemView) {
        super(itemView);
        adCoverView = (ImageView) itemView.findViewById(R.id.ad_cover);
        adChoiceView = (LinearLayout) itemView.findViewById(R.id.ad_choices_container);
        adBtn = (TextView) itemView.findViewById(R.id.ad_btn_action);
        adTitle = (TextView) itemView.findViewById(R.id.ad_title);
    }
}
