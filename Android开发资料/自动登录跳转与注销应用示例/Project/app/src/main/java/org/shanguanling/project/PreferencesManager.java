package org.shanguanling.project;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    /**
     * 简单数据存储类管理器
     * 封装了一些基础静态方法使其可以更加方便操控数据
     *
     * @author ShanguanLinG
     * @since 2025/12/26
     */

    // 声明一个常量，代表app配置文件名
    private static final String APP_PREF = "app_pref";
    // 声明一个简单数据存储类
    private static SharedPreferences sp;

    /**
     * 初始化简单数据存储类，并创建配置文件
     *
     * @param context 应用环境信息
     */
    public static void init(Context context) {
        if (sp == null) sp = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
    }

    /**
     * 存放一个数据
     *
     * @param key   需要存储的数据键
     * @param value 需要存储的数据值
     */
    public static void putBoolean(String key, Boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    /**
     * 获取一个数据
     *
     * @param key          需要获取的数据键
     * @param devalueValue 如果获取不到的默认数据值
     * @return 数据值
     */
    public static boolean getBoolean(String key, Boolean devalueValue) {
        return sp.getBoolean(key, devalueValue);
    }

    /**
     * 移除一个数据
     *
     * @param key 需要移除的数据键
     */
    public static void removeBoolean(String key) {
        sp.edit().remove(key).apply();
    }

}
