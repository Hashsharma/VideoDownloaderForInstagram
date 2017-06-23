package com.zxmark.videodownloader.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.component.PinchImageView;
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

    private RequestManager mImageLoader;

    private boolean mIsVideoMimeType = false;

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
        LogUtil.e("view", "mImageView=" + mImageView);

    }

    public void setMediaSource(String source) {
        mMediaSource = source;
        LogUtil.e("view", "source=" + source);
        initSelfByMimeType();
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
            LogUtil.v("view","getTag=" + getTag() + ":" + getTag().equals(0));
            if(getTag().equals(0)) {
                playVideo(videoPath);
            }
        } else {
            mVideoIcon.setVisibility(View.GONE);
            if (mVideoView != null && mVideoView.getVisibility() == View.VISIBLE) {
                mVideoView.setVisibility(View.GONE);
            }
            if (mImageView != null) {
                mImageView.reset();
                mImageLoader.load(mMediaSource).diskCacheStrategy(DiskCacheStrategy.RESULT).into(mImageView);
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
