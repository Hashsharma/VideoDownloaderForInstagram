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

public class NativeAdItemHolder extends RecyclerView.ViewHolder {


    public ImageView adCoverView;
    public ImageView adIconView;
    public TextView adTitleView;
    public TextView adBodyView;
    public Button adButton;
    public LinearLayout adChoiceView;


    public NativeAdItemHolder(View itemView) {
        super(itemView);
        adCoverView = (ImageView) itemView.findViewById(R.id.ad_cover);
        adIconView = (ImageView) itemView.findViewById(R.id.ad_icon);
        adTitleView = (TextView) itemView.findViewById(R.id.ad_title);
        adBodyView = (TextView) itemView.findViewById(R.id.ad_body);
        adButton = (Button) itemView.findViewById(R.id.facebook_ad_btn);
        adChoiceView = (LinearLayout) itemView.findViewById(R.id.ad_choices_container);
    }
}
