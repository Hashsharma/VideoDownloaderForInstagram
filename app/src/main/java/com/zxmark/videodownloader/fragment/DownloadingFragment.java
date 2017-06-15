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
import android.widget.TextView;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.zxmark.videodownloader.R;
import com.zxmark.videodownloader.adapter.ItemViewHolder;
import com.zxmark.videodownloader.adapter.MainDownloadingRecyclerAdapter;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.Globals;

import java.util.List;

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
        mUrlEditText = (EditText) findViewById(R.id.paste_url);
        TextView homeTv = (TextView) findViewById(R.id.home_directory);
        homeTv.setText(DownloadUtil.getHomeDirectory().getAbsolutePath());
        findViewById(R.id.btn_howto).setOnClickListener(this);
        findViewById(R.id.btn_paste).setOnClickListener(this);
        mListView = (RecyclerView) findViewById(R.id.downloading_list);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        mListView.setLayoutManager(mLayoutManager);

        mHowToView = findViewById(R.id.how_to_info);
        mDataList = DBHelper.getDefault().getDownloadingList();
        mAdapter = new MainDownloadingRecyclerAdapter(mDataList, true);
        mListView.setAdapter(mAdapter);
        if (!TextUtils.isEmpty(mReceiveUrlParams)) {
            receiveSendAction(mReceiveUrlParams);
        }

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(getActivity());
// repeat many times:
        ImageView itemIcon = new ImageView(getActivity());
        itemIcon.setImageResource(R.mipmap.float_download);
        SubActionButton button1 = itemBuilder.setContentView(itemIcon).build();

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
            case R.id.btn_paste:
                final ClipboardManager cb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                String pastUrl = cb.getText().toString();
                if (!TextUtils.isEmpty(pastUrl)) {
                    startDownload(pastUrl);
                }
                break;
            case R.id.btn_howto:
                if (mHowToView.getVisibility() == View.VISIBLE) {
                    mHowToView.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                } else {
                    mHowToView.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                }
                break;
        }
    }
}
