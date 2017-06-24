package com.zxmark.videodownloader.adapter;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.main.GalleryPagerActivity;
import com.zxmark.videodownloader.main.ImageGalleryActivity;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.MimeTypeUtil;
import com.zxmark.videodownloader.widget.MobMediaView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by fanlitao on 6/21/17.
 */

public class ImageGalleryPagerAdapter extends PagerAdapter {


    private List<GalleryPagerActivity.PagerBean> mDataList;
    private LinkedList<MobMediaView> mPageViewList;
    private RequestManager mImageLoader;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public ImageGalleryPagerAdapter(Context context, List<GalleryPagerActivity.PagerBean> dataList) {
        mDataList = dataList;
        mImageLoader = Glide.with(context);
        mPageViewList = new LinkedList<>();
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;

    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        MobMediaView view = (MobMediaView) object;
        container.removeView(view);
        mPageViewList.addLast(view);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GalleryPagerActivity.PagerBean bean = mDataList.get(position);
        MobMediaView convertView = null;
        if (mPageViewList.size() > 0) {
            convertView = mPageViewList.removeFirst();
        } else {
            convertView = new MobMediaView(mContext);
        }

        convertView.setTag(position);
        if (bean.file != null) {
            convertView.setMediaSource(bean.file.getAbsolutePath());
        } else {
            LogUtil.e("view","facebookNativeAd:" + bean.facebookNativeAd);
            convertView.setAdSource(bean.facebookNativeAd);
        }
        container.addView(convertView);
        return convertView;
    }
}
