package com.zxmark.videodownloader.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
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
import com.facebook.ads.AdChoicesView;
import com.zxmark.videodownloader.DownloaderBean;
import com.zxmark.videodownloader.MainApplication;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.util.DownloadUtil;
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

    private List<VideoBean> mDataList;
    private RequestManager imageLoader;
    private boolean mFullImageState = false;
    private Context mContext;
    private DBHelper mDBHelper;

    public MainListRecyclerAdapter(List<VideoBean> dataList, boolean isFullImage) {
        mDataList = dataList;
        imageLoader = Glide.with(MainApplication.getInstance().getApplicationContext());
        mFullImageState = isFullImage;
        mDBHelper = DBHelper.getDefault();
        mContext = MainApplication.getInstance().getApplicationContext();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == MainDownloadingRecyclerAdapter.VIEW_TYPE_AD) {
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
        final VideoBean bean = mDataList.get(position);

        if (baseHolder instanceof ItemViewHolder) {
            ItemViewHolder holder = (ItemViewHolder) baseHolder;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadUtil.openVideo(bean.videoPath);
                }
            });

            holder.titleTv.setText(bean.pageTitle);

            holder.playView.setVisibility(MimeTypeUtil.isVideoType(bean.videoPath) ? View.VISIBLE : View.GONE);
            holder.repostView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShareActionUtil.startInstagramShare(MainApplication.getInstance().getApplicationContext(), bean.videoPath);
                }
            });

            imageLoader.load(bean.videoPath).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.thumbnailView);
            holder.moreIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopWindowUtils.showVideoMoreOptionWindow(v, new IPopWindowClickCallback() {
                        @Override
                        public void onDelete() {
                            bean.file.delete();
                            mDataList.remove(bean);
                            DBHelper.getDefault().deleteDownloadingVideo(bean.file.getAbsolutePath());
                            notifyDataSetChanged();
                        }

                        @Override
                        public void launchAppByUrl() {
                            if (bean != null && !TextUtils.isEmpty(bean.appPageUrl)) {
                                Utils.openInstagramByUrl(bean.appPageUrl);
                            }
                        }

                        @Override
                        public void onPasteSharedUrl() {
                            if (bean != null && !TextUtils.isEmpty(bean.appPageUrl)) {
                                Utils.copyText2Clipboard(bean.sharedUrl);
                            }
                        }

                        @Override
                        public void onShare() {
                            Utils.startShareIntent(bean.file.getAbsolutePath());
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


                imageLoader.load(bean.facebookNativeAd.getAdCoverImage().getUrl()).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        holder.adCoverView.setBackgroundDrawable(new BitmapDrawable(resource));
                    }
                });
                holder.adBtn.setText(bean.facebookNativeAd.getAdCallToAction());
                holder.adTitle.setText(bean.facebookNativeAd.getAdTitle());
                // Register the native ad view with the native ad instance
                bean.facebookNativeAd.registerViewForInteraction(holder.itemView);

            }
        }


    }

    public interface IPopWindowClickCallback {
        void onDelete();

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
        return mDataList.get(position).type;
    }
}
