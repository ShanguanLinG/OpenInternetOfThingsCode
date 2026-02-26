package org.shanguanling.project;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * 一个弹窗与背景遮罩示例应用
     * 基于简单登录应用改写
     *
     * @author ShanguanLinG
     * @since 2025/12/26
     */

    Button showButton;
    Button logoutButton;
    EditText accountText;
    EditText passwordText;
    Button signInButton;
    TextView userNameText;
    Dialog dialog;
    boolean logged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化视图
        initViews();
        // 初始化监听器
        initListeners();
        // 初始化Dialog
        initDialog();
        // 初始化Dialog中的视图
        initDialogViews();
        // 初始化Dialog中的监听器
        initDialogListeners();
    }

    private void initViews() {
        showButton = findViewById(R.id.showButton);
        logoutButton = findViewById(R.id.logoutButton);
        userNameText = findViewById(R.id.userNameText);
    }

    /**
     * 为部分组件设置监听器
     */
    private void initListeners() {
        // 设置登录按钮被点击时触发的事件
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果已登录，则提示
                if (logged) {
                    Toast.makeText(MainActivity.this, "你已经登录过了！", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 否则弹出登录窗口
                dialog.show();
            }
        });
        // 设置退出登录按钮被点击时触发的事件
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果已登录
                if (logged) {
                    // 将用户的登录状态改为false
                    logged = false;
                    // 设置主界面用户名文字为”未登录“
                    userNameText.setText("未登录");
                    // 并设置主界面用户名颜色为红色
                    userNameText.setTextColor(Color.RED);
                    Toast.makeText(MainActivity.this, "您已注销，请重新登录。", Toast.LENGTH_SHORT).show();
                } else {
                    // 否则仅提示用户未登录
                    Toast.makeText(MainActivity.this, "你还没有登录！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 创建并初始化Dialog，方便下面的步骤中进行展示或者关闭
     */
    private void initDialog() {
        // 获取LayoutInflater对象，用于将XML布局文件转换为View对象
        View dialogView = getLayoutInflater().inflate(R.layout.activity_dialog, null);
        // 初始化当前Activity的Dialog对象
        dialog = new Dialog(this);
        // 将创建的View对象设置为对话框的内容
        dialog.setContentView(dialogView);
        // 创建一个透明的背景
        ColorDrawable drawable = new ColorDrawable(Color.TRANSPARENT);
        // 获取Dialog关联的Window对象（每个Dialog都有一个对应的Window）
        Window window = dialog.getWindow();
        if (window != null) {
            // 设置Dialog对象的背景为刚才创建的透明背景（移除系统默认的背景）
            window.setBackgroundDrawable(drawable);
        }
        // 不允许通过点击Dialog外部关闭Dialog（默认允许）
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 初始化Dialog中的视图
     */
    private void initDialogViews() {
        accountText = dialog.findViewById(R.id.accountText);
        passwordText = dialog.findViewById(R.id.passwordText);
        signInButton = dialog.findViewById(R.id.signInButton);
    }

    /**
     * 初始化Dialog中的监听器
     */
    private void initDialogListeners() {
        dialog.findViewById(R.id.signInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取账号输入框中的可编辑文本内容
                Editable accountString = accountText.getText();
                // 获取密码输入框中的可编辑文本内容
                Editable passwordString = passwordText.getText();
                // 检查账号或者密码是否为空，如果为空则提前返回（不会执行return之后的代码）并提示用户账号或密码不得为空
                if (textIsNull(accountString) || textIsNull(passwordString)) {
                    Toast.makeText(MainActivity.this, "账号或密码不得为空!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 检查账号或密码是否正确，如果不正确则提前返回（不会执行return之后的代码）并提示用户账号或密码错误
                if (!authenticationSuccessful(accountString, passwordString)) {
                    Toast.makeText(MainActivity.this, "账号或密码错误，请重试。", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 提示用户登录成功
                Toast.makeText(MainActivity.this, "登录成功。", Toast.LENGTH_SHORT).show();
                // 用户登录成功
                logged = true;
                // 关闭Dialog窗口
                dialog.dismiss();
                // 设置主界面的用户名为admin
                userNameText.setText("admin");
                // 将字体颜色改为深灰色
                userNameText.setTextColor(Color.DKGRAY);
            }
        });
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
     * 同时检查账号和密码是否与提前设定好的字符串相匹配，如果匹配则代表登录成功
     *
     * @param accountString  账号名文本
     * @param passwordString 密码文本
     * @return 是否登录成功
     */
    private boolean authenticationSuccessful(CharSequence accountString, CharSequence passwordString) {
        return ("admin".contentEquals(accountString) && "123456".contentEquals(passwordString));
    }
}
