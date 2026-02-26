package org.shanguanling.project;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * 物联网应用开发赛题第4套 子任务2-5 商品查询系统开发
     *
     * @author ShanguanLinG
     * 创建时间 2025/12/16
     * 最后一次修改 2025/12/19
     */

    // 声明需要操作的视图
    EditText codeView;
    TextView nameView;
    TextView priceView;
    // 存储数据
    Information[] informations = new Information[]{
            new Information("E2 80 68 94 00 00 50 31 A1 C2 D5 36", "华为mate20", 5999),
            new Information("E2 80 68 94 00 00 40 31 A1 C2 D1 36", "IPoneXS", 2299),
            new Information("E2 80 68 94 00 00 50 31 A1 C2 CD 36", "小米Mix3", 7699)
    };
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化视图
        initViews();
        // 初始化语音播报模块
        initTTS();
        codeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // 如果按下了Enter键
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (codeView.getText() == null || "".contentEquals(codeView.getText())) {
                        // 如果文字为空则提前返回，避免执行之后的语句
                        return true;
                    }
                    // 识别到的有效文字
                    String s = codeView.getText().toString();
                    // 查找EPC（超高频标签卡号）
                    Information infoByCode = findInfoByCode(s);
                    // 如果没有找到
                    if (infoByCode == null) {
                        // 提示没有查询到相关信息
                        Toast.makeText(MainActivity.this, "未查询到相关信息！", Toast.LENGTH_SHORT).show();
                        // 清空输入框
                        codeView.setText("");
                        // 提前返回
                        return true;
                    }
                    // 查询到了相关信息
                    // 设置nameView为查询到的商品名称
                    nameView.setText(infoByCode.getName());
                    // 设置priceView为查询到的商品价格
                    priceView.setText(infoByCode.getPrice() + "元");
                    // 提示查询到相关商品信息
                    Toast.makeText(MainActivity.this, "已查询到相关商品信息。", Toast.LENGTH_SHORT).show();
                    // 构建字符串，之后要进行语音播报
                    String stringNeedToTalk = "商品名称：" + infoByCode.getName() + "，商品价格:" + infoByCode.getPrice() + "元";
                    // 语音播报文字
                    textToSpeech.speak(stringNeedToTalk, TextToSpeech.QUEUE_FLUSH, null, null);
                    // 清空输入框
                    codeView.setText("");
                }
                return true;
            }
        });
    }

    private Information findInfoByCode(String code) {
        // 通过电子标签生成的EPC（超高频标签卡号）查找存储中的数据是否有对应的，有则返回这个对象
        for (Information information : informations) {
            if (code.equals(information.getCode())) return information;
        }
        // 如果没有找到则返回null
        return null;
    }

    private void initViews() {
        // 初始化视图
        codeView = findViewById(R.id.codeView);
        nameView = findViewById(R.id.nameView);
        priceView = findViewById(R.id.priceView);
    }

    private void initTTS() {
        // 初始化语音播报模块
        textToSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                // 回调函数，如果初始化成功的话会调用此处的方法
                // 可以传入日志以监测是否初始化成功
            }
        });
    }
}
