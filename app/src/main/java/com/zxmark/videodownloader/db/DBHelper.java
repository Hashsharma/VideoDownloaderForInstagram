package com.zxmark.videodownloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanlitao on 17/6/13.
 */

public class DBHelper {


    private static final String DATABASE_NAME = "downloader.db";// 数据库名
    SQLiteDatabase db;
    Context context;//应用环境上下文   Activity 是其子类

    public static final int STATE_VIDEO_DOWNLOADING = 0;
    public static final int STATE_VIDEO_DOWNLOAD_SUCESSFUL = 1;
    public static final int STATE_VIDEO_DOWNLOAD_FAILED = 2;

    public static final String TABLE_NAME = "downloading_table";

    private static volatile DBHelper sDefault;

    public static final String INSERT_SQL_FORMAT = "insert into downloading_table values(null,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d)";

    public static DBHelper getDefault() {
        if (sDefault == null) {
            sDefault = new DBHelper(MainApplication.getInstance().getApplicationContext());
        }

        return sDefault;
    }

    public DBHelper(Context _context) {
        context = _context;
        //开启数据库
        db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        createTable();
    }

    public void createTable() {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS downloading_table (" +
                    "_ID INTEGER PRIMARY KEY autoincrement,"
                    + "video_title TEXT, page_url varchar(512),thumbnail_url varchar(512),video_url varchar(512),app_page_url varchar(512),video_path varchar(512),video_status int default 0"
                    + ");");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入一条新任务
     *
     * @param title
     */
    public void insertNewTask(String title, String pageUrl, String thumbnailUrl, String videoUrl, String appPageUrl, String videoPath) {
        String sql = "";
        try {
            sql = String.format(INSERT_SQL_FORMAT, title, pageUrl, thumbnailUrl, videoUrl, appPageUrl, videoPath, 0);
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<VideoBean> getDownloadingList() {
        Cursor cursor = db.query("downloading_table", null, "video_status=?", new String[]{String.valueOf(0)}, null, null, "_ID desc");
        List<VideoBean> dataList = new ArrayList<VideoBean>();
        try {
            while (cursor.moveToNext()) {
                VideoBean bean = new VideoBean();
                bean.pageTitle = cursor.getString(1);
                bean.sharedUrl = cursor.getString(2);
                bean.thumbnailUrl = cursor.getString(3);
                bean.downloadVideoUrl = cursor.getString(4);
                bean.appPageUrl = cursor.getString(5);
                bean.videoPath = cursor.getString(6);
                LogUtil.v("sd", "bean.videoPath=" + bean.videoPath);
                dataList.add(bean);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dataList;
    }

    public VideoBean getVideoBeanByPageURL(String sharedUrl) {
        Cursor cursor = db.query("downloading_table", null, "page_url = ?", new String[]{sharedUrl}, null, null, "_ID desc");
        try {
            if (cursor.moveToNext()) {
                VideoBean bean = new VideoBean();
                bean.pageTitle = cursor.getString(1);
                bean.sharedUrl = cursor.getString(2);
                bean.thumbnailUrl = cursor.getString(3);
                bean.downloadVideoUrl = cursor.getString(4);
                bean.appPageUrl = cursor.getString(5);
                bean.videoPath = cursor.getString(6);
                return bean;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public VideoBean getVideoBeanByVideoPath(String videoPath) {
        Cursor cursor = db.query("downloading_table", null, "video_path = ?", new String[]{videoPath}, null, null, "_ID desc");
        try {
            if (cursor.moveToNext()) {
                VideoBean bean = new VideoBean();
                bean.pageTitle = cursor.getString(1);
                bean.sharedUrl = cursor.getString(2);
                bean.thumbnailUrl = cursor.getString(3);
                bean.downloadVideoUrl = cursor.getString(4);
                bean.appPageUrl = cursor.getString(5);
                bean.videoPath = cursor.getString(6);
                return bean;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public boolean isDownloadedPage(String pageURL) {
        Cursor cursor = db.query("downloading_table", null, "page_url = ?", new String[]{pageURL}, null, null, "_ID desc");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }


    public void deleteDownloadingVideo(String videoPath) {
        db.delete(TABLE_NAME, "video_path = ? ", new String[]{videoPath});
    }

    /**
     * 下载完成，修改标志位
     *
     * @param path
     */
    public void finishDownload(String path) {
        ContentValues cv = new ContentValues();
        cv.put("video_status", STATE_VIDEO_DOWNLOAD_SUCESSFUL);
        db.update(TABLE_NAME, cv, "video_path = ?", new String[]{path});
    }

    /**
     * 通过路径获取当前视频的相关信息
     *
     * @param path
     * @return
     */
    public VideoBean getVideoInfoByPath(String path) {
        Cursor cursor = db.query("downloading_table", null, "video_status=? and video_path = ?", new String[]{String.valueOf(STATE_VIDEO_DOWNLOAD_SUCESSFUL), path}, null, null, "_ID desc");
        try {
            if (cursor.moveToNext()) {
                VideoBean bean = new VideoBean();
                bean.pageTitle = cursor.getString(1);
                bean.sharedUrl = cursor.getString(2);
                bean.thumbnailUrl = cursor.getString(3);
                bean.downloadVideoUrl = cursor.getString(4);
                bean.appPageUrl = cursor.getString(5);
                bean.videoPath = cursor.getString(6);
                return bean;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 通过路径获取当前视频的相关信息
     *
     * @param
     * @return
     */
    public List<String> getDownloadedVideoList() {
        Cursor cursor = db.query("downloading_table", null, "video_status=?", new String[]{String.valueOf(STATE_VIDEO_DOWNLOAD_SUCESSFUL)}, null, null, "_ID desc");
        try {
            List<String> dataList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String  videoPath = cursor.getString(6);
                dataList.add(videoPath);
            }
            return dataList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean isDownloadingByPath(String path) {
        LogUtil.v("sd", "bean.getVideoInfoByPath=" + path);
        Cursor cursor = db.query("downloading_table", null, "video_path = ?", new String[]{path}, null, null, "_ID desc");
        try {
            if (cursor.moveToNext()) {
                VideoBean bean = new VideoBean();
                return cursor.getInt(7) == STATE_VIDEO_DOWNLOADING;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

}
