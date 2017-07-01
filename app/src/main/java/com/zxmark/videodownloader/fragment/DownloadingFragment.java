package com.zxmark.videodownloader.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.duapps.ad.DuAdListener;
import com.duapps.ad.DuNativeAd;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.adapter.ItemViewHolder;
import com.zxmark.videodownloader.adapter.MainDownloadingRecyclerAdapter;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.downloader.VideoDownloadFactory;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.util.ADCache;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PreferenceUtils;
import com.zxmark.videodownloader.util.URLMatcher;
import com.zxmark.videodownloader.widget.IToast;

import java.util.List;

// In the Activity that will launch the native ad,
// implement the AdListener interface and add the following:

/**
 * Created by fanlitao on 17/6/13.
 */

public class DownloadingFragment extends Fragment implements View.OnClickListener, MainDownloadingRecyclerAdapter.IBtnCallback {


    private static final int AD_PID = 138164;

    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private MainDownloadingRecyclerAdapter mAdapter;
    private List<DownloadContentItem> mDataList;
    private ProgressDialog mProgressDialog;

    public String mReceiveUrlParams;

    private NativeAd nativeAd;

    private View mFacebookAdViewContainer;
    private RequestManager mGlide;
    private DownloadContentItem mFirstAdBean;
    private DownloadContentItem mHowToBean = null;

    private boolean isShowHowToPage;

    private String mFormatLeftFileString;

    private DuNativeAd mDuNativeAd;


    private Handler mHandler = new Handler() {

    };

    private BroadcastReceiver mUpdateDataReceiver;

    public static DownloadingFragment newInstance(String params) {
        DownloadingFragment fragment = new DownloadingFragment();
        fragment.mReceiveUrlParams = params;
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.downloading_page, container, false);

        return view;
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerLocalBroadcast();
        mGlide = Glide.with(getActivity());

