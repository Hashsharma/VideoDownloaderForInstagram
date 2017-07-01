package com.zxmark.videodownloader.main;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.duapps.ad.DuAdListener;
import com.duapps.ad.DuNativeAd;
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
import com.zxmark.videodownloader.util.ADCache;
import com.zxmark.videodownloader.util.EventUtil;
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


    public static final int PID = 138166;

    public static final int MAX_COUNT_THREHOLD = 4;

    private ViewPager mMainViewPager;
    private TextView mCountInfoView;

    private ImageGalleryPagerAdapter mAdapter;
    private List<PagerBean> mDataList;
    private MobMediaView mSelectedMobView;

    private String mPageHome;

    private NativeAd nativeAd;

    private int mSelectedPosition = 0;

    private PagerBean mAdBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventUtil.getDefault().onEvent("UI", "GalleryPageActivity.onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery_pager);

        String baseHome = getIntent().getStringExtra(Globals.EXTRAS);
        //  if(Globals.TEST_FOR_GP) {
        // baseHome = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mob_ins_downloader/test";
        //}
        mPageHome = baseHome;
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.more_vert).setOnClickListener(this);
        mCountInfoView = (TextView) findViewById(R.id.count_info);
        mMainViewPager = (ViewPager) findViewById(R.id.viewPager);
        mMainViewPager.setOffscreenPageLimit(2);
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mSelectedPosition = position;
                MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(position);
                mSelectedMobView = itemView;
                if (itemView != null) {
                    itemView.play();
                }
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

        final String home = baseHome;
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File targetFile = new File(home);
                        if (targetFile == null) {
                            return;
                        }
                        mDataList = new ArrayList<PagerBean>();
                        if (targetFile.listFiles() != null && targetFile.listFiles().length > 0) {
                            for (File file : targetFile.listFiles()) {
                                PagerBean bean = new PagerBean();
                                bean.file = file;
                                mDataList.add(bean);
                            }

                            Collections.sort(mDataList, new PagerBeanComparator());
                            if (mDataList.size() > 1 && mDataList.size() < 4) {
                                DownloadContentItem item = ADCache.getDefault().getFacebookNativeAd(ADCache.AD_KEY_GALLERY);
                                if (item == null) {
                                    item = ADCache.getDefault().getFacebookNativeAd(ADCache.AD_KEY_HISTORY_VIDEO);
                                }
                                if (item != null) {
                                    mAdBean = new PagerBean();
                                    if (item.duNativeAd != null) {
                                        mAdBean.duNativeAd = item.duNativeAd;
                                    } else {
                                        mAdBean.facebookNativeAd = item.facebookNativeAd;
                                    }
                                    mDataList.add(mAdBean);
                                }
                            } else {

                            }
                            mAdapter = new ImageGalleryPagerAdapter(GalleryPagerActivity.this, mDataList);
                            mMainViewPager.setAdapter(mAdapter);
                            if (mDataList.size() == 1) {
                                mCountInfoView.setVisibility(View.GONE);
                            }
                            mCountInfoView.setText(getResources().getString(R.string.file_count_format, 1, mDataList.size()));
                            if (mDataList.size() >= MAX_COUNT_THREHOLD) {
                                if (!ADCache.SHOW_AD) {
                                    return;
                                }
                                showNativeAd();
                            }
                        }
                    }
                });
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mDataList != null && mStopCurrentPosition > -1) {
            MobMediaView mobMediaView = (MobMediaView) mMainViewPager.findViewWithTag(mStopCurrentPosition);
            mobMediaView.resume();
        }
    }

    private int mStopCurrentPosition = -1;

    @Override
    protected void onStop() {
        super.onStop();
        if (mDataList != null) {
            mStopCurrentPosition = mSelectedPosition;
            MobMediaView mobMediaView = (MobMediaView) mMainViewPager.findViewWithTag(mStopCurrentPosition);
            if (mobMediaView != null) {
                mobMediaView.stop();
            }
        }

    }

    private DuNativeAd mDuNativeAd;

    private void showNativeAd() {
        if (mAdBean == null) {
            mDuNativeAd = new DuNativeAd(this, PID, 2);
            mDuNativeAd.setMobulaAdListener(new DuAdListener() {
                @Override
                public void onError(DuNativeAd duNativeAd, com.duapps.ad.AdError adError) {
                    LogUtil.e("facebook", "DuNative:OnError:" + adError);
                    startLoadFacebookAd();
                }

                @Override
                public void onAdLoaded(DuNativeAd duNativeAd) {
                    LogUtil.e("main", "DuAdLoaded.onAdLoaded" + duNativeAd);
                    onDuNativeAdLoaded(duNativeAd);
                }

                @Override
                public void onClick(DuNativeAd duNativeAd) {

                }
            });
            mDuNativeAd.load();
//
        }
    }


    private void startLoadFacebookAd() {
        if (isFinishing()) {
            return;
        }
        nativeAd = new NativeAd(this, "2099565523604162_2105972009630180");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                LogUtil.e("facebook", "onError:" + adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                onFacebookAdLoaded(ad);
            }

            @Override
            public void onAdClicked(Ad ad) {
                if (mAdBean != null) {
                    ADCache.getDefault().removedAdByKey(ADCache.AD_KEY_GALLERY);
                }
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
        DownloadContentItem downloadContentItem = new DownloadContentItem();
        downloadContentItem.itemType = DownloadContentItem.TYPE_FACEBOOK_AD;
        downloadContentItem.facebookNativeAd = nativeAd;
        ADCache.getDefault().setFacebookNativeAd(ADCache.AD_KEY_GALLERY, downloadContentItem);
        mDataList.add(adBean);
        mAdapter.notifyDataSetChanged();
    }

    private void onDuNativeAdLoaded(DuNativeAd duNativeAd) {
        PagerBean adBean = new PagerBean();
        adBean.duNativeAd = duNativeAd;
        DownloadContentItem downloadContentItem = new DownloadContentItem();
        downloadContentItem.itemType = DownloadContentItem.TYPE_FACEBOOK_AD;
        downloadContentItem.duNativeAd = duNativeAd;
        ADCache.getDefault().setFacebookNativeAd(ADCache.AD_KEY_GALLERY, downloadContentItem);
        mDataList.add(adBean);
        mAdapter.notifyDataSetChanged();
    }

    public static class PagerBean {
        public File file;
        public NativeAd facebookNativeAd;
        public DuNativeAd duNativeAd;


        @Override
        public boolean equals(Object obj) {

            if (obj instanceof PagerBean) {
                PagerBean right = (PagerBean) obj;
                if (file != null && right.file != null) {
                    return file.getAbsolutePath().equals(right.file.getAbsolutePath());
                }

                if ((file != null && right.file == null) || (file == null && right.file != null)) {
                    return false;
                }

            }
            return false;
        }
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
                public void onDelete() {
                    MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(mSelectedPosition);
                    String filePath = itemView.getMediaSource();
                    PagerBean bean = new PagerBean();
                    bean.file = new File(filePath);
                    mAdapter.deleteItem(bean, itemView);
                    mMainViewPager.setAdapter(mAdapter);

                    if(mDataList.size() == 0) {
                        GalleryPagerActivity.this.finish();
                        return;
                    }
                    if (mDataList.size() == 1) {
                        mCountInfoView.setVisibility(View.GONE);
                    }
                    mCountInfoView.setText(getResources().getString(R.string.file_count_format, 1, mDataList.size()));
                }

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
