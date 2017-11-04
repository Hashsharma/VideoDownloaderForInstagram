package com.zxmark.videodownloader.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.adapter.ItemViewHolder;
import com.zxmark.videodownloader.adapter.MainListRecyclerAdapter;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.util.ADCache;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fanlitao on 17/6/13.
 */

public class VideoHistoryFragment extends Fragment {


    public static final int PID = 138165;
    private EditText mUrlEditText;
    private Button mDownloadBtn;
    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private List<DownloadContentItem> mDataList;
    private MainListRecyclerAdapter mAdapter;
    private NativeAd mNativeAd;

    private MainListRecyclerAdapter.ISelectChangedListener mListener;
    private boolean mInsertFacebookAdStatus = false;
    private int mLastAdInsertedPosition = 0;
    //多申请几个Facebook广告位
    private static String[] FACEBOOK_IDS = new String[]{"2099565523604162_2099583463602368", "2099565523604162_2170925976468116"};
    private HashMap<String, NativeAd> mNativeAdMap = new HashMap<String, NativeAd>();
    private HashMap<Integer, DownloadContentItem> mBeanMap = new HashMap<>();
    private Handler mMainLooperHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                DownloadContentItem downloadContentItem = (DownloadContentItem) msg.obj;
                if (mListener != null) {
                    mListener.onDeleteDownloadItem(downloadContentItem);
                }
                final int index = mDataList.indexOf(downloadContentItem);
                if (index > -1) {
                    mAdapter.notifyItemRemoved(index);
                    mDataList.remove(index);
                }
            } else if (msg.what == 1) {
                mAdapter.clearSelectedList();
                if (mDataList.size() == 0) {
                    mAdapter.quitSelectMode();
                    if (mListener != null) {
                        mListener.onQuitSelectMode();
                    }
                }
            }
        }
    };

    private BroadcastReceiver mUpdateDataReceiver;

    public static VideoHistoryFragment newInstance() {
        VideoHistoryFragment fragment = new VideoHistoryFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_page, container, false);
        return view;
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (RecyclerView) findViewById(R.id.video_history_list);
        mListView.setHasFixedSize(true);
        mListView.setItemAnimator(new DefaultItemAnimator());
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        mListView.setLayoutManager(mLayoutManager);
        mListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (!mInsertFacebookAdStatus) {
                            return;
                        }
                        int visibleItemCount = mLayoutManager.getChildCount();
                        int totalItemCount = mLayoutManager.getItemCount();
                        int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
                        Log.v("fan", visibleItemCount + ":" + totalItemCount + ":" + firstVisibleItemPosition + ":" + lastVisibleItemPosition);
                        if (firstVisibleItemPosition < 2) {
                            return;
                        }
                        View view = null;
                        boolean haveFacebookAdInScreen = false;
                        for (int index = 0; index < visibleItemCount; index++) {
                            view = mListView.getChildAt(index);
                            if (view != null) {
                                DownloadContentItem downloadContentItem = (DownloadContentItem) view.getTag();
                                if (downloadContentItem != null) {
                                    if (downloadContentItem.itemType == DownloadContentItem.TYPE_FACEBOOK_AD) {
                                        haveFacebookAdInScreen = true;
                                    }
                                }
                            }
                        }

                        if (!haveFacebookAdInScreen) {
                            DownloadContentItem downloadContentItem = mBeanMap.get(mLastAdInsertedPosition % 2);
                            if (downloadContentItem != null) {
                                int adPosition = lastVisibleItemPosition - 1;
                                mDataList.add(adPosition, downloadContentItem);
                                mAdapter.notifyItemInserted(adPosition);
                                mLastAdInsertedPosition += 1;
                            }
                        }

                        break;

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {

                    mInsertFacebookAdStatus = true;
                } else {
                    mInsertFacebookAdStatus = false;
                }
            }
        });
        initData();
    }

    private void initData() {
        registerLocalBroadcast();
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                mDataList = DownloaderDBHelper.SINGLETON.getDownloadedTask();

//                mAdVideoBean = ADCache.getDefault().getFacebookNativeAd(ADCache.AD_KEY_HISTORY_VIDEO);
//                if (mAdVideoBean != null) {
//                    if (mDataList.size() > 2) {
//                        mDataList.add(2, mAdVideoBean);
//                    } else {
//                        mDataList.add(mAdVideoBean);
//                    }
//                }

                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter = new MainListRecyclerAdapter(mDataList, false);
                            mAdapter.setLayoutManager(mLayoutManager);
                            mListView.setAdapter(mAdapter);
                            showNativeAd();
                        }
                    });
                }
            }
        });


    }

    public void onAddNewDownloadedFile(String pageURL) {
        LogUtil.v("main", "onAddNewDownloadFile:" + pageURL);
        if (mDataList != null) {
            DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
            if (videoBean != null) {
                final int index = mDataList.indexOf(videoBean);
                if (index < 0) {
                    mDataList.add(0, videoBean);
                    mAdapter.notifyItemInserted(0);
                    mListView.smoothScrollToPosition(0);
                } else {
                    RecyclerView.ViewHolder viewHolder = mListView.findViewHolderForAdapterPosition(index);
                    if (viewHolder != null && viewHolder instanceof ItemViewHolder) {
                        ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;
                        itemHolder.circleProgress.setVisibility(View.GONE);
                        DownloadContentItem downloadContentItem = mDataList.get(index);
                        downloadContentItem.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED;
                    }
                }
            }
        }
    }


    private void registerLocalBroadcast() {
        if (isAdded()) {
            mUpdateDataReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String pageURL = intent.getStringExtra(Globals.KEY_BEAN_PAGE_URL);
                    mMainLooperHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.e("history", "locale:" + pageURL);
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
        if (isAdded()) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateDataReceiver);
        }
    }


    private void showNativeAd() {
        if (!ADCache.SHOW_AD) {
            return;
        }
        //LogUtil.e("history", "showNativeAd:" + mAdVideoBean);
        // if (mAdVideoBean == null) {
        if (isAdded()) {
            startLoadFacebookAd();
        }
        //}
    }


    private void startLoadFacebookAd() {
        if (getActivity() != null && isAdded()) {
            boolean onlyOneAd = mDataList.size() <= 1;
            for (int index = 0; index < FACEBOOK_IDS.length; index++) {
                if (onlyOneAd && (index == 1)) {
                    break;
                }
                final int position = index;
                NativeAd nativeAd = new NativeAd(getActivity(), FACEBOOK_IDS[index]);
                nativeAd.setAdListener(new AdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        LogUtil.v("facebook", "onError:" + adError.getErrorMessage());
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        onFacebookAdLoaded(position, ad);
                    }

                    @Override
                    public void onAdClicked(Ad ad) {
                        LogUtil.e("facebook", "onAdClicked");
//                        mMainLooperHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                if (mAdVideoBean != null) {
//                                    ADCache.getDefault().removedAdByKey(ADCache.AD_KEY_HISTORY_VIDEO);
//                                    final int position = mDataList.indexOf(mAdVideoBean);
//                                    LogUtil.e("facebook2", "position:" + position);
//                                    if (position >= 0) {
//                                        mDataList.remove(position);
//                                        mAdapter.notifyItemRemoved(position);
//                                        mAdVideoBean = null;
//                                    }
//                                }
//                            }
//                        }, 1000);
                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {

                    }
                });

                nativeAd.loadAd();
                mNativeAdMap.put(FACEBOOK_IDS[index], nativeAd);
            }
        }
    }


    // The next step is to extract the ad metadata and use its properties
