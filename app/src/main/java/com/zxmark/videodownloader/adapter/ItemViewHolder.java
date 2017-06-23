package com.zxmark.videodownloader.adapter;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bcgdv.asia.lib.fanmenu.FanMenuButtons;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.imobapp.videodownloaderforinstagram.R;

/*
 * Created by fanlitao on 17/6/7.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder {


    public ImageView thumbnailView;
    public ImageView operationBtn;
    public NumberProgressBar progressBar;
    public TextView titleTv;
    public ImageView moreIv;
    public FanMenuButtons fanMenuButtons;
    public View repostView;
    public View playView;
    public View albumView;
    public TextView taskCountView;
    public TextView hashTagView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
        operationBtn = (ImageView) itemView.findViewById(R.id.btn_operation);
        progressBar = (NumberProgressBar) itemView.findViewById(R.id.progressbar);
        titleTv = (TextView) itemView.findViewById(R.id.title);
        moreIv = (ImageView) itemView.findViewById(R.id.more);
        fanMenuButtons = (FanMenuButtons) itemView.findViewById(R.id.myFABSubmenu);

        repostView = itemView.findViewById(R.id.repost);
        playView = itemView.findViewById(R.id.play_view);
        albumView = itemView.findViewById(R.id.album_icon);
        taskCountView = (TextView) itemView.findViewById(R.id.download_task_info);
        hashTagView = (TextView) itemView.findViewById(R.id.hash_tag);
    }
}
