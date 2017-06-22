package com.zxmark.videodownloader.service;

import com.zxmark.videodownloader.spider.HttpRequestSpider;
import com.zxmark.videodownloader.util.CpuUtils;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fanlitao on 17/6/9.
 * <p>
 * 实现自学习的多线程下载器
 */

public class LearningDownloader {


    public static final String TAG = "learning";
    public static final int CODE_OK = 0;
    public static final int CODE_DOWNLOAD_FAILED = -1;
    public static final int CODE_DOWNLOAD_CANCELED = 100;
    public volatile static LearningDownloader sInstance;

    public static final int MAX_RETRY_TIMES = 5;

    public int THREAD_COUNT = 1;

    private volatile int mReadBytesCount = 0;

    private AtomicBoolean mInternalErrorInterupted = new AtomicBoolean(false);
    private IPowerfulDownloadCallback mCallback;

    private ConcurrentHashMap<Integer, DownloadingThread> mDownloadingTaskMap = new ConcurrentHashMap<>();

    private String mCurrentTaskId;

    private LearningDownloader() {
        int cpuCount = CpuUtils.getNumberOfCPUCores() + 1;
        THREAD_COUNT = cpuCount;
    }


    public static LearningDownloader getDefault() {
        synchronized (LearningDownloader.class) {
            if (sInstance == null) {
                sInstance = new LearningDownloader();
            }
        }
        return sInstance;
    }


    public void interupted() {
        LogUtil.e(TAG, "interrupted");
        mInternalErrorInterupted.set(true);
    }

    public void startDownload(String taskId, String fileUrl, IPowerfulDownloadCallback callback) {
        mCallback = callback;
        mCurrentTaskId = taskId;
        LogUtil.e(TAG, "startDownload:" + taskId);
        long start = System.currentTimeMillis();
        download(fileUrl, DownloadUtil.getDownloadTargetInfo(fileUrl), THREAD_COUNT, 0, true);
        LogUtil.e(TAG, "time = " + (System.currentTimeMillis() - start));
    }

