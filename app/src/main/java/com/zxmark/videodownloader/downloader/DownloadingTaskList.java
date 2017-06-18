package com.zxmark.videodownloader.downloader;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.service.PowerfulDownloader;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.widget.IToast;

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
    private HashMap<String,WebPageStructuredData>  mFutureTaskDetailMap = new HashMap<>();

    private DownloadingTaskList() {

    }

    public void addNewDownloadTask(String taskId, boolean needSaveDb) {
        if (mFuturedTaskList.size() > 0) {
            if (mFuturedTaskList.contains(taskId)) {
                return;
            }
            mFuturedTaskList.add(taskId);
            return;
        }


        mFuturedTaskList.add(taskId);
        executeNextTask(needSaveDb);
    }

    public void addNewDownloadTask(String taskId,WebPageStructuredData data) {
        if (mFuturedTaskList.size() > 0) {
            if (mFuturedTaskList.contains(taskId)) {
                return;
            }
            mFuturedTaskList.add(taskId);
            mFutureTaskDetailMap.put(taskId,data);
            return;
        }


        mFuturedTaskList.add(taskId);
        mFutureTaskDetailMap.put(taskId,data);
        executeNextTask(false);
    }



    private Handler mHandler;

    public void setHandler(Handler handler) {
        mHandler = handler;
    }


    public void intrupted(String taskId) {
        PowerfulDownloader.getDefault().interupted();
        if (!TextUtils.isEmpty(taskId)) {
            mFuturedTaskList.remove(taskId);
        }
    }

    private void downloadVideo(final String taskId, WebPageStructuredData data) {
        if (data.futureVideoList != null && data.futureVideoList.size() > 0) {
            for (String videoUrl : data.futureVideoList) {
                mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_START, 0, 0, DownloadUtil.getDownloadTargetInfo(videoUrl)).sendToTarget();
                PowerfulDownloader.getDefault().startDownload(videoUrl, new PowerfulDownloader.IPowerfulDownloadCallback() {
                    @Override
                    public void onStart(String path) {

                    }

                    @Override
                    public void onFinish(int code, String path) {
                        LogUtil.e("download","code:" + code);
                        DBHelper.getDefault().finishDownload(path);
                        mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_SUCCESS, 0, 0, path).sendToTarget();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onProgress(String path, int progress) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                        msg.arg1 = progress;
                        msg.obj = path;
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
    }

    private void downloadImage(final String taskId, WebPageStructuredData data) {
        if (data.futureImageList != null && data.futureImageList.size() > 0) {
            for (String imageUrl : data.futureImageList) {
                mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_START, 0, 0, DownloadUtil.getDownloadTargetInfo(imageUrl)).sendToTarget();
                LogUtil.e("download", imageUrl);
                PowerfulDownloader.getDefault().startDownload(imageUrl, new PowerfulDownloader.IPowerfulDownloadCallback() {
                    @Override
                    public void onStart(String path) {

                    }

                    @Override
                    public void onFinish(int statusCode, String path) {
                        DBHelper.getDefault().finishDownload(path);
                        mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_SUCCESS, 0, 0, path).sendToTarget();
                    }

                    @Override
                    public void onError(int errorCode) {

                    }

                    @Override
                    public void onProgress(String path, int progress) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                        msg.arg1 = progress;
                        msg.obj = path;
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
        //TODO:
    }

    public void finishTask(String taskId) {
        mFuturedTaskList.remove(taskId);
        mFutureTaskDetailMap.remove(taskId);
    }

    public void executeNextTask(final boolean needSaveDB) {
        if (mFuturedTaskList.size() > 0) {
            final String taskId = mFuturedTaskList.get(0);
            LogUtil.e("task", "startExecuteTaskId:" + taskId);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {

                    WebPageStructuredData  cacheData = mFutureTaskDetailMap.get(taskId);
                    if(cacheData != null) {
                        //TODO:之前已经请求过网络，合理直接诶进行下载
                        downloadVideo(taskId, cacheData);
                        downloadImage(taskId, cacheData);
                    } else {
                        WebPageStructuredData webPageStructuredData = VideoDownloadFactory.getInstance().request(taskId);
                        if (webPageStructuredData.futureImageList != null || webPageStructuredData.futureVideoList != null) {
                            if (needSaveDB) {
                                if (webPageStructuredData != null) {
                                    if (webPageStructuredData.futureVideoList != null && webPageStructuredData.futureVideoList.size() > 0) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                DownloadUtil.showFloatView();
                                            }
                                        });
                                        DBHelper.getDefault().insertNewTask(webPageStructuredData.pageTitle, taskId, webPageStructuredData.videoThumbnailUrl, webPageStructuredData.futureVideoList.get(0), webPageStructuredData.appPageUrl, DownloadUtil.getDownloadTargetInfo(webPageStructuredData.futureVideoList.get(0)));
                                    }

                                    if (webPageStructuredData.futureImageList != null && webPageStructuredData.futureImageList.size() > 0) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                DownloadUtil.showFloatView();
                                            }
                                        });
                                        DBHelper.getDefault().insertNewTask(webPageStructuredData.pageTitle, taskId, webPageStructuredData.futureImageList.get(0), webPageStructuredData.futureImageList.get(0), webPageStructuredData.appPageUrl, DownloadUtil.getDownloadTargetInfo(webPageStructuredData.futureImageList.get(0)));
                                    }
                                }
                            }
                            downloadVideo(taskId, webPageStructuredData);
                            downloadImage(taskId, webPageStructuredData);
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
                    executeNextTask(needSaveDB);
                }
            });
        }
    }


    public ExecutorService getExecutorService() {
        return mExecutorService;
    }
}
