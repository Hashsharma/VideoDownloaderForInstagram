package com.zxmark.videodownloader.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.adapter.ItemViewHolder;
import com.zxmark.videodownloader.adapter.MainDownloadingRecyclerAdapter;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;

import java.util.List;

// In the Activity that will launch the native ad,
// implement the AdListener interface and add the following:

/**
 * Created by fanlitao on 17/6/13.
 */

public class DownloadingFragment extends Fragment implements View.OnClickListener {


    private EditText mUrlEditText;
    private Button mDownloadBtn;
    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private MainDownloadingRecyclerAdapter mAdapter;
    private List<VideoBean> mDataList;

    private View mHowToView;

    public String mReceiveUrlParams;

    private NativeAd nativeAd;

    private View mFacebookAdViewContainer;
    private RequestManager mGlide;



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

        mGlide = Glide.with(getActivity());

        mFacebookAdViewContainer = findViewById(R.id.main_ad_container);
//        TextView homeTv = (TextView) findViewById(R.id.home_directory);
//        homeTv.setText(getResources().getString(R.string.download_home_lable, DownloadUtil.getHomeDirectory().getAbsolutePath()));
//        findViewById(R.id.btn_howto).setOnClickListener(this);
//        findViewById(R.id.btn_paste).setOnClickListener(this);
        mListView = (RecyclerView) findViewById(R.id.downloading_list);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        mListView.setLayoutManager(mLayoutManager);

        mHowToView = findViewById(R.id.how_to_info);
        mDataList = DBHelper.getDefault().getDownloadingList();
        VideoBean headerBean = new VideoBean();
        headerBean.type = MainDownloadingRecyclerAdapter.VIEW_TYPE_HEAD;
        mDataList.add(0,headerBean);
        mAdapter = new MainDownloadingRecyclerAdapter(mDataList, true);
        mListView.setAdapter(mAdapter);
        if (!TextUtils.isEmpty(mReceiveUrlParams)) {
            receiveSendAction(mReceiveUrlParams);
        }

        showNativeAd();
    }

    public void receiveSendAction(String url) {
        mUrlEditText.setText(url);
        startDownload(url);
    }

    private void startDownload(final String url) {
        if (isAdded()) {
            Intent intent = new Intent(getActivity(), DownloadService.class);
            intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
            intent.putExtra(Globals.EXTRAS, url);
            getActivity().startService(intent);
        }
    }

    public void publishProgress(final String path, final int progress) {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VideoBean bean = new VideoBean();
                    bean.videoPath = path;
                    if (mDataList != null) {
                        int index = mDataList.indexOf(bean);
                        if (index > -1) {
                            RecyclerView.ViewHolder viewHolder = mListView.findViewHolderForAdapterPosition(index);
                            if (viewHolder != null && viewHolder instanceof ItemViewHolder) {
                                ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;
                                itemHolder.progressBar.setVisibility(View.VISIBLE);
                                itemHolder.progressBar.setProgress(progress);
                                if (progress >= 99) {
                                    itemHolder.progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    public void onStartDownload(String path) {
        if (mDataList == null || mDataList.size() == 0) {
            mHowToView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mDataList = DBHelper.getDefault().getDownloadingList();
            mAdapter = new MainDownloadingRecyclerAdapter(mDataList, true);
            mListView.setAdapter(mAdapter);
        } else {
            VideoBean bean = new VideoBean();
            bean.videoPath = path;
            if (!mDataList.contains(bean)) {
                mDataList = DBHelper.getDefault().getDownloadingList();
                mAdapter = new MainDownloadingRecyclerAdapter(mDataList, true);
                mListView.setAdapter(mAdapter);
            }
        }
    }

    public void deleteVideoByPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        VideoBean bean = new VideoBean();
        bean.videoPath = path;
        mDataList.remove(bean);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btn_paste:
//                final ClipboardManager cb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                String pastUrl = cb.getText().toString();
//                if (!TextUtils.isEmpty(pastUrl)) {
//                    startDownload(pastUrl);
//                }
//                break;
            case R.id.btn_howto:
                showHotToInfo();
                break;
        }
    }

    public void showHotToInfo() {
        if (mHowToView.getVisibility() == View.VISIBLE) {
            mHowToView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            if(mAdapter == null || mAdapter.getItemCount() == 0) {
                mFacebookAdViewContainer.setVisibility(View.VISIBLE);
            } else {
                mFacebookAdViewContainer.setVisibility(View.GONE);
            }
        } else {
            mHowToView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            mFacebookAdViewContainer.setVisibility(View.GONE);

        }
    }


    private void showNativeAd() {
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
        if (getActivity() == null || isDetached()) {
            return;
        }
        if (ad != nativeAd) {
            return;
        }


        VideoBean bean = new VideoBean();
        bean.type = MainDownloadingRecyclerAdapter.VIEW_TYPE_AD;
        bean.facebookNativeAd = nativeAd;

        mDataList.add(bean);
        mAdapter.notifyDataSetChanged();




//        mFacebookAdViewContainer.setVisibility(View.VISIBLE);
//
//        String titleForAd = nativeAd.getAdTitle();
//        NativeAd.Image coverImage = nativeAd.getAdCoverImage();
//        NativeAd.Image iconForAd = nativeAd.getAdIcon();
//        String socialContextForAd = nativeAd.getAdSocialContext();
//        String titleForAdButton = nativeAd.getAdCallToAction();
//        String textForAdBody = nativeAd.getAdBody();
//        NativeAd.Rating appRatingForAd = nativeAd.getAdStarRating();
//
//        // Add code here to create a custom view that uses the ad properties
//        // For example:
//        LinearLayout nativeAdContainer = new LinearLayout(getActivity());
//        TextView titleLabel = new TextView(getActivity());
//        titleLabel.setText(titleForAd);
//        nativeAdContainer.addView(titleLabel);
//
//        // Add the ad to your layout
//        LinearLayout mainContainer = (LinearLayout) findViewById(R.id.MainContainer);
////        mainContainer.addView(nativeAdContainer);
//
//        LinearLayout adChoicesContainer = (LinearLayout) findViewById(R.id.ad_choices_container);
//        AdChoicesView adChoicesView = new AdChoicesView(getActivity(), nativeAd, true);
//        adChoicesContainer.addView(adChoicesView);
//
//        ImageView adCover = (ImageView) mainContainer.findViewById(R.id.ad_cover);
//        mGlide.load(coverImage.getUrl()).into(adCover);
//
//        ImageView adIcon = (ImageView) mainContainer.findViewById(R.id.ad_icon);
//        mGlide.load(iconForAd.getUrl()).into(adIcon);
//
//        TextView adBodyTv = (TextView) mainContainer.findViewById(R.id.ad_body);
//        adBodyTv.setText(textForAdBody);
//
//        TextView adTitleTv = (TextView) mainContainer.findViewById(R.id.ad_title);
//        adTitleTv.setText(titleForAd);
//
//        Button adButton = (Button) mainContainer.findViewById(R.id.facebook_ad_btn);
//        adButton.setText(titleForAdButton);
//        // Register the native ad view with the native ad instance
//        nativeAd.registerViewForInteraction(mainContainer);
    }
}
