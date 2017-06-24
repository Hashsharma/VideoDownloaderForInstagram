package com.zxmark.videodownloader.util;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static boolean isFirstRunMainFragment() {
        getSharedPreferences();
        boolean result = mMainSharedPreference.getBoolean(FIRST_LAUNCH, true);
        mMainSharedPreference.edit().putBoolean(FIRST_LAUNCH, false).commit();
        return result;
    }

    public static void showedRateGuide() {
        getSharedPreferences();
        mMainSharedPreference.edit().putBoolean(SHOW_RATE_GUIDE,true);
    }

    public static boolean isShowedRateGuide() {
        getSharedPreferences();
        return mMainSharedPreference.getBoolean(SHOW_RATE_GUIDE,false);
    }
}
