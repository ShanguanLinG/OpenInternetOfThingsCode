package org.shanguanling.project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button backButton;
    Button logoutButton;
    private static String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 设置当前界面的标题栏为“主界面”
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("主界面");
        // 初始化视图
        initViews();
        // 初始化轻量存储管理器类
        initSP();
        // 初始化监听器
        initListeners();
    }


    /**
     * 初始化视图
     */
    private void initViews() {
        backButton = findViewById(R.id.backButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        // 设置返回按钮被点击时触发的事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 直接进行跳转并结束当前应用
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // 设置注销按钮被点击时触发的事件
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 进行跳转并结束当前应用，同时移除存储的用户登录状态
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                PreferencesManager.removeBoolean(KEY_IS_LOGGED_IN);
                finish();
            }
        });
    }

    /**
     * 初始化数据存储管理器类
     */
    private void initSP() {
        PreferencesManager.init(MainActivity.this);
    }
}
