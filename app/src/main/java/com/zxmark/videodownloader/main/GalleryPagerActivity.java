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
import com.zxmark.videodownloader.downloader.KuaiVideoDownloader;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.FileComparator;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.widget.MobMediaView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by fanlitao on 6/21/17.
 */

public class GalleryPagerActivity extends BaseActivity {


    private ViewPager mMainViewPager;

    private ImageGalleryPagerAdapter mAdapter;
    private List<File> mDataList;
    private MobMediaView mSelectedMobView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery_pager);

        final String baseHome = getIntent().getStringExtra(Globals.EXTRAS);

        mMainViewPager = (ViewPager) findViewById(R.id.viewPager);
        mMainViewPager.setOffscreenPageLimit(2);
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(position);
                mSelectedMobView = itemView;
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                    case ViewPager.SCROLL_STATE_SETTLING:
                        if (mSelectedMobView != null) {
                            mSelectedMobView.stop();
                        }
                        break;
                }
            }
        });


        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File targetFile = new File(baseHome);
                        if (targetFile == null) {
                            return;
                        }
                        mDataList = new ArrayList<File>();
                        for (File file : targetFile.listFiles()) {
                            mDataList.add(file);
                        }

                        Collections.sort(mDataList,new FileComparator());

                        mAdapter = new ImageGalleryPagerAdapter(GalleryPagerActivity.this, mDataList);
                        mMainViewPager.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

}
