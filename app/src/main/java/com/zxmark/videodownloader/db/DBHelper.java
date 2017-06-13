package com.zxmark.videodownloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by fanlitao on 17/6/13.
 */

public class DBHelper {


    private static final String DATABASE_NAME = "downloader.db";// 数据库名
    SQLiteDatabase db;
    Context context;//应用环境上下文   Activity 是其子类

    public DBHelper(Context _context) {
        context = _context;
        //开启数据库
        db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        createTable();
    }

    public void createTable() {
        try {
            db.execSQL("CREATE TABLE downloading_table (" +
                    "_ID INTEGER PRIMARY KEY autoincrement,"
                    + "video_title TEXT, video_url varchar(512),page_url varchar(512),video_status int default 0"
                    + ");");

//            db.execSQL("CREATE TABLE gif_like (" +
//                    "_ID INTEGER PRIMARY KEY autoincrement,"
//                    + "gif_title TEXT, gif_url varchar(255),like_info int"
//                    + ");");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
