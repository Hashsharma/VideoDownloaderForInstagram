package com.zxmark.videodownloader.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanlitao on 6/23/17.
 */

public class DownloaderDBHelper {


    public static final DownloaderDBHelper SINGLETON = new DownloaderDBHelper();

    private Context mContext;
    private ContentResolver mContentResolver;

    private DownloaderDBHelper() {
        mContext = MainApplication.getInstance().getApplicationContext();
        mContentResolver = mContext.getContentResolver();
    }


    public void saveNewDownloadTask(DownloadContentItem item) {
        if (item != null && !TextUtils.isEmpty(item.pageURL)) {
            if(getPageIdByPageURL(item.pageURL) > -1) {
                return;
            }
            Uri id = mContentResolver.insert(DownloadContentItem.CONTENT_URI, DownloadContentItem.from(item));
            LogUtil.e("db", "saveNewDownloadTask:" + id + ":" + item.pageURL);
        }
    }

    public List<DownloadContentItem> getDownloadingTask() {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_STATUS + " = ?", new String[]{String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOADING)}, null);
        List<DownloadContentItem> itemList = new ArrayList<>();
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    itemList.add(item);
                }
                return itemList;
            }

            return itemList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<DownloadContentItem> getDownloadedTask() {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_STATUS + " = ?", new String[]{String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED)}, null);
        try {
            if (cursor != null) {
                List<DownloadContentItem> itemList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    itemList.add(item);
                }
                return itemList;
            }

            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public DownloadContentItem getDownloadItemByPageURL(String pageURL) {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? ", new String[]{pageURL}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    return item;
                }
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public DownloadContentItem getDownloadItemByPageHome(String pageHome) {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_HOME + " = ? ", new String[]{pageHome}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    return item;
                }
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getPageIdByPageURL(String pageURL) {
        LogUtil.v("db", "getPageIdByPageURL=" + pageURL);
        if (TextUtils.isEmpty(pageURL)) {
            return -1;
        }
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? ", new String[]{pageURL}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem._ID));
                }
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getDownloadedPageIdByURL(String pageURL) {
        LogUtil.v("db", "getDownloadedPageIdByURL=" + pageURL);
        if (TextUtils.isEmpty(pageURL)) {
            return -1;
        }
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? and " + DownloadContentItem.PAGE_STATUS + " = ?", new String[]{pageURL,String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED)}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem._ID));
                }
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public void finishDownloadTask(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return;
        }
        int pageId = getPageIdByPageURL(pageURL);
        LogUtil.e("db", "finishDownloadTask:" + pageURL + ":" + pageId);
        if (pageId > -1) {
            updateDownloadTaskStatus(pageId, DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED);
        }
    }

    public int updateDownloadTaskStatus(int pageId, int status) {
        ContentValues values = new ContentValues();
        values.put(DownloadContentItem.PAGE_STATUS, status);
        Uri uri = ContentUris.withAppendedId(DownloadContentItem.CONTENT_URI, pageId);
        int count = mContentResolver.update(uri, values, null, null);
        return count;
    }

    public int deleteDownloadTask(String pageURL) {
        DownloadContentItem item = getDownloadItemByPageURL(pageURL);
        if (item != null) {
            String dir = item.pageHOME;
            File dirFile = new File(dir);
            if (dirFile.isDirectory()) {
                for (File meidaFile : dirFile.listFiles()) {
                    meidaFile.delete();
                }
                dirFile.delete();
            } else {
                dirFile.delete();
            }
        }

        return mContentResolver.delete(DownloadContentItem.CONTENT_URI, DownloadContentItem.PAGE_URL + " = ? ", new String[]{pageURL});
    }

    public boolean isExistDownloadedPageURL(String pageURL) {
        return getDownloadedPageIdByURL(pageURL) > -1;
    }

    public void deleteDownloadTaskAsync(final String pageURL) {
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                deleteDownloadTask(pageURL);
            }
        });
    }
}
