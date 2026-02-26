package org.shanguanling.project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * 一个简单的登录应用示例
     *
     * @author ShanguanLinG
     * @since 2025/12/26
     */

    EditText accountText;
    EditText passwordText;
    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化视图
        initViews();
        // 初始化监听器
        initListeners();
    }

    private void initViews() {
        accountText = findViewById(R.id.accountText);
        passwordText = findViewById(R.id.passwordText);
        signInButton = findViewById(R.id.signInButton);
    }

    /**
     * 为部分组件设置监听器
     * 之所以没有设置accountText和passwordText的键盘监听事件是因为在布局中
     * 设置了android:singleLine="true"，代表禁止换行并改变回车键行为
     * 如果不加这个属性用户按下回车会导致换行，加上了这个属性用户按下回车将会进入下一个可输入控件
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
                    Toast.makeText(MainActivity.this, "账号或密码不得为空!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 检查账号或密码是否正确，如果不正确则提前返回（不会执行return之后的代码）并提示用户账号或密码错误
                if (!authenticationSuccessful(accountString, passwordString)) {
                    Toast.makeText(MainActivity.this, "账号或密码错误，请重试。", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 提示用户登录成功，可以在接下来的doSomething()方法中处理登录成功后的操作
                Toast.makeText(MainActivity.this, "登录成功。", Toast.LENGTH_SHORT).show();
                doSomething();
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

    /**
     * 自定义方法，代表登录成功后你要干的事情，想写什么都可以
     */
    private void doSomething() {
    }
}
