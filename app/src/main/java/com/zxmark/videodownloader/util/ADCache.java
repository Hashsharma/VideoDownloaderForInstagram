package com.zxmark.videodownloader.util;

import com.facebook.ads.NativeAd;
import com.zxmark.videodownloader.db.DownloadContentItem;

import java.util.HashMap;

/**
 * Created by fanlitao on 6/25/17.
 */

public class ADCache {


    public static final boolean SHOW_AD = true;
    public static final String AD_KEY_DOWNLOADING_VIDEO = "AD_KEY_DOWNLOADING_VIDEO";
    public static final String AD_KEY_HISTORY_VIDEO = "AD_KEY_HISTORY_VIDEO";
    public static final String AD_KEY_GALLERY =  "AD_KEY_GALLERY";


    public static final long MAX_EXPIRE_TIME = 30 * 60 * 1000L;

    public HashMap<String, DownloadContentItem> mAdCacheMap;

    private static final ADCache sInstance = new ADCache();


    private ADCache() {

    }

    public static ADCache getDefault() {
        return sInstance;
    }


    public void setFacebookNativeAd(String key, DownloadContentItem itemAd) {
        if (mAdCacheMap == null) {
            mAdCacheMap = new HashMap<>();
        }

        mAdCacheMap.put(key, itemAd);
    }

    public DownloadContentItem getFacebookNativeAd(String key) {
        if (mAdCacheMap != null) {
            DownloadContentItem item = mAdCacheMap.get(key);
            if (item != null) {
                if (System.currentTimeMillis() - item.createdTime > MAX_EXPIRE_TIME) {
                    mAdCacheMap.remove(key);
                    return null;
                } else {
                    return item;
                }
            }
        }
        return null;
    }

    public void removedAdByKey(String key) {
        if (mAdCacheMap != null) {
            mAdCacheMap.remove(key);
        }
    }

    public void removeClickedAd(DownloadContentItem item) {

    }

}
