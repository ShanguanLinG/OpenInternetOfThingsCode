package org.shanguanling.project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    /**
     * 登录后跳转，并实现自动登录和注销等功能应用示例
     * 基于简单登录应用示例进行开发
     *
     * @author ShanguanLinG
     * @since 2025/12/26
     */

    EditText accountText;
    EditText passwordText;
    Button signInButton;
    CheckBox rememberBox;
    private static String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        // 设置当前界面的标题栏为“登录界面”
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("登录界面");
        // 初始化视图
        initViews();
        // 初始化轻量存储管理器类
        initSP();
        // 初始化监听器
        initListeners();
        // 如果选择过了一次记住密码并成功登录过一次，那么将本次将触发自动登录
        if (getLoggedInStatus()) {
            // 进行自动登录
            autoLogin();
            // 结束当前界面
            finish();
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        accountText = findViewById(R.id.accountText);
        passwordText = findViewById(R.id.passwordText);
        signInButton = findViewById(R.id.signInButton);
        rememberBox = findViewById(R.id.rememberBox);
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        // 设置登录按钮被点击时触发的事件
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取账号输入框中的可编辑文本内容
                Editable accountString = accountText.getText();
                // 获取密码输入框中的可编辑文本内容
                Editable passwordString = passwordText.getText();
                // 检查账号或者密码是否为空，如果为空则提前返回（不会执行return之后的代码）并提示用户账号或密码不得为空
                if (textIsNull(accountString) || textIsNull(passwordString)) {
                    Toast.makeText(LoginActivity.this, "账号或密码不得为空!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 检查账号或密码是否正确，如果不正确则提前返回（不会执行return之后的代码）并提示用户账号或密码错误
                if (!authenticationSuccessful(accountString, passwordString)) {
                    Toast.makeText(LoginActivity.this, "账号或密码错误，请重试。", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 提示用户登录成功
                Toast.makeText(LoginActivity.this, "登录成功。", Toast.LENGTH_SHORT).show();
                // 当用户登录成功时，如果用户选中了”记住密码“按钮，那么将用户登录成功的状态进行存储
                if (rememberBox.isChecked()) setLoggedInStatus(true);
                // 构建一个Intent类，表示应用意图将要从LoginActivity跳转到MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // 执行跳转
                startActivity(intent);
                // 结束当前界面
                finish();
            }
        });
    }

    /**
     * 初始化数据存储管理器类
     */
    private void initSP() {
        PreferencesManager.init(LoginActivity.this);
    }

    /**
     * 检查 CharSequence 是否为空（null 或空字符串）
     * 使用 contentEquals 而不是 equals，因为参数可能是 Editable 等 CharSequence 实现类
     * contentEquals 能正确处理各种实现类，而 equals 可能返回 false
     *
     * @param text 需要被检查的文本
     * @return 文本为空
     */
    private boolean textIsNull(CharSequence text) {
        if (text == null) return true;
        return "".contentEquals(text);
    }

    /**
     * 检查用户的用户名和密码，并尝试登录
     *
     * @return 用户是否登录成功
     */
    private boolean authenticationSuccessful(CharSequence accountString, CharSequence passwordString) {
        return ("admin".contentEquals(accountString) && "123456".contentEquals(passwordString));
    }

    /**
     * 设置用户的登录状态，
     * 此方法只有在用户选中了”记住密码“选项并登录成功后触发
     */
    private void setLoggedInStatus(boolean isLoggedIn) {
        PreferencesManager.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
    }

    /**
     * 获取存储的值，查询用户的登录状态是否为true
     * defaultValue的值代表如果查询不到则返回false
     *
     * @return 用户的登录状态
     */
    private boolean getLoggedInStatus() {
        // 获取存储的值，查询用户的登录状态是否为true
        // defaultValue的值代表如果查询不到则返回false
        return PreferencesManager.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * 进行自动登录，弹窗提示并切换页面
     */
    private void autoLogin() {
        Toast.makeText(LoginActivity.this, "已为您自动登录。", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
