package com.zxmark.videodownloader.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by fanlitao on 6/22/17.
 */

public class DownloadContentItem implements BaseColumns {

    public static String TABLE_NAME = "download_content";
    public static String AUTHORITY = "com.imobapp.videodownloaderforinstagram";

    public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.imob.videodownloader.content";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.imob.videodownloader.content";


    public static final String PAGE_URL = "page_url";
    public static final String PAGE_HOME = "page_home";
    public static final String PAGE_THUMBNAIL = "page_thumbnail";
    public static final String PAGE_TITLE = "page_title";
    public static final String PAGE_DESCRIPTION = "page_desc";
    public static final String HASH_TAGS = "hash_tags";
    public static final String PAGE_DOWNLOAD_FILE_COUNT = "count";
    public static final String PAGE_STATUS = "page_status";

    public static final String DEFAULT_ORDERBY = _ID + "  DESC";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + _ID + " INTEGER PRIMARY KEY,"
            + PAGE_URL + " VARCHAR(255)," + PAGE_HOME + " VARCHAR(255)," + PAGE_THUMBNAIL + " VARCHAR(255)," + PAGE_TITLE + " VARCHAR(255) ," + PAGE_DESCRIPTION + " VARCHAR(255) ," + HASH_TAGS + " VARCHAR(255)," + PAGE_DOWNLOAD_FILE_COUNT + " VARCHAR(255)," + PAGE_STATUS + " int default 0);";


}
