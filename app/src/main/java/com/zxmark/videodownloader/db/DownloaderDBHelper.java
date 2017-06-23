package com.zxmark.videodownloader.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zxmark.videodownloader.MainApplication;

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
        mContentResolver.insert(DownloadContentItem.CONTENT_URI, DownloadContentItem.from(item));
    }

    public List<DownloadContentItem> getDownloadingTask() {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_STATUS + " = ?", new String[]{String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOADING)}, null);
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


    public int updateDownloadTaskStatus(DownloadContentItem item, int status) {
        ContentValues values = new ContentValues();
        values.put(DownloadContentItem.PAGE_STATUS, status);
        Uri uri = ContentUris.withAppendedId(DownloadContentItem.CONTENT_URI, item.pageId);
        int count = mContentResolver.update(uri, values, null, null);
        return count;

    }
}