        mFacebookAdViewContainer = findViewById(R.id.main_ad_container);
        mListView = (RecyclerView) findViewById(R.id.downloading_list);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        mListView.setLayoutManager(mLayoutManager);
        mListView.setItemAnimator(new DefaultItemAnimator());

        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                mDataList = DownloaderDBHelper.SINGLETON.getDownloadingTask();
                DownloadContentItem headerBean = new DownloadContentItem();
                headerBean.itemType = DownloadContentItem.TYPE_HEADER_ITEM;
                mDataList.add(0, headerBean);
                if (!PreferenceUtils.isShowedHowToInfo()) {
                    isShowHowToPage = true;
                    PreferenceUtils.showedHowToInfo();
                    mHowToBean = new DownloadContentItem();
                    mHowToBean.itemType = DownloadContentItem.TYPE_HOWTO_ITEM;
                    mDataList.add(mHowToBean);
                } else {
                    mFirstAdBean = ADCache.getDefault().getFacebookNativeAd(ADCache.AD_KEY_DOWNLOADING_VIDEO);
                    if (mFirstAdBean != null) {
                        if (mDataList.size() <= 1) {
                            mDataList.add(mFirstAdBean);
                        } else {
                            mDataList.add(2, mFirstAdBean);
                        }
                    }
                }
                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter = new MainDownloadingRecyclerAdapter(mDataList, true, DownloadingFragment.this);
                            mListView.setAdapter(mAdapter);
                            showNativeAd();
                        }
                    });
                }
            }
        });


        if (!TextUtils.isEmpty(mReceiveUrlParams)) {
            receiveSendAction(mReceiveUrlParams);
        }

        mFormatLeftFileString = getResources().getString(R.string.downloading_left_task_count);

    }

    public void receiveSendAction(String url) {
        startDownload(url);
    }

    private void startDownload(final String url) {
        if (isAdded()) {
            String pageURL = URLMatcher.getHttpURL(url);
            if (VideoDownloadFactory.getInstance().isSupportWeb(pageURL)) {
                showCheckURLProgressDialog();
                Intent intent = new Intent(getActivity(), DownloadService.class);
                intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
                intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, false);
                intent.putExtra(Globals.EXTRAS, url);
                getActivity().startService(intent);
            } else {
                IToast.makeText(getActivity(), R.string.not_support_url, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showCheckURLProgressDialog() {
        if (isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = ProgressDialog.show(getActivity(), getActivity().getString(R.string.eheck_url_dialgo_title), getActivity().getString(R.string.check_url), true, true);
                    mProgressDialog.show();
                }
            });
        }
    }


    private void registerLocalBroadcast() {
        if (isAdded()) {
            mUpdateDataReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String pageURL = intent.getStringExtra(Globals.KEY_BEAN_PAGE_URL);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (TextUtils.isEmpty(pageURL)) {
                                return;
                            }
                            DownloadContentItem bean = new DownloadContentItem();
                            bean.pageURL = pageURL;
                            int index = mDataList.indexOf(bean);
                            if (index > -1) {
                                mAdapter.notifyItemRemoved(mDataList.indexOf(bean));
                                mDataList.remove(bean);
                            }
                        }
                    });
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Globals.ACTION_NOTIFY_DATA_CHANGED);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateDataReceiver, intentFilter);
        }
    }

    private void unRegisterLocalBroadcast() {
        if(isAdded()) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateDataReceiver);
        }
    }

    public void publishProgress(final String pageURL, final int filePosition, final int progress) {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DownloadContentItem bean = new DownloadContentItem();
                    bean.pageURL = pageURL;
                    if (mDataList != null) {
                        int index = mDataList.indexOf(bean);
                        if (index > -1) {
                            DownloadContentItem downloadContentItem = mDataList.get(index);
                            RecyclerView.ViewHolder viewHolder = mListView.findViewHolderForAdapterPosition(index);
                            if (viewHolder != null && viewHolder instanceof ItemViewHolder) {
                                ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;
                                itemHolder.progressBar.setVisibility(View.VISIBLE);
                                int leftFileCount = downloadContentItem.fileCount - filePosition;

                                itemHolder.taskCountView.setText(String.format(mFormatLeftFileString, leftFileCount));
                                int count = downloadContentItem.fileCount * 100;
                                int position = filePosition;
                                int totalProgress = position * 100 + progress;
                                int newProgrees = totalProgress * 100 / count;
                                if (newProgrees >= itemHolder.progressBar.getProgress() && newProgrees <= 100) {
                                    itemHolder.progressBar.setProgress(newProgrees);
                                    //if (newProgrees >= 100) {
                                    //  itemHolder.progressBar.setVisibility(View.GONE);
                                    //}
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * receive new task
     */
    public void onReceiveNewTask(String pageURL) {
        if (isAdded()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            if (TextUtils.isEmpty(pageURL)) {
                return;
            }

            if (pageURL.equals(getString(R.string.toast_downlaoded_video))) {
                return;
            }
            DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
            LogUtil.e("downloading", "receiveNewTask=" + pageURL);
            if (videoBean != null) {
                mDataList.add(1, videoBean);
                mAdapter.notifyItemInserted(1);
            }
        }
    }

    public void onStartDownload(String pageURL) {
        LogUtil.e("main", "onStartDownload111=" + pageURL);
        if (isAdded()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            if (TextUtils.isEmpty(pageURL)) {
                IToast.makeText(getActivity(), R.string.spider_request_error, Toast.LENGTH_SHORT).show();
                return;
            }

            DownloadContentItem bean = new DownloadContentItem();
            bean.pageURL = pageURL;
            if (!mDataList.contains(bean)) {
                DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
                if (videoBean != null) {
                    mDataList.add(1, videoBean);
                    mAdapter.notifyItemInserted(1);
                }

                showNativeAd();
            }
        }
    }

    /**
     * deleteVideoByPageURL
     *
     * @param pageURL
     */
    public void deleteVideoByPath(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return;
        }
        DownloadContentItem bean = new DownloadContentItem();
        bean.pageURL = pageURL;
        int index = mDataList.indexOf(bean);
        if (index > -1) {
            DownloadContentItem downloadContentItem = mDataList.get(index);
            downloadContentItem.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED;
            RecyclerView.ViewHolder viewHolder = mListView.findViewHolderForAdapterPosition(index);
            if (viewHolder != null && viewHolder instanceof ItemViewHolder) {
                ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.operationBtn.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
    }


    public void showHotToInfo() {
        if (isShowHowToPage) {
            isShowHowToPage = false;
            mDataList.remove(mHowToBean);
            mAdapter.notifyDataSetChanged();
        } else {
            isShowHowToPage = true;
            if (mHowToBean == null) {
                mHowToBean = new DownloadContentItem();
                mHowToBean.itemType = DownloadContentItem.TYPE_HOWTO_ITEM;
            }
            mDataList.add(1, mHowToBean);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void hideHowToInfoCard() {
        if (isShowHowToPage) {
            showHotToInfo();
            PreferenceUtils.showedHowToInfo();
        }
    }


    private void showNativeAd() {
        if (!ADCache.SHOW_AD) {
            return;
        }
        if (isAdded()) {
            if (mFirstAdBean != null) {
                return;
            }
            if (mDataList != null && (mDataList.size() == 1 || mDataList.size() > 3)) {

                mDuNativeAd = new DuNativeAd(getActivity(), AD_PID, 2);
                mDuNativeAd.setMobulaAdListener(new DuAdListener() {
                    @Override
                    public void onError(DuNativeAd duNativeAd, com.duapps.ad.AdError adError) {
                        LogUtil.e("main", "DuAdLoaded.onError" + adError.getErrorMessage());
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
//                nativeAd = new NativeAd(getActivity(), "2099565523604162_2099565860270795");
//                nativeAd.setAdListener(new AdListener() {
//                    @Override
//                    public void onError(Ad ad, AdError adError) {
//                        LogUtil.v("facebook", "onError:" + adError);
//                    }
//
//                    @Override
//                    public void onAdLoaded(Ad ad) {
//                        onFacebookAdLoaded(ad);
//                    }
//
//                    @Override
//                    public void onAdClicked(Ad ad) {
//                        LogUtil.e("facebook", "onAdClicked");
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                               // ADCache.getDefault().removeClickedAd(mFirstAdBean);
//                                final int position = mDataList.indexOf(mFirstAdBean);
//                                if (position >= 0) {
//                                    mDataList.remove(position);
//                                    mAdapter.notifyItemRemoved(position);
//                                    mFirstAdBean = null;
//                                }
//                            }
//                        }, 1000);
//                    }
//
//                    @Override
//                    public void onLoggingImpression(Ad ad) {
//
//                    }
//                });
//
//                nativeAd.loadAd();
            }
        }
    }

    private void startLoadFacebookAd() {
        if (getActivity() != null && isAdded()) {
            nativeAd = new NativeAd(getActivity(), "2099565523604162_2099565860270795");
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
                    LogUtil.e("facebook", "onAdClicked");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ADCache.getDefault().removedAdByKey(ADCache.AD_KEY_DOWNLOADING_VIDEO);
                            final int position = mDataList.indexOf(mFirstAdBean);
                            if (position >= 0) {
                                mDataList.remove(position);
                                mAdapter.notifyItemRemoved(position);
                                mFirstAdBean = null;
                            }
                        }
                    }, 1000);
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });

            nativeAd.loadAd();
        }
    }

    // The next step is to extract the ad metadata and use its properties
// to build your customized native UI. Modify the onAdLoaded function
// above to retrieve the ad properties. For example:
    public void onFacebookAdLoaded(Ad ad) {
        if (getActivity() == null || isDetached()) {
            return;
        }
        if (ad != nativeAd) {
            return;
        }


        mFirstAdBean = new DownloadContentItem();
        mFirstAdBean.itemType = DownloadContentItem.TYPE_FACEBOOK_AD;
        mFirstAdBean.facebookNativeAd = nativeAd;
        mFirstAdBean.createdTime = System.currentTimeMillis();

        ADCache.getDefault().setFacebookNativeAd(ADCache.AD_KEY_DOWNLOADING_VIDEO, mFirstAdBean);

        int lastPosition = mLayoutManager.findLastVisibleItemPosition();
        LogUtil.e("facebook", "insert1FacebookAd");
        int insertADPosition = lastPosition + 1;
        if (insertADPosition <= mDataList.size() - 1) {
            mDataList.add(insertADPosition, mFirstAdBean);
            mAdapter.notifyItemInserted(insertADPosition);
        } else {
            mDataList.add(mFirstAdBean);
            mAdapter.notifyItemInserted(insertADPosition);
        }
    }

    private void onDuNativeAdLoaded(DuNativeAd duNativeAd) {
        if (getActivity() == null || isDetached()) {
            return;
        }

        mFirstAdBean = new DownloadContentItem();
        mFirstAdBean.itemType = DownloadContentItem.TYPE_FACEBOOK_AD;
        mFirstAdBean.duNativeAd = duNativeAd;
        mFirstAdBean.createdTime = System.currentTimeMillis();

        ADCache.getDefault().setFacebookNativeAd(ADCache.AD_KEY_DOWNLOADING_VIDEO, mFirstAdBean);

        int lastPosition = mLayoutManager.findLastVisibleItemPosition();
        LogUtil.e("facebook", "insert1FacebookAd");

        if (mDataList != null && mDataList.size() <= 1) {
            mDataList.add(mFirstAdBean);
            mAdapter.notifyDataSetChanged();
        } else {
            int insertADPosition = lastPosition + 1;
            if (insertADPosition <= mDataList.size() - 1) {
                mDataList.add(insertADPosition, mFirstAdBean);
                mAdapter.notifyItemInserted(insertADPosition);
            }
        }
    }

    @Override
    public void showHowTo() {
        showHotToInfo();
    }

    @Override
    public void onDownloadFromClipboard() {
        final ClipboardManager cb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        String pastUrl = cb.getText().toString();
        if (!TextUtils.isEmpty(pastUrl)) {
            startDownload(pastUrl);
        }
    }


    @Override
    public void onDestroy() {
        unRegisterLocalBroadcast();
        super.onDestroy();
    }
}
