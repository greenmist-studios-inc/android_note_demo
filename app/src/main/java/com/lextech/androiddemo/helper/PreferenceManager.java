package com.lextech.androiddemo.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handles setting and retrieving shared preferences
 * User: Brent
 * Date: 7/13/15
 */
public class PreferenceManager {

    public static final String KEY_FLOATING_NOTES = "floating";
    public static final String KEY_FLOATING_X = "x";
    public static final String KEY_FLOATING_Y = "y";

    private static SharedPreferences getPrefs(Context c) {
        return c.getSharedPreferences("androiddemo", Context.MODE_PRIVATE);
    }

    public static void set(Context c, String key, String value) {
        getPrefs(c).edit().putString(key, value).apply();
    }

    public static void set(Context c, String key, float value) {
        getPrefs(c).edit().putFloat(key, value).apply();
    }

    public static void set(Context c, String key, boolean value) {
        getPrefs(c).edit().putBoolean(key, value).apply();
    }

    public static String getStringValue(Context c, String key) {
        return getPrefs(c).getString(key, null);
    }

    public static boolean getBooleanValue(Context c, String key) {
        return getPrefs(c).getBoolean(key, false);
    }

    public static float getFloatValue(Context c, String key) {
        return getPrefs(c).getFloat(key, 0);
    }

    public static boolean containsKey(Context c, String key) {
        return getPrefs(c).contains(key);
    }


}
