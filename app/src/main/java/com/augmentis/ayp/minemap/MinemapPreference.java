package com.augmentis.ayp.minemap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Apinya on 9/30/2016.
 */

public class MinemapPreference {

    private static final String PREF_SEARCH_KEY = "minemappref";

    public static String getStoredSearchKey(Context context) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_SEARCH_KEY, null);
    }

    public static void setStoredSearchKey(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit()
                .putString(PREF_SEARCH_KEY, key)
                .apply();
    }
}
