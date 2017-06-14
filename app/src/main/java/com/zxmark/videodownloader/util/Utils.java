package com.zxmark.videodownloader.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.zxmark.videodownloader.MainApplication;

/**
 * Created by fanlitao on 17/6/13.
 */

public class Utils {


    public static void openInstagramByUrl(String url) {

        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        intent.setPackage("com.instagram.android");

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainApplication.getInstance().getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
//            startActivity(new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("http://instagram.com/xxx")));
        }
    }



    public static void openKuaiShouApp(String url) {

        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        intent.setPackage("com.smile.gifmaker");

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainApplication.getInstance().getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
//            startActivity(new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("http://instagram.com/xxx")));
        }
    }
}
