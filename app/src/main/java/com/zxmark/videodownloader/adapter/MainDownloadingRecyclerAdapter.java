package com.zxmark.videodownloader.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.downloader.VideoDownloadFactory;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.FileUtils;
import com.zxmark.videodownloader.util.MimeTypeUtil;
import com.zxmark.videodownloader.util.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by fanlitao on 17/6/7.
 */

public class MainDownloadingRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static final int VIEW_TYPE_NORMAL = 0;
    public static final int VIEW_TYPE_HEAD = 1;
    public static final int VIEW_TYPE_AD = 2;
    public static final int VIEW_TYPE_HOW_TO = 3;
    private List<DownloadContentItem> mDataList;
    private RequestManager imageLoader;
    private boolean mFullImageState = false;
    private Context mContext;
    private IBtnCallback callback;
    private boolean mClickedPasteBtn = false;
    private Resources mResources;

    public MainDownloadingRecyclerAdapter(List<DownloadContentItem> dataList, boolean isFullImage, IBtnCallback callback) {
        mDataList = dataList;
        imageLoader = Glide.with(MainApplication.getInstance().getApplicationContext());
        mFullImageState = isFullImage;
        mContext = MainApplication.getInstance().getApplicationContext();
        this.callback = callback;
        mClickedPasteBtn = false;
        mResources = mContext.getResources();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == DownloadContentItem.TYPE_FACEBOOK_AD) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.facebook_native_item, parent, false);

            return new NativeAdItemHolder(itemView);
        } else if (viewType == DownloadContentItem.TYPE_HEADER_ITEM) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new ItemHeaderHolder(itemView);
        } else if (viewType == DownloadContentItem.TYPE_HOWTO_ITEM) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_how_to, parent, false);
            return new ItemHowToHolder(itemView);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout2, parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder baseHolder, final int position) {
        final DownloadContentItem bean = mDataList.get(position);

        if (baseHolder instanceof ItemViewHolder) {
            final ItemViewHolder holder = (ItemViewHolder) baseHolder;
            holder.operationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.fanMenuButtons.toggleShow();
                }
            });

            holder.progressBar.setProgress(0);
            holder.fanMenuButtons.setOnFanButtonClickListener(new FanMenuButtons.OnFanClickListener() {
                @Override
                public void onFanButtonClicked(int index) {
                    holder.fanMenuButtons.toggleShow();
                    if (index == 0) {
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                        DownloadUtil.startResumeDownload(bean.pageURL);
                    } else if (index == 1) {
                        DownloadUtil.downloadThumbnail(bean.pageURL, bean.pageThumb);
                    } else if (index == 2) {
                        deleteDownloadingVideo(bean, position);
                    }
                }
            });

            holder.hashTagView.setText(bean.pageTags);
            holder.taskCountView.setText(mResources.getString(R.string.downloading_left_task_count, bean.fileCount));
            holder.playView.setVisibility(bean.mimeType == bean.PAGE_MIME_TYPE_VIDEO ? View.VISIBLE : View.GONE);
            imageLoader.load(bean.pageThumb).centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.thumbnailView);
            if (TextUtils.isEmpty(bean.pageTitle)) {
                holder.titleTv.setVisibility(View.GONE);
            } else {
                holder.titleTv.setText(bean.pageTitle);
                holder.titleTv.setVisibility(View.VISIBLE);
            }
        } else if (baseHolder instanceof NativeAdItemHolder) {
            final NativeAdItemHolder holder = (NativeAdItemHolder) baseHolder;
            if (bean.facebookNativeAd != null) {
                AdChoicesView adChoicesView = new AdChoicesView(mContext, bean.facebookNativeAd, true);
                if (holder.adChoiceView.getChildCount() == 0) {
                    holder.adChoiceView.addView(adChoicesView);
                }

                imageLoader.load(bean.facebookNativeAd.getAdCoverImage().getUrl()).into(holder.adCoverView);
                imageLoader.load(bean.facebookNativeAd.getAdIcon().getUrl()).into(holder.adIconView);

                holder.adBodyView.setText(bean.facebookNativeAd.getAdBody());
                holder.adTitleView.setText(bean.facebookNativeAd.getAdTitle());
                // Register the native ad view with the native ad instance
                holder.adButton.setText(bean.facebookNativeAd.getAdCallToAction());
                bean.facebookNativeAd.registerViewForInteraction(holder.itemView);

            }
        } else if (baseHolder instanceof ItemHeaderHolder) {
            final ItemHeaderHolder holder = (ItemHeaderHolder) baseHolder;
            holder.showHowToBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.showHowTo();
                    }
                }
            });

            holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.downloadBtn.setVisibility(View.GONE);
                    mClickedPasteBtn = true;
                    if (callback != null) {
                        callback.onDownloadFromClipboard();
                    }
                }
            });

            if (mClickedPasteBtn) {
                holder.downloadBtn.setVisibility(View.GONE);
            } else {
                if (VideoDownloadFactory.getInstance().needShowPasteBtn(Utils.getTextFromClipboard())) {
                    holder.downloadBtn.setVisibility(View.VISIBLE);
                } else {
                    holder.downloadBtn.setVisibility(View.GONE);
                }
            }
        }

    }

    //TODO:最后一个位置有问题
    private void deleteDownloadingVideo(final DownloadContentItem bean, int positoin) {
        mDataList.remove(bean);
        notifyItemRemoved(positoin);
        DownloadingTaskList.SINGLETON.intrupted(bean.pageURL);
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                DownloaderDBHelper.SINGLETON.deleteDownloadTask(bean.pageURL);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).itemType;
    }


    public interface IBtnCallback {
        public void showHowTo();

        void onDownloadFromClipboard();

    }

}
