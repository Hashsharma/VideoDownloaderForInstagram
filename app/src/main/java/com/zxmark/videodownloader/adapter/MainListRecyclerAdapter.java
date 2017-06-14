package com.zxmark.videodownloader.adapter;

import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zxmark.videodownloader.DownloaderBean;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.R;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by fanlitao on 17/6/7.
 */

public class MainListRecyclerAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private List<DownloaderBean> mDataList;
    private RequestManager imageLoader;
    private boolean mFullImageState = false;

    private DBHelper mDBHelper;

    public MainListRecyclerAdapter(List<DownloaderBean> dataList, boolean isFullImage) {
        mDataList = dataList;
        imageLoader = Glide.with(MainApplication.getInstance().getApplicationContext());
        mFullImageState = isFullImage;
        mDBHelper = DBHelper.getDefault();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(mFullImageState ? R.layout.item_layout2 : R.layout.item_layout,
                        parent, false);
        ItemViewHolder holder = new ItemViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final DownloaderBean bean = mDataList.get(position);
        holder.thumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadUtil.openVideo(bean.file);
            }
        });
        holder.operationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bean.file.delete();
                mDataList.remove(bean);
                notifyDataSetChanged();

            }
        });
        imageLoader.load(bean.file).into(holder.thumbnailView);

       VideoBean videoBean  =  mDBHelper.getVideoInfoByPath(bean.file.getAbsolutePath());
        LogUtil.v("sd","videoBean:" + videoBean);
        if(videoBean != null) {
            holder.titleTv.setText(videoBean.pageTitle);
        }

    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }
}
