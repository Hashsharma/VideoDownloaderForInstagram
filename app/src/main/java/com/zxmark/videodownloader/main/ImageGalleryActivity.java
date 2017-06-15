package com.zxmark.videodownloader.main;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zxmark.videodownloader.R;
import com.zxmark.videodownloader.util.LogUtil;

/**
 * Created by fanlitao on 17/6/15.
 */

public class ImageGalleryActivity extends Activity {






    private Uri mImagePath;

    private ImageView  mImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.image_play);

        mImageView =  (ImageView) findViewById(R.id.image);

        //  4.  videoview 的设置

        mImagePath  = getIntent().getData();


        Glide.with(this).load(mImagePath).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mImageView);
    }
}
