package com.zxmark.videodownloader.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.zxmark.videodownloader.DownloaderBean;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.adapter.MainListRecyclerAdapter;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.FileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fanlitao on 17/6/13.
 */

public class VideoHistoryFragment extends Fragment {


    private EditText mUrlEditText;
    private Button mDownloadBtn;
    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private List<DownloaderBean> mDataList;
    private MainListRecyclerAdapter mAdapter;

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
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);

        mListView.setLayoutManager(mLayoutManager);
        initData();
    }

    private void initData() {
        File file = DownloadUtil.getHomeDirectory();
        File[] fileArray = file.listFiles();
        DBHelper dbHelper = DBHelper.getDefault();
        if (fileArray != null && fileArray.length > 0) {
            mDataList = new ArrayList<DownloaderBean>();
            for (File item : fileArray) {
                if (dbHelper.isDownloadingByPath(item.getAbsolutePath())) {
                    continue;
                }
                DownloaderBean bean = new DownloaderBean();

                bean.file = item;
                bean.progress = 0;
                mDataList.add(bean);
            }
            Collections.sort(mDataList, new FileComparator());
            mAdapter = new MainListRecyclerAdapter(mDataList, false);
            mListView.setAdapter(mAdapter);
        }
    }

    public void refreshUI() {
        initData();
    }
}
