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
        item.addImage("ddd1111");
        item.addImage("ddd");
        DownloaderDBHelper.SINGLETON.saveNewDownloadTask(item);
        DownloadContentItem item3 = new DownloadContentItem();
        item3.pageURL = "http://www.ys0316.com/63333228501.htm";
        item3.pageThumb = "http://image.ys0316.com/upload/5/06/5066ac586d8c855c349a8c93bb8be05b_thumb.jpg";
        item3.pageTitle = "Antelope Canyon";
        item3.pageTags="#ins_hashtags";
        item3.addImage("ddd1111");
        item3.addImage("ddd");
        item3.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED;
        item3.fileCount = 1;
        DownloaderDBHelper.SINGLETON.saveNewDownloadTask(item3);


        DownloadContentItem item2 = new DownloadContentItem();
        item2.pageURL = "http://www.ys0316.com/622850QQ1.htm";
        item.pageThumb = "http://image.ys0316.com/upload/5/06/5066ac586d8c855c349a8c93bb8be05b_thumb.jpg";
        item2.pageTitle = "Antelope Canyon";
        item2.pageTags="#ins_hashtags";
        item2.fileCount = 2;
        item2.addVideo("2222");
        item2.addVideo("234234234");
        item2.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED;
        DownloaderDBHelper.SINGLETON.saveNewDownloadTask(item2);

        DownloadContentItem item4 = new DownloadContentItem();
        item4.pageURL = "http://www.ys0316.com/622850QQ221.htm";
        item4.pageThumb = "https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1498276777&di=03d1804c6a88e4918d9a5422a8d8aa97&src=http://www.360gann.com/360gannAdmin/UploadFile/image/8.jpg";
        item4.pageTitle = "Beauty Antelope Canyon";
        item4.pageTags="#ins_hashtags";
        item4.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED;
        DownloaderDBHelper.SINGLETON.saveNewDownloadTask(item4);

    }
}
