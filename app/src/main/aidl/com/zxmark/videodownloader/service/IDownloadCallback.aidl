// IDownloadCallback.aidl
package com.zxmark.videodownloader.service;
// Declare any non-default types here with import statements

interface IDownloadCallback {


    void onStartDownload(String path);
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onPublishProgress(String key,int progress);


    void onDownloadSuccess(String path);
}
