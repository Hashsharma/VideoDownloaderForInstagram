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

import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.bean.WebPageStructuredData;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.downloader.VideoDownloadFactory;
import com.zxmark.videodownloader.floatview.FloatViewManager;
import com.zxmark.videodownloader.util.ActivityManagerUtils;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.EventUtil;
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
    public static final String DOWNLOAD_PAGE_URL = "page_url";
    public static final String REQUEST_VIDEO_URL_ACTION = "request_video_url_action";
    public static final String REQUEST_DOWNLOAD_VIDEO_ACTION = "request_download_video_action";
    public static final String EXTRAS_FLOAT_VIEW = "extras_float_view";
    public static final String DOWNLOAD_URL = "download_url";

    public static final int MSG_DOWNLOAD_SUCCESS = 0;
    public static final int MSG_DOWNLOAD_ERROR = 1;
    public static final int MSG_DOWNLOAD_START = 2;
    public static final int MSG_UPDATE_PROGRESS = 3;
    public static final int MSG_NOTIFY_DOWNLOADED = 4;
    public static final int MSG_HANDLE_SEND_ACTION = 5;
    public static final int MSG_REQUSET_URL_ERROR = 6;


    final RemoteCallbackList<IDownloadCallback> mCallbacks = new RemoteCallbackList<IDownloadCallback>();

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DOWNLOAD_SUCCESS) {
                if (msg.obj != null) {
                    IToast.makeText(DownloadService.this, R.string.download_result_success, Toast.LENGTH_SHORT).show();
                    DownloadService.this.notifyDownloadFinished((String) msg.obj);
                }
            } else if (msg.what == MSG_DOWNLOAD_ERROR) {
                IToast.makeText(DownloadService.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            } else if (msg.what == MSG_DOWNLOAD_START) {
                DownloadService.this.notifyStartDownload((String) msg.obj);
            } else if (msg.what == MSG_UPDATE_PROGRESS) {
                DownloadService.this.notifyDownloadProgress((String) msg.obj, msg.arg2, msg.arg1);
            } else if (msg.what == MSG_NOTIFY_DOWNLOADED) {
                IToast.makeText(DownloadService.this, R.string.toast_downlaoded_video, Toast.LENGTH_SHORT).show();
                DownloadService.this.notifyReceiveNewTask(getString(R.string.toast_downlaoded_video));
            } else if (msg.what == MSG_HANDLE_SEND_ACTION) {
                if (msg.obj == null) {
                    IToast.makeText(DownloadService.this, R.string.spider_request_error, Toast.LENGTH_SHORT).show();
                }
                DownloadService.this.notifyReceiveNewTask((String) msg.obj);
            }
        }
    };

    private void showFloatView() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (DownloadingTaskList.SINGLETON.getFutureTask().size() == 0) {
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
                final String pageURL = intent.getStringExtra(DOWNLOAD_PAGE_URL);

                if (TextUtils.isEmpty(url)) {
                    return super.onStartCommand(intent, flags, startId);
                }
                DownloadContentItem item = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
                final String homeDir = item.getTargetDirectory(url);
                DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        PowerfulDownloader.getDefault().startDownload(pageURL, 0, url, homeDir, new PowerfulDownloader.IPowerfulDownloadCallback() {
                            @Override
                            public void onStart(String path) {

                            }

                            @Override
                            public void onFinish(int statusCode, String pageURL, int filePositon, String path) {
                                mHandler.sendEmptyMessage(MSG_DOWNLOAD_SUCCESS);
                            }

                            @Override
                            public void onError(int errorCode) {

                            }

                            @Override
                            public void onProgress(String pageURL, int filePositon, String path, int progress) {

                            }
                        });
                    }
                });

            } else if (REQUEST_VIDEO_URL_ACTION.equals(intent.getAction())) {
                final String url = intent.getStringExtra(Globals.EXTRAS);
                if (TextUtils.isEmpty(url)) {
                    return super.onStartCommand(intent, flags, startId);
                }
                final boolean showFloatView = intent.getBooleanExtra(DownloadService.EXTRAS_FLOAT_VIEW, true);
                if (DownloaderDBHelper.SINGLETON.isExistDownloadedPageURL(url)) {
                    mHandler.sendEmptyMessage(MSG_NOTIFY_DOWNLOADED);
                    return super.onStartCommand(intent, flags, startId);
                }


                DownloadingTaskList.SINGLETON.setHandler(mHandler);
                DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        DownloadContentItem downloadContentItem = null;
                        downloadContentItem = VideoDownloadFactory.getInstance().request(url);

                        if (downloadContentItem != null && downloadContentItem.getFileCount() > 0) {
                            EventUtil.getDefault().onEvent("download", "DownloadService.StartDownload:" + url);
                            if (showFloatView) {
                                showFloatView();
                            }
                            LogUtil.e("download", "startDownload:" + url + ":" + downloadContentItem.pageHOME);
                            String pageHome = DownloaderDBHelper.SINGLETON.getPageHomeByPageURL(url);
                            LogUtil.e("download", "startDownload:existHome=" + pageHome);
                            if (!TextUtils.isEmpty(pageHome)) {
                                downloadContentItem.pageHOME = pageHome;
                                downloadContentItem.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOADING;
                            }
                            DownloaderDBHelper.SINGLETON.saveNewDownloadTask(downloadContentItem);
                            mHandler.obtainMessage(MSG_DOWNLOAD_START, downloadContentItem.pageURL).sendToTarget();
                            DownloadingTaskList.SINGLETON.addNewDownloadTask(url, downloadContentItem);
                        } else {
                            mHandler.sendEmptyMessage(MSG_HANDLE_SEND_ACTION);
                        }
                    }
                });


            } else if (REQUEST_DOWNLOAD_VIDEO_ACTION.equals(intent.getAction())) {
                String url = intent.getStringExtra(Globals.EXTRAS);
                if (DownloaderDBHelper.SINGLETON.isExistDownloadedPageURL(url)) {
                    mHandler.sendEmptyMessage(MSG_NOTIFY_DOWNLOADED);
                    return super.onStartCommand(intent, flags, startId);
                }
                DownloadingTaskList.SINGLETON.setHandler(mHandler);
                DownloadingTaskList.SINGLETON.addNewDownloadTask(url);
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
    public void publishProgress(String pageURL, int filePosition, int progress) {
        notifyDownloadProgress(pageURL, filePosition, progress);
    }

    private Object sCallbackLock = new Object();

    private void notifyDownloadProgress(String pageUrl, int filePosition, int progress) {
        synchronized (sCallbackLock) {
            if (mCallbacks.getRegisteredCallbackCount() > 0) {
                final int N = mCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onPublishProgress(pageUrl, filePosition, progress);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                mCallbacks.finishBroadcast();
            }

        }
    }

    private void notifyReceiveNewTask(String pageURL) {
        synchronized (sCallbackLock) {
            if (mCallbacks.getRegisteredCallbackCount() > 0) {
                final int N = mCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onReceiveNewTask(pageURL);
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
