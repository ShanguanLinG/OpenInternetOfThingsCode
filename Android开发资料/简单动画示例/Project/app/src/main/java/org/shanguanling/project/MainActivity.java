package org.shanguanling.project;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.BitSet;

public class MainActivity extends AppCompatActivity {

    /**
     * 简单动画示例
     * 请参阅：frame_animation.xml
     *
     * @author ShanguanLinG
     * @since 2025/12/29
     */

    ImageView animationView;
    Button button;
    boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        animationView = findViewById(R.id.animationView);
        button = findViewById(R.id.button);
    }

    /**
     * 初始化监听器，实现按钮点击与播放动画逻辑
     */
    private void initListeners() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取ImageView的Drawable对象
                Drawable drawable = animationView.getDrawable();
                // 如果ImageView的Drawable对象是AnimationDrawable（动画类型）
                if (drawable instanceof AnimationDrawable) {
                    // 那么进行安全转换（先确认类型再进行强制转换），将Drawable转化为AnimationDrawable
                    AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                    // 如果正在播放
                    if (playing) {
                        // 将播放状态设置为停止
                        playing = false;
                        // 将文字设置为停止时的字样
                        button.setText("开始转动");
                        // 停止动画播放
                        animationDrawable.stop();
                    } else {
                        // 否则将播放状态设置为正在播放
                        playing = true;
                        // 将文字设置为播放时的字样
                        button.setText("停止转动");
                        // 开始动画播放
                        animationDrawable.start();
                    }
                }
            }
        });
    }
}
