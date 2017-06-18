package com.zxmark.videodownloader.main;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import com.imobapp.videodownloaderforinstagram.R;
import com.umeng.analytics.MobclickAgent;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PopWindowUtils;
import com.zxmark.videodownloader.util.Utils;

/**
 * Created by fanlitao on 17/6/15.
 */

public class VideoPlayActivity extends Activity  implements View.OnClickListener{




    private VideoView mVideoView;


    private Uri mVideoURI;
    private String mVideoPath;

    private boolean mErrorHappened = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.video_play);

        mVideoView = (VideoView) findViewById(R.id.videoView);

        //  4.  videoview 的设置

        mVideoURI  = getIntent().getData();
        mVideoPath = getIntent().getStringExtra(Globals.EXTRAS);
        //  4.1  获取MediaController对象，控制媒体播放，这里应该是获取 android.widget.MediaController 的对象
        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        //  4.2  绑定到 Video View
        mVideoView.setMediaController(mediaController);
        //  4.3  设置 URI，播放源
//        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/hehe2.mp4");
//        videoView.setVideoURI(uri);
        LogUtil.e("play","mVideoPath=" + mVideoURI);
        mVideoView.setVideoURI(mVideoURI);
        //  4.4  开始播放
        mVideoView.start();
        //  4.5  获取焦点
        mVideoView.requestFocus();

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mErrorHappened = true;
                return false;
            }
        });


        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(mErrorHappened) {
                    return;
                }
                mVideoView.setVideoURI(mVideoURI);
                mVideoView.start();
            }
        });


        findViewById(R.id.more_vert).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        if(v.getId() == R.id.more_vert) {
            PopWindowUtils.showPlayVideoMorePopWindow(v,new PopWindowUtils.IPopWindowCallback() {
                @Override
                public void onShare() {
                    VideoBean videoBean  = DBHelper.getDefault().getVideoInfoByPath(mVideoPath);
                    if(videoBean  != null) {
                        Utils.startShareIntent(videoBean);
                    }
                }

                @Override
                public void launchInstagram() {
                    VideoBean videoBean  = DBHelper.getDefault().getVideoInfoByPath(mVideoPath);
                    if(videoBean  != null) {
                        Utils.openInstagramByUrl(videoBean.appPageUrl);
                    }

                }

                @Override
                public void onPastePageUrl() {

                    VideoBean videoBean  = DBHelper.getDefault().getVideoInfoByPath(mVideoPath);
                    if(videoBean  != null) {
                        Utils.copyText2Clipboard(videoBean.appPageUrl);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


}
