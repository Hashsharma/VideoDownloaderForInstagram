package com.zxmark.videodownloader.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.zxmark.videodownloader.MainApplication;

import java.io.File;

/**
 * Created by fanlitao on 17/4/21.
 */

public class ShareActionUtil {

    public static final String PKG_FACEBOOK = "com.facebook.katana";
    public static final String PKG_TWITTER = "com.twitter.android";
    public static final String PKG_INSTAGRAM = "com.instagram.android";
    static String type = "image/*";
    static String filename = "/myPhoto.jpg";


    public static void startInstagramShare(Context activity, String imageUrl) {
        ShareActionUtil util = new ShareActionUtil();
        util.createInstagramIntent(activity, type, imageUrl);
    }

    private void createInstagramIntent(Context activity, String type, String mediaPath) {
        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage(PKG_INSTAGRAM);
        // Set the MIME type
        String mimeType = MimeTypeUtil.getMimeTypeByFileName(mediaPath);
        if(mimeType == null) {
            mimeType = type;
        }
        share.setType(mimeType);
        LogUtil.e("fan","mimeType:" + mimeType);
        // Create the URI from the media
        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        // activity.startActivity(Intent.createChooser(share, "Share to"));
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(share);
    }


    public static void startTwitterShare(Context context, String imageUrl) {
        try {

            LogUtil.v("twitter", "share:" + imageUrl);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(type);
            intent.setPackage(PKG_TWITTER);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imageUrl)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            LogUtil.v("twitter", "e=" + e.getLocalizedMessage());
        }
    }


    public static void startFacebookShare(Context context, String imageUrl) {
        try {

            LogUtil.v("facebook", "share:" + imageUrl);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(type);
            intent.setPackage(PKG_FACEBOOK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imageUrl)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            LogUtil.v("twitter", "e=" + e.getLocalizedMessage());
        }
    }

    public static void shareText(String packageName, String shareMsg) {
        try {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage(packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainApplication.getInstance().getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

}
