package com.zxmark.videodownloader.downloader;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.service.LearningDownloader;
import com.zxmark.videodownloader.service.PowerfulDownloader;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.MimeTypeUtil;
import com.zxmark.videodownloader.widget.IToast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

/**
 * Created by fanlitao on 17/6/9.
 */

public class DownloadingTaskList {

    public static final DownloadingTaskList SINGLETON = new DownloadingTaskList();

    private ExecutorService mExecutorService = Executors.newCachedThreadPool();


    private List<String> mFuturedTaskList = new LinkedList<>();
    private HashMap<String, DownloadContentItem> mFutureTaskDetailMap = new HashMap<>();

    private DownloadingTaskList() {

    }

    public void addNewDownloadTask(String taskId) {
        if (mFuturedTaskList.size() > 0) {
            if (mFuturedTaskList.contains(taskId)) {
                return;
            }
            mFuturedTaskList.add(taskId);
            return;
        }


        mFuturedTaskList.add(taskId);
        executeNextTask();
    }

    public void addNewDownloadTask(String taskId, DownloadContentItem data) {
        if (mFuturedTaskList.size() > 0) {
            if (mFuturedTaskList.contains(taskId)) {
                return;
            }
            mFuturedTaskList.add(taskId);
            mFutureTaskDetailMap.put(taskId, data);
            return;
        }


        mFuturedTaskList.add(taskId);
        mFutureTaskDetailMap.put(taskId, data);
        executeNextTask();
    }


    private Handler mHandler;

    public void setHandler(Handler handler) {
        mHandler = handler;
    }


    public void intrupted(String taskId) {
        if (TextUtils.isEmpty(taskId)) {
            return;
        }

        if (taskId.equals(LearningDownloader.getDefault().getCurrentDownloadingTaskId())) {
            LearningDownloader.getDefault().interupted();
        }

        if (taskId.equals(PowerfulDownloader.getDefault().getCurrentDownloadingTaskId())) {
            PowerfulDownloader.getDefault().interupted();
        }
        mFuturedTaskList.remove(taskId);
    }

    /**
     * 开始下载当前任务的入口API
     *
     * @param item
     */
    private void downloadItemContent(final DownloadContentItem item) {
        if (item != null) {
            List<String> futureDownloadedList = item.getDownloadContentList();
            downloadItem(futureDownloadedList, item);
            LogUtil.e("download", item.pageURL + ":下载完成");
            DownloaderDBHelper.SINGLETON.finishDownloadTask(item.pageURL);
            mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_SUCCESS,0,0,item.pageURL).sendToTarget();
        }
    }


    private void downloadItem(final List<String> totalDownloadedList, final DownloadContentItem item) {
        if (item.getVideoCount() > 0) {
            final String fileURL = item.getVideoList().remove(0);
            final int filePositon = totalDownloadedList.indexOf(fileURL);
            LearningDownloader.getDefault().startDownload(filePositon, item.pageURL, fileURL, item.getTargetDirectory(fileURL), new LearningDownloader.IPowerfulDownloadCallback() {
                @Override
                public void onStart(String path) {

                }

                @Override
                public void onFinish(int code, String pageURL, int filePosition, String path) {
                    LogUtil.e("download", "LearningDownloadercode:" + code + ":" + pageURL);
                    if (code == PowerfulDownloader.CODE_OK) {
                        // mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_SUCCESS, filePosition, 0, pageURL).sendToTarget();
                        Message msg = mHandler.obtainMessage();
                        msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                        msg.arg1 = 100;
                        msg.arg2 = filePosition;
                        msg.obj = pageURL;
                        mHandler.sendMessage(msg);
                    } else if (code == PowerfulDownloader.CODE_DOWNLOAD_FAILED) {
                    } else if (code == PowerfulDownloader.CODE_DOWNLOAD_CANCELED) {
                        DownloaderDBHelper.SINGLETON.deleteDownloadTask(pageURL);
                    }
                    downloadItem(totalDownloadedList, item);
                }

                @Override
                public void onError(int errorCode) {
                }

                @Override
                public void onProgress(String pageURL, int filePosition, String path, int progress) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                    msg.arg1 = progress;
                    msg.arg2 = filePosition;
                    msg.obj = pageURL;
                    mHandler.sendMessage(msg);
                }
            });
        } else if (item.getImageCount() > 0) {
            final String fileURL = item.getImageList().remove(0);
            final int filePositon = totalDownloadedList.indexOf(fileURL);
            PowerfulDownloader.getDefault().startDownload(item.pageURL, filePositon, fileURL, item.getTargetDirectory(fileURL), new PowerfulDownloader.IPowerfulDownloadCallback() {
                @Override
                public void onStart(String path) {

                }

                @Override
                public void onFinish(int statusCode, String pageURL, int filePosition, String path) {
                    LogUtil.e("download", "PowerfulDownloaderonFinish=" + statusCode + ":" + pageURL);
                    Message msg = mHandler.obtainMessage();
                    msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                    msg.arg1 = 100;
                    msg.arg2 = filePosition;
                    msg.obj = pageURL;
                    mHandler.sendMessage(msg);

                    downloadItem(totalDownloadedList, item);
                }

                @Override
                public void onError(int errorCode) {

                }

                @Override
                public void onProgress(String pageURL, int filePosition, String path, int progress) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                    msg.arg1 = progress;
                    msg.arg2 = filePosition;
                    msg.obj = pageURL;
                    mHandler.sendMessage(msg);
                }
            });
        }
    }
//
//    private void downloadImage(final String taskId, final DownloadContentItem data) {
//        if (data.futureImageList != null && data.futureImageList.size() > 0) {
//            String imageUrl = data.futureImageList.remove(0);
//            mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_START, 0, 0, data.pageURL).sendToTarget();
//            LogUtil.e("download", imageUrl);
//
//        }
//        //TODO:
//    }

    public void finishTask(String taskId) {
        mFuturedTaskList.remove(taskId);
        mFutureTaskDetailMap.remove(taskId);
    }

    public void executeNextTask() {
        if (mFuturedTaskList.size() > 0) {
            final String taskId = mFuturedTaskList.get(0);
            LogUtil.e("task", "startExecuteTaskId:" + taskId);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadContentItem cacheData = mFutureTaskDetailMap.get(taskId);
                    if (cacheData != null) {
                        //TODO:之前已经请求过网络，合理直接诶进行下载
                        mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_START, 0, 0, cacheData.pageURL).sendToTarget();
                        downloadItemContent(cacheData);
                    } else {
                        DownloadContentItem downloadContentItem = VideoDownloadFactory.getInstance().request(taskId);
                        if (downloadContentItem != null) {
                            if (downloadContentItem != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DownloadUtil.showFloatView();
                                    }
                                });
                                DownloaderDBHelper.SINGLETON.saveNewDownloadTask(downloadContentItem);
                                mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_START, 0, 0, downloadContentItem.pageURL).sendToTarget();
                                downloadItemContent(downloadContentItem);
                            }
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    IToast.makeText(MainApplication.getInstance().getApplicationContext(), R.string.spider_request_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    finishTask(taskId);
                    executeNextTask();
                }
            });
        }
    }


    public ExecutorService getExecutorService() {
        return mExecutorService;
    }
}
