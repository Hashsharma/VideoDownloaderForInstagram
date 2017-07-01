package com.zxmark.videodownloader.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Util;
import com.duapps.ad.DuNativeAd;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.NativeAd;
import com.zxmark.videodownloader.DownloaderBean;
import com.zxmark.videodownloader.MainApplication;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.EventUtil;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.MimeTypeUtil;
import com.zxmark.videodownloader.util.PopWindowUtils;
import com.zxmark.videodownloader.util.ShareActionUtil;
import com.zxmark.videodownloader.util.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by fanlitao on 17/6/7.
 */

public class MainListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DownloadContentItem> mDataList;
    private RequestManager imageLoader;
    private boolean mFullImageState = false;
    private Context mContext;
    private DBHelper mDBHelper;

    public MainListRecyclerAdapter(List<DownloadContentItem> dataList, boolean isFullImage) {
        mDataList = dataList;
        imageLoader = Glide.with(MainApplication.getInstance().getApplicationContext());
        mFullImageState = isFullImage;
        mDBHelper = DBHelper.getDefault();
        mContext = MainApplication.getInstance().getApplicationContext();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == DownloadContentItem.TYPE_FACEBOOK_AD) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.facebook_native_item_2,
                            parent, false);
            return new NativeAdItemHolder2(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(mFullImageState ? R.layout.item_layout2 : R.layout.item_layout,
                            parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder baseHolder, int position) {
        final DownloadContentItem bean = mDataList.get(position);

        if (baseHolder instanceof ItemViewHolder) {
            ItemViewHolder holder = (ItemViewHolder) baseHolder;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventUtil.getDefault().onEvent("history", "openFileList");
                    DownloadUtil.openFileList(bean.pageHOME);
                }
            });

            holder.titleTv.setText(bean.pageTitle);
            final boolean isVideo = bean.mimeType == bean.PAGE_MIME_TYPE_VIDEO;
            if (isVideo) {
                holder.playView.setVisibility(View.VISIBLE);
            } else {
                holder.playView.setVisibility(View.GONE);
            }

            holder.albumView.setVisibility(bean.fileCount > 1 ? View.VISIBLE : View.GONE);
            try {
                if (TextUtils.isEmpty(bean.pageThumb)) {
                    File file = new File(bean.pageHOME);
                    if (file != null && file.listFiles() != null && file.listFiles().length > 0) {
                        String path = file.listFiles()[0].getAbsolutePath();
                        if (MimeTypeUtil.isVideoType(path)) {
                            imageLoader.load(path).into(holder.thumbnailView);
                        }
                    }

                } else {
                    imageLoader.load(bean.pageThumb).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.thumbnailView);
                }
            } catch (OutOfMemoryError error) {
                System.gc();
                System.gc();
                System.gc();
            }
            holder.repostView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventUtil.getDefault().onEvent("history", "delete");
                    int index = mDataList.indexOf(bean);
                    notifyItemRemoved(index);
                    mDataList.remove(index);
                    sendDeleteVideoBroadcast(bean.pageURL);
                    DownloaderDBHelper.SINGLETON.deleteDownloadTaskAsync(bean.pageURL);
                }
            });

            if (TextUtils.isEmpty(bean.pageTags)) {
                holder.hashTagView.setVisibility(View.GONE);
            } else {
                holder.hashTagView.setVisibility(View.VISIBLE);
                holder.hashTagView.setText(bean.pageTags);
            }

            holder.moreIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopWindowUtils.showVideoMoreOptionWindow(v, new IPopWindowClickCallback() {
                        @Override
                        public void onCopyAll() {

                            EventUtil.getDefault().onEvent("history", "copyAll");
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

                            EventUtil.getDefault().onEvent("history", "copyHashTags");
                            String hashTags = bean.pageTags;
                            StringBuilder sb = new StringBuilder();

                            if (!TextUtils.isEmpty(hashTags)) {
                                sb.append(hashTags);
                                Utils.copyText2Clipboard(sb.toString());
                            }
                        }

                        @Override
                        public void launchAppByUrl() {

                            EventUtil.getDefault().onEvent("history", "launchInstagramByURL");
                            if (bean != null && !TextUtils.isEmpty(bean.pageURL)) {
                                Utils.openInstagramByUrl(bean.pageURL);
                            }
                        }

                        @Override
                        public void onPasteSharedUrl() {

                            EventUtil.getDefault().onEvent("history", "pasteURL");
                            if (bean != null && !TextUtils.isEmpty(bean.pageURL)) {
                                Utils.copyText2Clipboard(bean.pageURL);
                            }

                        }

                        @Override
                        public void onShare() {
                        }
                    });
                }
            });
        } else if (baseHolder instanceof NativeAdItemHolder2) {
            final NativeAdItemHolder2 holder = (NativeAdItemHolder2) baseHolder;
            if (bean.facebookNativeAd != null) {
                AdChoicesView adChoicesView = new AdChoicesView(mContext, bean.facebookNativeAd, true);
                if (holder.adChoiceView.getChildCount() == 0) {
                    holder.adChoiceView.addView(adChoicesView);
                }

                try {
                    imageLoader.load(bean.facebookNativeAd.getAdCoverImage().getUrl()).asBitmap().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            holder.adCoverView.setBackgroundDrawable(new BitmapDrawable(resource));
                        }
                    });
                } catch (OutOfMemoryError error) {
                    System.gc();
                    System.gc();
                    System.gc();
                }
                holder.adBtn.setText(bean.facebookNativeAd.getAdCallToAction());
                holder.adTitle.setText(bean.facebookNativeAd.getAdTitle());
                // Register the native ad view with the native ad instance
                bean.facebookNativeAd.registerViewForInteraction(holder.itemView);

            } else if (bean.duNativeAd != null) {
                if (bean.duNativeAd.getAdChannelType() == DuNativeAd.CHANNEL_TYPE_FB) {
                    AdChoicesView adChoicesView = new AdChoicesView(mContext,(NativeAd)bean.duNativeAd.getRealSource().getRealData(), true);
                    if (holder.adChoiceView.getChildCount() == 0) {
                        holder.adChoiceView.addView(adChoicesView);
                    }
                }

                try {
                    imageLoader.load(bean.duNativeAd.getImageUrl()).asBitmap().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            holder.adCoverView.setBackgroundDrawable(new BitmapDrawable(resource));
                        }
                    });
                } catch (OutOfMemoryError error) {
                    System.gc();
                    System.gc();
                    System.gc();
                }
                holder.adBtn.setText(bean.duNativeAd.getCallToAction());
                holder.adTitle.setText(bean.duNativeAd.getTitle());
                // Register the native ad view with the native ad instance
                bean.duNativeAd.registerViewForInteraction(holder.itemView);
            }
        }
    }


    private void sendDeleteVideoBroadcast(String pageURL) {
        Intent intent = new Intent(Globals.ACTION_NOTIFY_DATA_CHANGED);
        intent.putExtra(Globals.KEY_BEAN_PAGE_URL, pageURL);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    public interface IPopWindowClickCallback {
        void onCopyAll();

        void onCopyHashTags();

        void onShare();

        void launchAppByUrl();

        void onPasteSharedUrl();

    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).itemType;
    }
}
