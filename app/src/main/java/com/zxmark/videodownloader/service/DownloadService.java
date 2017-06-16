package com.zxmark.videodownloader.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.downloader.VideoDownloadFactory;
import com.zxmark.videodownloader.floatview.FloatViewManager;
import com.zxmark.videodownloader.util.ActivityManagerUtils;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.NetWorkUtil;
import com.zxmark.videodownloader.widget.IToast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fanlitao on 17/6/7.
 */

public class DownloadService extends Service {


    public static final String DIR = "mob_ins_downloader";
    public static final String DOWNLOAD_ACTION = "download_action";
    public static final String REQUEST_VIDEO_URL_ACTION = "request_video_url_action";
    public static final String REQUEST_DOWNLOAD_VIDEO_ACTION = "request_download_video_action";
    public static final String DOWNLOAD_URL = "download_url";

    public static final int MSG_DOWNLOAD_SUCCESS = 0;
    public static final int MSG_DOWNLOAD_ERROR = 1;
    public static final int MSG_DOWNLOAD_START = 2;
    public static final int MSG_UPDATE_PROGRESS = 3;


    final RemoteCallbackList<IDownloadCallback> mCallbacks = new RemoteCallbackList<IDownloadCallback>();

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DOWNLOAD_SUCCESS) {
                IToast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
                FloatViewManager.getDefault().dismissFloatView();
                DownloadService.this.notifyDownloadFinished((String) msg.obj);
            } else if (msg.what == MSG_DOWNLOAD_ERROR) {
                IToast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
            } else if (msg.what == MSG_DOWNLOAD_START) {
                IToast.makeText(DownloadService.this, "start download", Toast.LENGTH_SHORT).show();
                DownloadService.this.notifyStartDownload((String) msg.obj);
            } else if (msg.what == MSG_UPDATE_PROGRESS) {
                DownloadService.this.notifyDownloadProgress((String) msg.obj, msg.arg1);
            }
        }
    };

    private void showFloatView() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!ActivityManagerUtils.isTopActivity(DownloadService.this)) {
                    FloatViewManager manager = FloatViewManager.getDefault();
                    manager.showFloatView();
                }
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            LogUtil.v("TL", "onHandleIntent:" + intent.getAction());
            if (DOWNLOAD_ACTION.equals(intent.getAction())) {
                final String url = intent.getStringExtra(Globals.EXTRAS);
                if (TextUtils.isEmpty(url)) {
                    return super.onStartCommand(intent, flags, startId);
                }

                DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        PowerfulDownloader.getDefault().startDownload(url, new PowerfulDownloader.IPowerfulDownloadCallback() {
                            @Override
                            public void onStart(String path) {

                            }

                            @Override
                            public void onFinish(int statusCode, String path) {
                                mHandler.obtainMessage(MSG_DOWNLOAD_SUCCESS).sendToTarget();
                            }

                            @Override
                            public void onError(int errorCode) {

                            }

                            @Override
                            public void onProgress(String path, int progress) {
//                        mHandler.obtainMessage(MSG_UPDATE_PROGRESS, progress, 0).sendToTarget();
//                        DownloadService.this.notifyDownloadProgress(path, progress);
                            }
                        });
                    }
                });

            } else if (REQUEST_VIDEO_URL_ACTION.equals(intent.getAction())) {
                final String url = intent.getStringExtra(Globals.EXTRAS);
                LogUtil.e("ds","NetworkUtil.isWifi:" + NetWorkUtil.isWifi(this));
                if(NetWorkUtil.isWifi(this) && false) {
                    //TODO:directly download video  with  wifi
                    DownloadingTaskList.SINGLETON.setHandler(mHandler);
                    DownloadingTaskList.SINGLETON.addNewDownloadTask(url, true);
                } else {
                    DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
                        @Override
                        public void run() {
                            WebPageStructuredData webPageStructuredData = VideoDownloadFactory.getInstance().request(url);
                            if (webPageStructuredData != null) {
                                if (webPageStructuredData.futureVideoList != null && webPageStructuredData.futureVideoList.size() > 0) {
                                    showFloatView();
                                    DBHelper.getDefault().insertNewTask(webPageStructuredData.pageTitle, url, webPageStructuredData.videoThumbnailUrl, webPageStructuredData.futureVideoList.get(0), webPageStructuredData.appPageUrl, DownloadUtil.getDownloadTargetInfo(webPageStructuredData.futureVideoList.get(0)));
                                }

                                if (webPageStructuredData.futureImageList != null && webPageStructuredData.futureImageList.size() > 0) {
                                    showFloatView();
                                    DBHelper.getDefault().insertNewTask(webPageStructuredData.pageTitle, url, webPageStructuredData.futureImageList.get(0), webPageStructuredData.futureImageList.get(0), webPageStructuredData.appPageUrl, DownloadUtil.getDownloadTargetInfo(webPageStructuredData.futureImageList.get(0)));
                                }
                            }
                        }
                    });
                }
            } else if (REQUEST_DOWNLOAD_VIDEO_ACTION.equals(intent.getAction())) {
                String url = intent.getStringExtra(Globals.EXTRAS);
                DownloadingTaskList.SINGLETON.setHandler(mHandler);
                DownloadingTaskList.SINGLETON.addNewDownloadTask(url, false);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */


//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        LogUtil.v("TL", "onHandleIntent:" + intent.getAction());
//        if (DOWNLOAD_ACTION.equals(intent.getAction())) {
//            String url = intent.getStringExtra(DOWNLOAD_URL);
//            if(TextUtils.isEmpty(url)) {
//                return;
//            }
//            PowerfulDownloader.getDefault().startDownload(url, new PowerfulDownloader.IPowerfulDownloadCallback() {
//                @Override
//                public void onStart(String path) {
//
//                }
//
//                @Override
//                public void onFinish(String path) {
//                    mHandler.obtainMessage(MSG_DOWNLOAD_SUCCESS).sendToTarget();
//                }
//
//                @Override
//                public void onError(int errorCode) {
//
//                }
//
//                @Override
//                public void onProgress(String path, int progress) {
//                    mHandler.obtainMessage(MSG_UPDATE_PROGRESS,progress,0).sendToTarget();
//                    DownloadService.this.notifyDownloadProgress(path, progress);
//                }
//            });
//        } else if (REQUEST_VIDEO_URL_ACTION.equals(intent.getAction())) {
//            String url = intent.getStringExtra(Globals.EXTRAS);
//            LogUtil.e("downloadSerivice","url:" + url);
//
//            DownloadingTaskList.SINGLETON.addNewDownloadTask(url);
//            WebPageStructuredData webPageStructuredData = VideoDownloadFactory.getInstance().request(url);
//            downloadVideo(webPageStructuredData);
//            downloadImage(webPageStructuredData);
//        }
//    }
    private boolean startDownload(String fileUrl) {
        if (TextUtils.isEmpty(fileUrl)) {
            return false;
        }
        Log.v("download", "startDownload:" + fileUrl);
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String targetPath = null;
        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            LogUtil.e("TL", "connection.getResponseCode:" + connection.getResponseCode());
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.v("download", "request.responseCode is not ok");
                return false;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            LogUtil.e("TL", "fileLength=" + fileLength);
            // download the file
            input = connection.getInputStream();
            targetPath = DownloadUtil.getDownloadTargetInfo(fileUrl);
            output = new FileOutputStream(targetPath);
            notifyStartDownload(targetPath);
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress(targetPath, (int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            LogUtil.e("TL", "total.byte=" + total);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("TL", "exception:" + e.getMessage());
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
                LogUtil.e("TL", "exception:" + ignored.getMessage());
                return false;
            }

            if (connection != null)
                connection.disconnect();
        }

        return true;
    }

    public void publishProgress(String filePath, int progress) {
        notifyDownloadProgress(filePath, progress);
    }

    private Object sCallbackLock = new Object();

    private void notifyDownloadProgress(String filePath, int progress) {
        synchronized (sCallbackLock) {
            if (mCallbacks.getRegisteredCallbackCount() > 0) {
                final int N = mCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onPublishProgress(filePath, progress);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                mCallbacks.finishBroadcast();
            }

        }
    }

    private void notifyStartDownload(String filePath) {
        if (mCallbacks.getRegisteredCallbackCount() > 0) {
            final int N = mCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    mCallbacks.getBroadcastItem(i).onStartDownload(filePath);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
            }
            mCallbacks.finishBroadcast();
        }

    }

    private void notifyDownloadFinished(String filePath) {
        if (mCallbacks.getRegisteredCallbackCount() > 0) {
            final int N = mCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    mCallbacks.getBroadcastItem(i).onDownloadSuccess(filePath);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
            }
            mCallbacks.finishBroadcast();
        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private final IDownloadBinder.Stub mBinder = new IDownloadBinder.Stub() {
        @Override
        public void registerCallback(IDownloadCallback callback) throws RemoteException {
            mCallbacks.register(callback);
        }

        @Override
        public void unregisterCallback(IDownloadCallback callback) throws RemoteException {
            mCallbacks.unregister(callback);
        }
    };
}
