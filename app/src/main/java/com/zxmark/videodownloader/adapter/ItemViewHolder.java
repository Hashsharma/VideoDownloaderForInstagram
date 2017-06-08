package com.zxmark.videodownloader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.zxmark.videodownloader.R;

/**
 * Created by fanlitao on 17/6/7.
 */

public class ItemViewHolder extends  RecyclerView.ViewHolder {



    public ImageView thumbnailView;
    public ItemViewHolder(View itemView) {
        super(itemView);
        thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
    }
}
