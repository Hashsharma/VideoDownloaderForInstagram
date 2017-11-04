package com.zxmark.videodownloader.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.ads.NativeAd;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.util.ADCache;
import com.zxmark.videodownloader.widget.MobMediaView;

/**
 * Created by fanlitao on 17/10/30.
 */

public class FullScreenAdActivity extends Activity {


    private MobMediaView mobMediaView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_ad);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mobMediaView = (MobMediaView) findViewById(R.id.full_ad);

        NativeAd nativeAd = ADCache.getDefault().getFullScreenNativeAd();

        if (nativeAd != null) {
            GalleryPagerActivity.PagerBean pagerBean = new GalleryPagerActivity.PagerBean();
            pagerBean.facebookNativeAd = nativeAd;
            mobMediaView.setAdSource(pagerBean);
        }
    }
}
