package com.zxmark.videodownloader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bcgdv.asia.lib.fanmenu.FanMenuButtons;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.ads.AdChoicesView;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.util.DownloadUtil;

import java.io.File;
import java.util.List;

/**
 * Created by fanlitao on 17/6/7.
 */

public class MainDownloadingRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static final int VIEW_TYPE_NORMAL = 0;
    public static final int VIEW_TYPE_HEAD = 1;
    public static final int VIEW_TYPE_AD = 2;
    private List<VideoBean> mDataList;
    private RequestManager imageLoader;
    private boolean mFullImageState = false;
    private Context mContext;

    public MainDownloadingRecyclerAdapter(List<VideoBean> dataList, boolean isFullImage) {
        mDataList = dataList;
        imageLoader = Glide.with(MainApplication.getInstance().getApplicationContext());
        mFullImageState = isFullImage;
        mContext = MainApplication.getInstance().getApplicationContext();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == VIEW_TYPE_AD) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.facebook_native_item, parent, false);

            return new NativeAdItemHolder(itemView);
        } else if(viewType == VIEW_TYPE_HEAD) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new ItemHeaderHolder(itemView);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout2, parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder baseHolder, int position) {
        final VideoBean bean = mDataList.get(position);

        if (baseHolder instanceof ItemViewHolder) {
            final ItemViewHolder holder = (ItemViewHolder) baseHolder;
            holder.operationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.fanMenuButtons.toggleShow();
                }
            });
            holder.fanMenuButtons.setOnFanButtonClickListener(new FanMenuButtons.OnFanClickListener() {
                @Override
                public void onFanButtonClicked(int index) {
                    holder.fanMenuButtons.toggleShow();
                    if (index == 0) {
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);

                        DownloadUtil.startDownload(bean.sharedUrl);
                    } else if (index == 1) {
                        DownloadUtil.downloadThumbnail(bean.thumbnailUrl);
                    } else if (index == 2) {
                        deleteDownloadingVideo(bean);
                    }
                }
            });
            imageLoader.load(bean.thumbnailUrl).centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.thumbnailView);
            holder.titleTv.setText(bean.pageTitle);
        } else if(baseHolder instanceof NativeAdItemHolder){
            final NativeAdItemHolder holder = (NativeAdItemHolder) baseHolder;
            if (bean.facebookNativeAd != null) {
                AdChoicesView adChoicesView = new AdChoicesView(mContext, bean.facebookNativeAd, true);
                holder.adChoiceView.addView(adChoicesView);

                imageLoader.load(bean.facebookNativeAd.getAdCoverImage().getUrl()).into(holder.adCoverView);
                imageLoader.load(bean.facebookNativeAd.getAdIcon().getUrl()).into(holder.adIconView);

                holder.adBodyView.setText(bean.facebookNativeAd.getAdBody());
                holder.adTitleView.setText(bean.facebookNativeAd.getAdTitle());
                // Register the native ad view with the native ad instance
                holder.adButton.setText(bean.facebookNativeAd.getAdCallToAction());
                bean.facebookNativeAd.registerViewForInteraction(holder.itemView);

            }
        } else if (baseHolder instanceof ItemHeaderHolder) {

        }

    }

    private void deleteDownloadingVideo(VideoBean bean) {
        mDataList.remove(bean);
        notifyDataSetChanged();
        DBHelper.getDefault().deleteDownloadingVideo(bean.videoPath);
        DownloadingTaskList.SINGLETON.intrupted(bean.sharedUrl);
        new File(bean.videoPath).delete();

    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).type;
    }


}
