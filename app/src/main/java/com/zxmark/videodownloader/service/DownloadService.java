package com.zxmark.videodownloader.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.zxmark.videodownloader.util.DownloadUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fanlitao on 17/6/7.
 */

public class DownloadService extends IntentService {


    public static final String DIR = "ins_downloader";
    public static final String DOWNLOAD_ACTION = "download_action";
    public static final String DOWNLOAD_URL = "download_url";

    public static final int MSG_DOWNLOAD_SUCCESS = 0;
    public static final int MSG_DOWNLOAD_ERROR = 1;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DOWNLOAD_SUCCESS) {
                Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
            } else if (msg.what == MSG_DOWNLOAD_ERROR) {
                Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DownloadService() {
        super("download video...");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (DOWNLOAD_ACTION.equals(intent.getAction())) {
            String url = intent.getStringExtra(DOWNLOAD_URL);
            boolean result = startDownload(url);
            Log.v("fan", "startDownload.Resut:" + result);
            mHandler.obtainMessage(result ? MSG_DOWNLOAD_SUCCESS : MSG_DOWNLOAD_ERROR).sendToTarget();
        }
    }


    private boolean startDownload(String fileUrl) {
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
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.v("download", "request.responseCode is not ok");
                return false;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            targetPath = getDownloadTargetInfo(fileUrl);
            output = new FileOutputStream(targetPath);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                total += count;
                // publishing the progress....
//                if (fileLength > 0) // only if total length is known
//                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("download", "exception:" + e.getMessage());
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
                return false;
            }

            if (connection != null)
                connection.disconnect();
        }

        return true;
    }

    private String getFileNameByUrl(String url) {
        final int lastIndex = url.lastIndexOf("/");
        return url.substring(lastIndex);
    }

    private String getDownloadTargetInfo(String url) {

        File targetDir = DownloadUtil.getHomeDirectory();

        if (targetDir.exists()) {
            return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
        }

        targetDir.mkdir();
        return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
    }
}
