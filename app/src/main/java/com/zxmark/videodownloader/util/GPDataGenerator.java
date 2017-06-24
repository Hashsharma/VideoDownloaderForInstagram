package com.zxmark.videodownloader.util;

import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;

/**
 * Created by fanlitao on 6/24/17.
 */

public class GPDataGenerator {


    public static void saveGPTask() {
        DownloadContentItem item = new DownloadContentItem();
        item.pageURL = "http://www.ys0316.com/6228501.htm";
        item.pageThumb = "http://image.ys0316.com/upload/5/06/5066ac586d8c855c349a8c93bb8be05b_thumb.jpg";
        item.pageTitle = "Antelope Canyon";
        item.pageTags="#ins_hashtags";
        item.fileCount = 1;
        DownloaderDBHelper.SINGLETON.saveNewDownloadTask(item);

    }
}
