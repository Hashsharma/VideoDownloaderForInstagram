package com.zxmark.videodownloader.main;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PopWindowUtils;
import com.zxmark.videodownloader.util.Utils;

/**
 * Created by fanlitao on 17/6/15.
 */

public class ImageGalleryActivity extends Activity implements View.OnClickListener {


    private Uri mImageUri;
    private String mImagePath;

    private ImageView mImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.image_play);

        mImageView = (ImageView) findViewById(R.id.image);

        //  4.  videoview 的设置

        mImageUri = getIntent().getData();
        mImagePath = getIntent().getStringExtra(Globals.EXTRAS);

        Glide.with(this).load(mImageUri).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mImageView);

        findViewById(R.id.more_vert).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.more_vert) {
            PopWindowUtils.showPlayVideoMorePopWindow(v, new PopWindowUtils.IPopWindowCallback() {
                @Override
                public void onShare() {
                    VideoBean videoBean = DBHelper.getDefault().getVideoInfoByPath(mImagePath);
                    if (videoBean != null) {
                        Utils.startShareIntent(videoBean);
                    } else {
                        Utils.startShareIntent(mImagePath);
                    }
                }

                @Override
                public void launchInstagram() {
                    VideoBean videoBean = DBHelper.getDefault().getVideoInfoByPath(mImagePath);
                    if (videoBean != null) {
                        Utils.openInstagramByUrl(videoBean.appPageUrl);
                    }

                }

                @Override
                public void onPastePageUrl() {
                    VideoBean videoBean = DBHelper.getDefault().getVideoInfoByPath(mImagePath);
                    if (videoBean != null) {
                        Utils.copyText2Clipboard(videoBean.appPageUrl);
                    }
                }
            });
        }
    }
}