    /**
     * 下载文件
     */
    private void download(String fileUrl, String targetPath, int threadNum, int retryTime, boolean notifyCallback) {
        int codeStatus = CODE_OK;
        CountDownLatch latch = null;
        if (threadNum > 1) {
            latch = new CountDownLatch(threadNum);
        }
        mDownloadingTaskMap.clear();
        mInternalErrorInterupted.set(false);
        mReadBytesCount = 0;
        HttpURLConnection conn = null;
        boolean firstRequestFailed = false;
        int targetLength = 0;
        try {
            //通过下载路径获取连接
            URL url = new URL(fileUrl);
            conn = (HttpURLConnection) url.openConnection();
            //设置连接的相关属性
            conn.setRequestMethod(HttpRequestSpider.METHOD_GET);
            conn.setReadTimeout(HttpRequestSpider.CONNECTION_TIMEOUT);
            //判断连接是否正确。
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 获取文件大小。
                int fileSize = conn.getContentLength();
                targetLength = fileSize;
                if (fileSize <= 0) {
                    if (mCallback != null) {
                        mCallback.onFinish(CODE_DOWNLOAD_FAILED, targetPath);
                    }
                    return;
                }
                File file = new File(targetPath);
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(fileSize);
                raf.close();
                // 将文件分成threadNum = 5份。
                int block = fileSize % threadNum == 0 ? fileSize / threadNum
                        : fileSize / threadNum + 1;
                for (int threadId = 0; threadId < threadNum; threadId++) {
                    new DownloadThread(threadId, fileSize, block, file, url, latch).start();

                }
                LogUtil.e(TAG, "wait all downloading thread");
                if (latch != null) {
                    latch.await();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            firstRequestFailed = true;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        LogUtil.e(TAG, "internalErrorIntruppted:" + mInternalErrorInterupted.get());
        if (mInternalErrorInterupted.get()) {
            if (mInternalErrorInterupted.get()) {
                codeStatus = CODE_DOWNLOAD_CANCELED;
            }
        } else {
            if (firstRequestFailed) {
                codeStatus = CODE_DOWNLOAD_FAILED;
            } else {
                retryLearningDownload(1);
                if (mDownloadingTaskMap.size() > 0) {
                    codeStatus = CODE_DOWNLOAD_FAILED;
                }
            }
        }

        LogUtil.e("download", targetLength + ":" + new File(targetPath).length());

        if (targetLength != new File(targetPath).length()) {
            LogUtil.e(TAG,"multi task download file size different");
           boolean result =  startDownloadBySingleThread(fileUrl, targetPath);
            if(!result) {
                codeStatus = CODE_DOWNLOAD_FAILED;
            }
        }

        if (notifyCallback) {
            if (mCallback != null) {
                mCallback.onFinish(codeStatus, targetPath);
            }

        }
        mReadBytesCount = 0;
        mCurrentTaskId = null;
    }


    private void retryLearningDownload(int retryTime) {
        if (mDownloadingTaskMap.size() > 0) {
            LogUtil.e(TAG, "retryLearningDownload:" + retryTime);
            if (retryTime <= MAX_RETRY_TIMES) {
                if (mDownloadingTaskMap.size() == THREAD_COUNT) {
                    //TODO:开启多线程下载是失败的策略，在这里选择单线程下载
                    LogUtil.e(TAG, "start single thread");
                    DownloadingThread thread = mDownloadingTaskMap.get(0);
                    startDownloadBySingleThread(thread.url, thread.file);
                } else {
                    LogUtil.e(TAG, "multi task download error:" + mDownloadingTaskMap.size());
                    CountDownLatch retryLatch = new CountDownLatch(mDownloadingTaskMap.size());
                    HashMap<Integer, DownloadingThread> tempTaskMap = new HashMap(mDownloadingTaskMap);
                    mDownloadingTaskMap.clear();
                    for (Integer threadId : tempTaskMap.keySet()) {
                        DownloadingThread futureTask = tempTaskMap.get(threadId);
                        if (futureTask != null) {
                            LogUtil.e(TAG, "futureTask:" + futureTask.threadId + ":" + mReadBytesCount);
                            new DownloadThread(futureTask, retryLatch).start();
                        }
                    }
                    tempTaskMap.clear();
                    try {
                        LogUtil.e(TAG, "learing downloader retry  wait");
                        retryLatch.await();
                        retryLearningDownload(++retryTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                LogUtil.e(TAG, "beyond max retry times");
            }
        }
    }

    public String getCurrentDownloadingTaskId() {
        return mCurrentTaskId;
    }

    private boolean startDownloadBySingleThread(String stringUrl, String targetpath) {
        try {
            URL requestUrl = new URL(stringUrl);
            return startDownloadBySingleThread(requestUrl, new File(targetpath));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean startDownloadBySingleThread(URL requestUrl, File targetFile) {
        HttpURLConnection conn = null;
        //通过下载路径获取连接
        try {

            conn = (HttpURLConnection) requestUrl.openConnection();
            //设置连接的相关属性
            conn.setRequestMethod(HttpRequestSpider.METHOD_GET);
            conn.setReadTimeout(HttpRequestSpider.CONNECTION_TIMEOUT);
            //判断连接是否正确。
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 获取文件大小。
                int fileSize = conn.getContentLength();
                if (fileSize <= 0) {
                    if (mCallback != null) {
                        mCallback.onFinish(CODE_DOWNLOAD_FAILED, targetFile.getAbsolutePath());
                    }
                    return false;
                }
                //得到文件名
                //根据文件大小及文件名，创建一个同样大小，同样文件名的文件
                InputStream fis = conn.getInputStream();
                // int i = httpUrlConnection.getContentLength();
                int i = 1024;
                byte[] buffer = new byte[i];
                // fis.read(b);
                int byteCount;
                FileOutputStream fos = new FileOutputStream(targetFile);
                byte[] temp;

                String targetPath = targetFile.getAbsolutePath();
                while ((byteCount = fis.read(buffer)) != -1) {
                    // 重点注意这样的写法
                    if (mInternalErrorInterupted.get()) {
                        break;
                    }
                    fos.write(buffer, 0, byteCount);
                    fos.flush();
                    mReadBytesCount += byteCount;
                    if (mCallback != null) {
                        mCallback.onProgress(targetPath, (int) (1 + 100 * (mReadBytesCount * 1.0f / fileSize)));
                    }
                }
                fis.close();
                fos.close();

                return true;
            }

            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    //文件下载线程
    class DownloadThread extends Thread {
        int start, end, threadId;
        File file = null;
        URL url = null;
        CountDownLatch latch;
        String filePath;
        int fileSize;

        public DownloadThread(int threadId, int fileSize, int block, File file, URL url, CountDownLatch latch) {
            this.threadId = threadId;
            start = block * threadId;
            end = block * (threadId + 1) - 1;
            this.file = file;
            this.url = url;
            this.latch = latch;
            filePath = file.getAbsolutePath();
            this.fileSize = fileSize;
            DownloadingThread thread = new DownloadingThread();
            thread.threadId = threadId;
            thread.fileSize = fileSize;
            thread.block = block;
            thread.file = file;
            thread.url = url;
            mDownloadingTaskMap.put(threadId, thread);
        }

        public DownloadThread(DownloadingThread downloadingThread, CountDownLatch latch) {
            this.threadId = downloadingThread.threadId;
            start = downloadingThread.block * threadId;
            end = downloadingThread.block * (threadId + 1) - 1;
            this.file = downloadingThread.file;
            this.url = downloadingThread.url;
            this.latch = latch;
            filePath = downloadingThread.file.getAbsolutePath();
            this.fileSize = downloadingThread.fileSize;
            mDownloadingTaskMap.put(threadId, downloadingThread);
            this.latch = latch;
        }

        public void run() {
            HttpURLConnection conn = null;
            InputStream inputStream = null;
            int partiionLength = 0;
            try {
                //获取连接并设置相关属性。
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(HttpRequestSpider.METHOD_GET);
                conn.setReadTimeout(HttpRequestSpider.CONNECTION_TIMEOUT);
                //此步骤是关键。
                conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    RandomAccessFile raf = new RandomAccessFile(file, "rw");
                    //移动指针至该线程负责写入数据的位置。
                    raf.seek(start);
                    //读取数据并写入
                    inputStream = conn.getInputStream();
                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = inputStream.read(b)) != -1) {
                        if (mInternalErrorInterupted.get()) {
                            break;
                        }

                        partiionLength += len;
                        mReadBytesCount += len;
                        if (mCallback != null) {
                            mCallback.onProgress(filePath, (int) (1 + 100 * (mReadBytesCount * 1.0f / fileSize)));
                        }
                        raf.write(b, 0, len);
                    }
                    LogUtil.e("download", "线程:" + threadId + ":" + partiionLength + ":download success");
                }
                mDownloadingTaskMap.remove(threadId);
            } catch (IOException e) {
                LogUtil.e("download", "线程:" + threadId + ":download failed。error=" + e);
                e.printStackTrace();
                mReadBytesCount -= partiionLength;
            } finally {
                latch.countDown();
                if (conn != null) {
                    conn.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public interface IPowerfulDownloadCallback {
        void onStart(String path);

        void onFinish(int statusCode, String path);

        void onError(int errorCode);

        void onProgress(String path, final int progress);
    }


    private class DownloadingThread {
        public int threadId;
        public int fileSize;
        public int start;
        public int end;
        public int block;
        public File file;
        public URL url;
        public boolean result;
    }
}