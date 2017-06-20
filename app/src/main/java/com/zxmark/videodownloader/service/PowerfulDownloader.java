package com.zxmark.videodownloader.service;

import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.spider.HttpRequestSpider;
import com.zxmark.videodownloader.util.CpuUtils;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * Created by fanlitao on 17/6/9.
 * <p>
 * 支持多线程同时下载同一个文件，加快文件的下载速度
 */

public class PowerfulDownloader {


    public static final int CODE_OK = 0;
    public static final int CODE_DOWNLOAD_FAILED = -1;
    public volatile static PowerfulDownloader sInstance;

    private static volatile boolean sIntrupted = false;

    private Object mSyncedLocked = new Object();

    private volatile boolean mMutilThreadDownloading = false;

    private boolean mSyncedLockedWaiting = false;

    public int THREAD_COUNT = 5;

    private volatile int mReadBytesCount = 0;
    private IPowerfulDownloadCallback mCallback;

    private PowerfulDownloader() {
        int cpuCount = CpuUtils.getNumberOfCPUCores() - 1;
        if (cpuCount < 4) {
            cpuCount = 4;
        }
        THREAD_COUNT = cpuCount;
    }


    public static PowerfulDownloader getDefault() {
        synchronized (PowerfulDownloader.class) {
            if (sInstance == null) {
                sInstance = new PowerfulDownloader();
            }
        }
        return sInstance;
    }


    public void interupted() {
        sIntrupted = true;
    }

    public void startDownload(String fileUrl, IPowerfulDownloadCallback callback) {
        mCallback = callback;
        sIntrupted = false;
        LogUtil.e("download", "startDownload:" + THREAD_COUNT);
        download(fileUrl, DownloadUtil.getDownloadTargetInfo(fileUrl), THREAD_COUNT);
    }

    /**
     * 下载文件
     */
    private void download(String fileUrl, String targetPath, int threadNum) {
        int codeStatus = CODE_OK;
        CountDownLatch latch = null;
        if (threadNum > 1) {
            latch = new CountDownLatch(threadNum);
        }
        mReadBytesCount = 0;
        HttpURLConnection conn = null;
        int targetFileSize = 0;
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
                if (fileSize <= 0) {
                    if (mCallback != null) {
                        mCallback.onFinish(-2, targetPath);
                    }
                    return;
                }
                LogUtil.e("fileSize", "fileUrl=" + fileUrl + ":" + fileSize);
                targetFileSize = fileSize;
                //得到文件名
                //根据文件大小及文件名，创建一个同样大小，同样文件名的文件
                if (threadNum == 1) {
                    InputStream fis = conn.getInputStream();
                    // int i = httpUrlConnection.getContentLength();
                    int i = 1024;
                    byte[] buffer = new byte[i];
                    // fis.read(b);
                    int byteCount;
                    File targetFile = new File(targetPath);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    byte[] temp;
                    while ((byteCount = fis.read(buffer)) != -1) {
                        // 重点注意这样的写法
                        fos.write(buffer, 0, byteCount);
                        fos.flush();
                        mReadBytesCount += byteCount;
                        if (mCallback != null) {
                            mCallback.onProgress(targetPath, (int) (1 + 100 * (mReadBytesCount * 1.0f / fileSize)));
                        }
                    }
                    fis.close();
                    fos.close();
                } else {
                    File file = new File(targetPath);
                    RandomAccessFile raf = new RandomAccessFile(file, "rw");
                    raf.setLength(fileSize);
                    raf.close();
                    // 将文件分成threadNum = 5份。
                    int block = fileSize % threadNum == 0 ? fileSize / threadNum
                            : fileSize / threadNum + 1;
                    LogUtil.e("download", "block:" + block);
                    for (int threadId = 0; threadId < threadNum; threadId++) {
                        //传入线程编号，并开始下载。
                        //executorService.execute(new DownloadPartialFileRunnable(threadId, fileSize, block, file, url, latch));
                        new DownloadThread(threadId, fileSize, block, file, url, latch).start();

                    }
                    LogUtil.e("download", "wait all downloading thread");
                    if (latch != null) {
                        latch.await();
                    }
                }

                mReadBytesCount = 0;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        if (mCallback != null) {
            if (new File(targetPath).length() != targetFileSize) {
                codeStatus = CODE_DOWNLOAD_FAILED;
                //TODO:经过多线程下载后发现下载大小和服务器返回不一致,用单线程retry一次，保证下载成功率
                if (threadNum > 1) {
                    download(fileUrl, targetPath, 1);
                }


            }
            LogUtil.e("fileSize", "fileUrl=" + new File(targetPath).length() + ":" + targetFileSize);
            mCallback.onFinish(codeStatus, targetPath);
        }

        LogUtil.e("download", "all thread executed finished");
    }

    class DownloadPartialFileRunnable implements Runnable {

        int start, end, threadId;
        File file = null;
        URL url = null;
        CountDownLatch latch;
        String filePath;
        int fileSize;

        public DownloadPartialFileRunnable(int threadId, int fileSize, int block, File file, URL url, CountDownLatch latch) {
            this.threadId = threadId;
            start = block * threadId;
            end = block * (threadId + 1) - 1;
            this.file = file;
            this.url = url;
            this.latch = latch;
            filePath = file.getAbsolutePath();
            this.fileSize = fileSize;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            InputStream inputStream = null;
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
                    int lengthPlus = 0;
                    while ((len = inputStream.read(b)) != -1) {
                        if (sIntrupted) {
                            break;
                        }
                        lengthPlus += len;
                        mReadBytesCount += len;
                        if (mCallback != null) {
                            mCallback.onProgress(filePath, (int) (1 + 100 * (mReadBytesCount * 1.0f / fileSize)));
                        }
                        raf.write(b, 0, len);
                    }
                    LogUtil.e("download", "线程:" + threadId + ":" + lengthPlus + ":下载完毕");
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        }

        public void run() {
            HttpURLConnection conn = null;
            InputStream inputStream = null;
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
                    int lengthPlus = 0;
                    while ((len = inputStream.read(b)) != -1) {
                        if (sIntrupted) {
                            break;
                        }
                        lengthPlus += len;
                        mReadBytesCount += len;
                        if (mCallback != null) {
                            mCallback.onProgress(filePath, (int) (1 + 100 * (mReadBytesCount * 1.0f / fileSize)));
                        }
                        raf.write(b, 0, len);
                    }
                    LogUtil.e("download", "线程:" + threadId + ":" + lengthPlus + ":下载完毕");
                }
            } catch (IOException e) {
                e.printStackTrace();
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
}