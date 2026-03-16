package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    /**
     * @author ShanguanLinG
     * @since 2026/01/12
     * nle_mylibrary包控制网关设备与获取网关设备数据示例
     *
     *
     * 需要保证平板的IP配置正确，且AndroidManifest.xml中写了
     * <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Loader.load()
    }
}