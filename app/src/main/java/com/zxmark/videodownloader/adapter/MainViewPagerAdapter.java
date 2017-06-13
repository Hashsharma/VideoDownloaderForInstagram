package com.zxmark.videodownloader.adapter;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.bumptech.glide.load.engine.Resource;
import com.zxmark.videodownloader.MainActivity;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.R;
import com.zxmark.videodownloader.fragment.DownloadingFragment;
import com.zxmark.videodownloader.fragment.VideoHistoryFragment;
import com.zxmark.videodownloader.util.LogUtil;

import okhttp3.ResponseBody;

/**
 * Created by fanlitao on 17/6/13.
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {


    public static final CharSequence[] TITLE_ARRAY = new CharSequence[]{"", ""};

    private Resources mRes;

    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
        mRes = MainApplication.getInstance().getApplicationContext().getResources();
    }

    @Override
    public int getCount() {
        return 2;
    }


    @Override
    public Fragment getItem(int position) {
        LogUtil.v("adapter", "getItem.position:" + position);
        switch (position) {
            case 0:
                return DownloadingFragment.newInstance();
            case 1:
                return VideoHistoryFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mRes.getString(R.string.tab_title_downloading);
            case 1:
                return mRes.getString(R.string.tab_title_history);
            default:

                return null;
        }
    }
}
