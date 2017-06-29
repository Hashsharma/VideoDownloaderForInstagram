package com.zxmark.videodownloader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.renderscript.ScriptIntrinsicYuvToRGB;

import com.zxmark.videodownloader.MainApplication;

/**
 * Created by fanlitao on 6/18/17.
 */

public class PreferenceUtils {


    private static SharedPreferences mMainSharedPreference;

    public static final String PREFERNCE_FILE_NAME = "shared_pfs";
    public static final String KEY_LANGUAGE_PRFS = "KEY_LANGUAGE";
    public static final String KEY_LANGUAGE_POSITION = "KEY_LANGUAGE_POSTION";
    public static final String FIRST_LAUNCH = "first_launch";
    public static final String SHOW_RATE_GUIDE = "show_rate_guide";

    public static final String RATE_US_BAD = "rate_us_bad";

    private static SharedPreferences getSharedPreferences() {
        if (mMainSharedPreference == null) {
            Context context = MainApplication.getInstance();
            mMainSharedPreference = context.getSharedPreferences(PREFERNCE_FILE_NAME, Context.MODE_MULTI_PROCESS);
        }
        return mMainSharedPreference;
    }

    public static void saveCurrentLanguage(String ccd, int position) {
        getSharedPreferences();
        mMainSharedPreference.edit().putString(KEY_LANGUAGE_PRFS, ccd).commit();
        mMainSharedPreference.edit().putInt(KEY_LANGUAGE_POSITION, position).commit();
    }

    public static int getCurrentLanguagePos() {
        getSharedPreferences();
        return mMainSharedPreference.getInt(KEY_LANGUAGE_POSITION, 0);
    }

    public static String getCurrentLanguage() {
        getSharedPreferences();
        return mMainSharedPreference.getString(KEY_LANGUAGE_PRFS, "");
    }

    public static boolean isShowedHowToInfo() {
        getSharedPreferences();
        boolean result = mMainSharedPreference.getBoolean(FIRST_LAUNCH, false);
        return result;
    }

    public static void showedHowToInfo() {
        getSharedPreferences();
        mMainSharedPreference.edit().putBoolean(FIRST_LAUNCH,true).commit();
    }

    public static void rateUsOnGooglePlay() {
        getSharedPreferences();
        mMainSharedPreference.edit().putBoolean(SHOW_RATE_GUIDE,true).commit();
    }

    public static boolean isRateUsOnGooglePlay() {
        getSharedPreferences();
        return mMainSharedPreference.getBoolean(SHOW_RATE_GUIDE,false);
    }

    public static void rateUsBad() {
        getSharedPreferences();
        mMainSharedPreference.edit().putLong(RATE_US_BAD,System.currentTimeMillis()).commit();
    }

    public static long getRateUsBadTimeStamp() {
        getSharedPreferences();
        return mMainSharedPreference.getLong(RATE_US_BAD,0);
    }
}
