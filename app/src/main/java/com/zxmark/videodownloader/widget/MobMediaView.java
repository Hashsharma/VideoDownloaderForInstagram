package com.zxmark.videodownloader.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.NativeAd;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.component.PinchImageView;
import com.zxmark.videodownloader.main.GalleryPagerActivity;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.MimeTypeUtil;

/**
 * Created by fanlitao on 6/21/17.
 */

public class MobMediaView extends FrameLayout {


    public static final String TYPE_AD = "IMobAppAD";
    public static final String TYPE_RECOMMEND = "IMobAppRecommend";

    private View mContentView;
    private String mMediaSource;

    private PinchImageView mImageView;
    private VideoView mVideoView;
    private View mVideoIcon;
    private View mAdContainer;

    private RequestManager mImageLoader;

    private boolean mIsVideoMimeType = false;
    private NativeAd mAd;

    public MobMediaView(@NonNull Context context) {
        super(context);
        init(context);
    }


    private void init(Context context) {
        mContentView = LayoutInflater.from(context).inflate(R.layout.media_view_content, null);
        addView(mContentView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mImageLoader = Glide.with(context);

        mImageView = (PinchImageView) mContentView.findViewById(R.id.imageView);
        mVideoIcon = mContentView.findViewById(R.id.video_flag);
        mAdContainer = findViewById(R.id.MainContainer);
        LogUtil.e("view", "mImageView=" + mImageView);

    }

    public void setMediaSource(String source) {
        mAdContainer.setVisibility(View.GONE);
        mMediaSource = source;
        LogUtil.e("view", "source=" + source);
        initSelfByMimeType();
    }

    public void setAdSource(GalleryPagerActivity.PagerBean adBean) {
        if (adBean != null) {
            if (adBean.facebookNativeAd != null) {
                mMediaSource = null;
                LogUtil.v("view", "setAdSource=" + adBean.facebookNativeAd);
                mAdContainer.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);
                if (mVideoView != null) {
                    mVideoView.setVisibility(View.GONE);
                }
                if (mVideoIcon != null) {
                    mVideoIcon.setVisibility(View.GONE);
                }
                // mImageLoader.load(nativeAd.getAdCoverImage().getUrl()).into(mImageView);
                AdChoicesView adChoicesView = new AdChoicesView(getContext(), adBean.facebookNativeAd, true);
                LinearLayout adChoiceView = (LinearLayout) findViewById(R.id.ad_choices_container);
                if (adChoiceView.getChildCount() == 0) {
                    adChoiceView.addView(adChoicesView);
                }
                ImageView adCoverView = (ImageView) findViewById(R.id.ad_cover);
                ImageView adIconView = (ImageView) findViewById(R.id.ad_icon);
                try {
                    mImageLoader.load(adBean.facebookNativeAd.getAdCoverImage().getUrl()).into(adCoverView);
                    mImageLoader.load(adBean.facebookNativeAd.getAdIcon().getUrl()).into(adIconView);

                } catch (OutOfMemoryError error) {
                    System.gc();
                    System.gc();
                    System.gc();
                }
                TextView adBodyView = (TextView) findViewById(R.id.ad_body);
                TextView adTitleView = (TextView) findViewById(R.id.ad_title);
                adBodyView.setText(adBean.facebookNativeAd.getAdBody());
                adTitleView.setText(adBean.facebookNativeAd.getAdTitle());
                // Register the native ad view with the native ad instance

                Button adButton = (Button) findViewById(R.id.facebook_ad_btn);
                adButton.setText(adBean.facebookNativeAd.getAdCallToAction());
                adBean.facebookNativeAd.registerViewForInteraction(mAdContainer);
            }
        }
    }

    private void initSelfByMimeType() {
        mIsVideoMimeType = MimeTypeUtil.isVideoType(mMediaSource);
        if (mIsVideoMimeType) {
            if (mVideoView == null) {
                mVideoView = (VideoView) mContentView.findViewById(R.id.videoView);

                android.widget.MediaController mediaController = new android.widget.MediaController(getContext());
                //  4.2  绑定到 Video View
                mVideoView.setMediaController(mediaController);
            }
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            mVideoIcon.setVisibility(View.VISIBLE);
            mImageLoader.load(mMediaSource).into(mImageView);
            final String videoPath = mMediaSource;
            mVideoIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo(videoPath);
                }
            });
            if (getTag().equals(0)) {
                playVideo(videoPath);
            }
        } else {
            mVideoIcon.setVisibility(View.GONE);
            if (mVideoView != null && mVideoView.getVisibility() == View.VISIBLE) {
                mVideoView.setVisibility(View.GONE);
            }
            if (mImageView != null) {
                mImageView.reset();
                try {
                    mImageLoader.load(mMediaSource).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mImageView);
                } catch (OutOfMemoryError error) {
                    System.gc();
                    System.gc();
                    System.gc();
                }
            }
        }
    }


    private void playVideo(String videoPath) {
        LogUtil.e("view", "playVideo=" + videoPath);
        mVideoView.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.GONE);
        mVideoIcon.setVisibility(View.GONE);


        mVideoView.setVideoPath(videoPath);
        mVideoView.start();

        //  4.5  获取焦点
        mVideoView.requestFocus();
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
    }


    public void play() {
        if (mIsVideoMimeType) {
            playVideo(mMediaSource);
        }
    }

    public void stop() {
        LogUtil.e("view", "stop:" + mMediaSource);
        if (mIsVideoMimeType) {
            if (mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
            }
            mVideoIcon.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    public void pause() {
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    public void resume() {
        if (mVideoView != null && mVideoView.getVisibility() == View.VISIBLE) {
            mVideoView.resume();
        }
    }

    public String getMediaSource() {
        return mMediaSource;
    }

    public void destory() {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        mImageView = (ImageView) findViewById(R.id.imageView);
//        if(!TextUtils.isEmpty(mMediaSource)) {
//            mImageLoader.load(mMediaSource).diskCacheStrategy(DiskCacheStrategy.RESULT).into(mImageView);
//        }
    }
}
