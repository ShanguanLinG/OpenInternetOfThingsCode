package org.shanguanling.project;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesManager {
    private static String APP_PRED = "app_pred";
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(APP_PRED, Context.MODE_PRIVATE);
        }
    }

    public static void putSet(String key, HashSet<String> set) {
        sharedPreferences.edit().putStringSet(key, set).apply();
    }

    public static Set<String> getSets(String key, Set<String> defaultSet) {
        return sharedPreferences.getStringSet(key, defaultSet);
    }

    public static void removeSets(String key) {
        sharedPreferences.edit().remove(key).apply();
    }
}
