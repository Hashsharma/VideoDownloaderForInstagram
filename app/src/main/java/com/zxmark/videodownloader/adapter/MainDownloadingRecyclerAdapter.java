package com.zxmark.videodownloader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.util.Util;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.R;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.Utils;

import java.util.List;

/**
 * Created by fanlitao on 17/6/7.
 */

public class MainDownloadingRecyclerAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private List<VideoBean> mDataList;
    private RequestManager imageLoader;
    private boolean mFullImageState = false;

    public MainDownloadingRecyclerAdapter(List<VideoBean> dataList, boolean isFullImage) {
        mDataList = dataList;
        imageLoader = Glide.with(MainApplication.getInstance().getApplicationContext());
        mFullImageState = isFullImage;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout2, parent, false);
        ItemViewHolder holder = new ItemViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final VideoBean bean = mDataList.get(position);
//        holder.thumbnailView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DownloadUtil.openVideo(bean.file);
                Utils.openKuaiShouApp("https://www.kuaishou.com/photo/513379859/2375662159?userId=513379859&photoId=2375662159&timestamp=1497351245304&cc=share_copylink&language=zh-cn&fid=536729859&et=1_i%252F1570086573710716933_h73");
//            }
//        });
        // holder.progressBar.setProgress(bean.progress);
        holder.operationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              //  bean.file.delete();
//                mDataList.remove(bean);
//                notifyDataSetChanged();

            }
        });

        LogUtil.v("db", "bean.thumburiL" + bean.thumbnailUrl);
        imageLoader.load(bean.thumbnailUrl).into(holder.thumbnailView);
        holder.titleTv.setText(bean.pageTitle);
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }
}
