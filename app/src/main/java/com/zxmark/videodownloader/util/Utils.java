package com.zxmark.videodownloader.util;

import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

import com.zxmark.videodownloader.MainActivity;
import com.zxmark.videodownloader.MainApplication;
import com.zxmark.videodownloader.R;

import java.util.concurrent.CopyOnWriteArrayList;

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


    public static void openAppByPackageName(String packageName) {
        Intent intent = MainApplication.getInstance().getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.getInstance().getApplicationContext().startActivity(intent);
    }
    public static void openInstagram() {
        openAppByPackageName("com.instagram.android");
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

    public static void launchMySelf() {
        Intent intent = new Intent(MainApplication.getInstance().getApplicationContext(), MainActivity.class);
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainApplication.getInstance().getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
//            startActivity(new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("http://instagram.com/xxx")));
        }
    }

    public static void copyText2Clipboard(String content) {
        final Context context = MainApplication.getInstance().getApplicationContext();
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    public static void sendMyApp() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,MainApplication.getInstance().getResources().getString(R.string.app_name) + " is very easy tool for downloading ins videos \n https://play.google.com/store/apps/details?id=com.zxmark.videodownloader");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent sendIntent = Intent.createChooser(sharingIntent,"Share using");
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.getInstance().getApplicationContext().startActivity(sendIntent);
    }
}
