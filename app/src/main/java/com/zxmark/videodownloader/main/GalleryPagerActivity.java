package com.zxmark.videodownloader.main;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.BaseActivity;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.adapter.ImageGalleryPagerAdapter;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.util.FileComparator;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PopWindowUtils;
import com.zxmark.videodownloader.util.ShareActionUtil;
import com.zxmark.videodownloader.util.Utils;
import com.zxmark.videodownloader.widget.MobMediaView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fanlitao on 6/21/17.
 */

public class GalleryPagerActivity extends BaseActivity implements View.OnClickListener {

    public static final int MAX_COUNT_THREHOLD = 3;

    private ViewPager mMainViewPager;
    private TextView mCountInfoView;

    private ImageGalleryPagerAdapter mAdapter;
    private List<PagerBean> mDataList;
    private MobMediaView mSelectedMobView;

    private String mPageHome;

    private NativeAd nativeAd;

    private int mSelectedPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery_pager);

        final String baseHome = getIntent().getStringExtra(Globals.EXTRAS);

        mPageHome = baseHome;
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.more_vert).setOnClickListener(this);
        mCountInfoView = (TextView) findViewById(R.id.count_info);
        mMainViewPager = (ViewPager) findViewById(R.id.viewPager);
        mMainViewPager.setOffscreenPageLimit(3);
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mSelectedPosition = position;
                MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(position);
                mSelectedMobView = itemView;
                mCountInfoView.setText(getResources().getString(R.string.file_count_format, 1 + position, mDataList.size()));
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
                        mDataList = new ArrayList<PagerBean>();
                        for (File file : targetFile.listFiles()) {
                            PagerBean bean = new PagerBean();
                            bean.file = file;
                            mDataList.add(bean);
                        }

                        Collections.sort(mDataList, new PagerBeanComparator());
                        mAdapter = new ImageGalleryPagerAdapter(GalleryPagerActivity.this, mDataList);
                        mMainViewPager.setAdapter(mAdapter);
                        if (mDataList.size() == 1) {
                            mCountInfoView.setVisibility(View.GONE);
                        }
                        mCountInfoView.setText(getResources().getString(R.string.file_count_format, 1, mDataList.size()));
                        if (mDataList.size() > MAX_COUNT_THREHOLD) {
                            showNativeAd();
                        }
                    }
                });
            }
        });
    }

    private void showNativeAd() {
        nativeAd = new NativeAd(this, "2099565523604162_2099565860270795");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                LogUtil.v("facebook", "onError:" + adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                onFacebookAdLoaded(ad);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        nativeAd.loadAd();
    }

    // The next step is to extract the ad metadata and use its properties
// to build your customized native UI. Modify the onAdLoaded function
// above to retrieve the ad properties. For example:
    public void onFacebookAdLoaded(Ad ad) {
        if (ad != nativeAd) {
            return;
        }


        PagerBean adBean = new PagerBean();
        adBean.facebookNativeAd = nativeAd;
        int count = mDataList.size();
        LogUtil.v("view", "onFacebookAdLoaded:" + nativeAd);
        mDataList.add(count - 1, adBean);
        mAdapter.notifyDataSetChanged();
    }

    public static class PagerBean {
        public File file;
        public NativeAd facebookNativeAd;
    }

    public class PagerBeanComparator implements Comparator<PagerBean> {


        @Override
        public int compare(PagerBean o1, PagerBean o2) {

            return (int) (o2.file.lastModified() - o1.file.lastModified());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.more_vert) {
            PopWindowUtils.showPlayVideoMorePopWindow(v, new PopWindowUtils.IPopWindowCallback() {

                @Override
                public void onRepost() {
                    MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(mSelectedPosition);
                    ShareActionUtil.startInstagramShare(MainApplication.getInstance().getApplicationContext(), itemView.getMediaSource());
                }

                @Override
                public void onShare() {
                    MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(mSelectedPosition);
                    Utils.startShareIntent(itemView.getMediaSource());
                }

                @Override
                public void launchInstagram() {
                    DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageHome(mPageHome);
                    if (videoBean != null) {
                        Utils.openInstagramByUrl(videoBean.pageURL);
                    }

                }

                @Override
                public void onPastePageUrl() {
                    DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageHome(mPageHome);
                    if (videoBean != null) {
                        Utils.copyText2Clipboard(videoBean.pageHOME);
                    }
                }
            });
        }
    }
}
