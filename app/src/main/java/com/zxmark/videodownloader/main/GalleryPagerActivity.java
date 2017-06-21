package com.zxmark.videodownloader.main;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.BaseActivity;
import com.zxmark.videodownloader.adapter.ImageGalleryPagerAdapter;
import com.zxmark.videodownloader.adapter.MainViewPagerAdapter;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.widget.MobMediaView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fanlitao on 6/21/17.
 */

public class GalleryPagerActivity extends BaseActivity {


    private ViewPager mMainViewPager;

    private ImageGalleryPagerAdapter mAdapter;
    private List<String> mDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery_pager);


        mMainViewPager = (ViewPager) findViewById(R.id.viewPager);
        mMainViewPager.setOffscreenPageLimit(2);
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LogUtil.v("view", "onPageScrolled:" + position);
                MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(position);
                LogUtil.e("view","itemView=" + itemView);
                if (itemView != null) {
                    itemView.stop();
                }
            }

            @Override
            public void onPageSelected(int position) {
                mMainViewPager.findViewWithTag(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File targetFile = DownloadUtil.getHomeDirectory();
                        mDataList = new ArrayList<String>();
                        for (File file : targetFile.listFiles()) {
                            mDataList.add(file.getAbsolutePath());
                        }
                        mAdapter = new ImageGalleryPagerAdapter(GalleryPagerActivity.this, mDataList);
                        mMainViewPager.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

}