// to build your customized native UI. Modify the onAdLoaded function
// above to retrieve the ad properties. For example:
    public void onFacebookAdLoaded(int position, Ad ad) {
        if (getActivity() == null || isDetached()) {
            return;
        }

        NativeAd nativeAd = mNativeAdMap.get(FACEBOOK_IDS[position]);
        if (ad != nativeAd) {
            return;
        }

        DownloadContentItem mAdVideoBean = new DownloadContentItem();
        mAdVideoBean.itemType = DownloadContentItem.TYPE_FACEBOOK_AD;
        mAdVideoBean.facebookNativeAd = nativeAd;
        mAdVideoBean.createdTime = System.currentTimeMillis();
        mBeanMap.put(position, mAdVideoBean);
        ADCache.getDefault().setFacebookNativeAd(ADCache.AD_KEY_HISTORY_VIDEO, mAdVideoBean);

        if (mDataList != null && mDataList.size() > 1) {
            int adPosition = position * 2 + 1;
            if (adPosition < mDataList.size()) {
                mDataList.add(adPosition, mAdVideoBean);
                mAdapter.notifyItemInserted(adPosition);
            }
        } else {
            mDataList.add(mAdVideoBean);
            mAdapter.notifyDataSetChanged();
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
                                itemHolder.circleProgress.setVisibility(View.VISIBLE);
                                int count = downloadContentItem.fileCount * 100;
                                int position = filePosition;
                                int totalProgress = position * 100 + progress;
                                int newProgrees = totalProgress * 100 / count;
                                if (newProgrees >= itemHolder.circleProgress.getProgress() && newProgrees <= 100) {
                                    itemHolder.circleProgress.setProgress(newProgrees);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        unRegisterLocalBroadcast();
        super.onDestroy();
    }


    public void setISelectChangedListener(MainListRecyclerAdapter.ISelectChangedListener listener) {
        mListener = listener;
        if (mAdapter != null) {
            mAdapter.setISelectChangedListener(listener);
        }
    }

    public void selectAll() {
        if (mAdapter != null) {
            mAdapter.selectAll();
        }
    }

    public void quitSelectMode() {
        if (mAdapter != null) {
            mAdapter.quitSelectMode();
        }
    }

    public boolean isSelectMode() {
        if (mAdapter != null) {
            return mAdapter.isSelectMode();
        }

        return false;
    }

    public void deleteSelectItems() {
        final HashSet<DownloadContentItem> selectDataList = mAdapter.getSelectList();
        final DownloaderDBHelper dbHelper = DownloaderDBHelper.SINGLETON;

        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                Iterator<DownloadContentItem> itemIterator = selectDataList.iterator();
                while (itemIterator.hasNext()) {
                    DownloadContentItem downloadContentItem = itemIterator.next();
                    dbHelper.deleteDownloadContentItem(downloadContentItem);
                    mMainLooperHandler.obtainMessage(0, downloadContentItem).sendToTarget();
                }
                mMainLooperHandler.sendEmptyMessage(1);
            }
        });

    }
}
