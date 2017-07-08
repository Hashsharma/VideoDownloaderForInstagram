package com.zxmark.videodownloader.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bcgdv.asia.lib.fanmenu.FanMenuButtons;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.ads.AdChoicesView;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.EventUtil;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.PopWindowUtils;
import com.zxmark.videodownloader.util.Utils;

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
    private String mLeftDownloadFileString;

    public MainDownloadingRecyclerAdapter(RequestManager requestManager,List<DownloadContentItem> dataList, boolean isFullImage, IBtnCallback callback) {
        mDataList = dataList;
        imageLoader = requestManager;
        mFullImageState = isFullImage;
        mContext = MainApplication.getInstance().getApplicationContext();
        this.callback = callback;
        mClickedPasteBtn = false;
        mResources = mContext.getResources();
        mLeftDownloadFileString = mResources.getString(R.string.downloading_left_task_count);
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
            if (holder.fanMenuButtons.getVisibility() == View.VISIBLE) {
                holder.fanMenuButtons.toggleShow();
            }
            holder.operationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.fanMenuButtons.toggleShow();
                }
            });
            if (DownloadingTaskList.SINGLETON.isPendingDownloadTask(bean.pageURL)) {
                holder.progressBar.setProgress(0);
                holder.progressBar.setVisibility(View.VISIBLE);
            } else {
                holder.progressBar.setVisibility(View.GONE);
            }
            holder.fanMenuButtons.setOnFanButtonClickListener(new FanMenuButtons.OnFanClickListener() {
                @Override
                public void onFanButtonClicked(int index) {
                    holder.fanMenuButtons.toggleShow();
                    if (index == 0) {
                        EventUtil.getDefault().onEvent("downloading", "startDownload");
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                        DownloadUtil.startResumeDownload(bean.pageURL);
                    } else if (index == 1) {
                        EventUtil.getDefault().onEvent("downloading", "downloadPageThumbnail");
                        DownloadUtil.downloadThumbnail(bean.pageURL, bean.pageThumb);
                    } else if (index == 2) {
                        EventUtil.getDefault().onEvent("downloading", "delete");
                        deleteDownloadingVideo(bean);
                        sendDeleteVideoBroadcast(bean.pageURL);
                    }
                }
            });

            holder.thumbnailView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean.pageStatus == DownloadContentItem.PAGE_STATUS_DOWNLOAD_FAILED) {
                        bean.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOADING;
                        EventUtil.getDefault().onEvent("downloading", "startDownloadFromThumbnail");
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                        DownloadUtil.startResumeDownload(bean.pageURL);
                    } else if (bean.pageStatus == DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED) {
                        EventUtil.getDefault().onEvent("downloading", "openFileList");
                        DownloadUtil.openFileList(bean.pageHOME);
                    }
                }
            });
            if (TextUtils.isEmpty(bean.pageTags)) {
                holder.hashTagView.setVisibility(View.GONE);
            } else {
                holder.hashTagView.setVisibility(View.VISIBLE);
                holder.hashTagView.setText(bean.pageTags);
            }
            holder.taskCountView.setText(String.format(mLeftDownloadFileString, bean.fileCount));
            holder.playView.setVisibility(bean.mimeType == bean.PAGE_MIME_TYPE_VIDEO ? View.VISIBLE : View.GONE);
            try {
                imageLoader.load(bean.pageThumb).centerCrop().priority(Priority.HIGH).thumbnail(0.1f).crossFade().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.thumbnailView);
            } catch (OutOfMemoryError error) {
                System.gc();
                System.gc();
                System.gc();
            }
            if (TextUtils.isEmpty(bean.pageTitle)) {
                holder.titleTv.setVisibility(View.GONE);
            } else {
                holder.titleTv.setText(bean.pageTitle);
                holder.titleTv.setVisibility(View.VISIBLE);
            }

            if (bean.pageStatus == DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED) {
                holder.moreIv.setVisibility(View.VISIBLE);
                holder.taskCountView.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                holder.operationBtn.setVisibility(View.GONE);
                holder.moreIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopWindowUtils.showVideoMoreOptionWindow(v, true, new MainListRecyclerAdapter.IPopWindowClickCallback() {
                            @Override
                            public void onCopyAll() {
                                EventUtil.getDefault().onEvent("downloading", "copyAll");
                                String title = bean.pageTitle;
                                String hashTags = bean.pageTags;
                                StringBuilder sb = new StringBuilder(bean.pageURL);
                                if (!TextUtils.isEmpty(title)) {
                                    sb.append(title);
                                }

                                if (!TextUtils.isEmpty(hashTags)) {
                                    sb.append(hashTags);
                                }

                                Utils.copyText2Clipboard(sb.toString());

                            }

                            @Override
                            public void onCopyHashTags() {
                                EventUtil.getDefault().onEvent("downloading", "copyHashTags");
                                String hashTags = bean.pageTags;
                                StringBuilder sb = new StringBuilder();
                                if (!TextUtils.isEmpty(hashTags)) {
                                    sb.append(hashTags);
                                    Utils.copyText2Clipboard(sb.toString());
                                }
                            }

                            @Override
                            public void launchAppByUrl() {
                                EventUtil.getDefault().onEvent("downloading", "launchInstagramByURL");
                                if (bean != null && !TextUtils.isEmpty(bean.pageURL)) {
                                    Utils.openInstagramByUrl(bean.pageURL);
                                }
                            }

                            @Override
                            public void onPasteSharedUrl() {
                                EventUtil.getDefault().onEvent("downloading", "pasteURL");
                                if (bean != null && !TextUtils.isEmpty(bean.pageURL)) {
                                    Utils.copyText2Clipboard(bean.pageURL);
                                }

                            }

                            @Override
                            public void onShare() {
                            }

                            @Override
                            public void onStartDownload() {
                                EventUtil.getDefault().onEvent("downloading", "delete");
                                deleteDownloadingVideo(bean);
                                sendDeleteVideoBroadcast(bean.pageURL);
                            }
                        });
                    }
                });
            } else {
                holder.moreIv.setVisibility(View.GONE);
            }
        } else if (baseHolder instanceof NativeAdItemHolder) {
            final NativeAdItemHolder holder = (NativeAdItemHolder) baseHolder;
            if (bean.facebookNativeAd != null) {
                AdChoicesView adChoicesView = new AdChoicesView(mContext, bean.facebookNativeAd, true);
                if (holder.adChoiceView.getChildCount() == 0) {
                    holder.adChoiceView.addView(adChoicesView);
                }
                try {
                    imageLoader.load(bean.facebookNativeAd.getAdCoverImage().getUrl()).thumbnail(0.1f).priority(Priority.HIGH).crossFade().into(holder.adCoverView);
                    imageLoader.load(bean.facebookNativeAd.getAdIcon().getUrl()).priority(Priority.HIGH).crossFade().into(holder.adIconView);
                } catch (OutOfMemoryError error) {
                    System.gc();
                    System.gc();
                    System.gc();
                }
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
                    EventUtil.getDefault().onEvent("main", "howto");
                    if (callback != null) {
                        callback.showHowTo();
                    }
                }
            });

            holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickedPasteBtn = true;
                    EventUtil.getDefault().onEvent("download", "Click Main Paste to Download");
                    if (callback != null) {
                        callback.onDownloadFromClipboard(holder.inputUrl, holder.inputUrl.getText().toString());
                    }

                    holder.inputUrl.setText("");
                }
            });
        }

    }


    private void sendDeleteVideoBroadcast(String pageURL) {
        Intent intent = new Intent(Globals.ACTION_NOTIFY_DATA_CHANGED);
        intent.putExtra(Globals.KEY_BEAN_PAGE_URL, pageURL);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    //TODO:最后一个位置有问题
    private void deleteDownloadingVideo(final DownloadContentItem bean) {

        int index = mDataList.indexOf(bean);
        if (index > -1) {
            notifyItemRemoved(index);
            mDataList.remove(index);
        }
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

        void onDownloadFromClipboard(View view, String httpURL);

    }

}
