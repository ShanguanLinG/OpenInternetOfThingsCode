package org.shanguanling.project;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.SensorInfo;
import cn.com.newland.nle_sdk.responseEntity.SensorReaTimeData;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.CloudService;
import cn.com.newland.nle_sdk.util.CloudServiceListener;

public class MainActivity extends AppCompatActivity {


    /**
     * 电动推杆练习（含定时任务）
     * 所需执行器：电动推杆-前进，电动推杆-缩回
     * 所需传感器：行程开关，接近开关
     *
     * @author ShanguanLinG
     * @since 2025/12/29
     */


    CloudService cloudService;
    // 创建定时执行周期性任务类
    Handler handler = new Handler();
    /**
     * 每1秒执行一次updateAll()方法
     * <p>
     * 执行流程：
     * 1. handler.post(autoUpdate) 触发（autoUpdate方法被调用处）
     * 2. run()方法被调用
     * 3. 执行 updateAll()
     * 4. 执行 handler.postDelayed(this, 1000)：
     * - 将当前的Runnable对象（this）再次放入消息队列
     * - 设置1秒后执行
     * 5. 1秒后，消息队列取出这个Runnable再次执行
     * 6. 回到第3步，形成无限循环，直到应用关闭
     * <p>
     * 注：需要先执行任务（调用updateAll()方法），确保第一次任务执行完毕后再安排下一次任务（handler.postDelayed(this, 1000)）
     * 如果先安排下一次任务在一秒后执行，且该任务的耗时超过一秒，会导致任务堆积
     */
    Runnable autoUpdate = new Runnable() {
        @Override
        public void run() {
            // 立刻执行updateAll()方法
            updateAll();
            // 延迟一秒后再次执行自己（传递this，将这个Runnable对象再次放入消息队列）
            handler.postDelayed(this, 1000);

        }
    };
    boolean m_near_detected;
    boolean m_travelSwitch_detected;
    TextView pushrod_putt_status_text;
    TextView m_travelSwitch_status_text;
    TextView m_near_status_text;
    Button putt_pushrod_button;
    Button back_pushrod_button;
    Button automode_button;
    boolean enableAutoMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        cloudService = new CloudService("http://192.168.0.138/");
        cloudService.signIn(new SignIn("18912345635", "123456"), new CloudServiceListener<BaseResponseEntity<User>>() {
            @Override
            protected void onResponse(BaseResponseEntity<User> userBaseResponseEntity) {
                String accessToken = userBaseResponseEntity.getResultObj().getAccessToken();
                System.out.println(accessToken);
                cloudService.setAccessToken(accessToken);
                initListeners();
                // 立刻启动周期循环（autoUpdate方法）
                handler.post(autoUpdate);
            }
        });
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        pushrod_putt_status_text = findViewById(R.id.pushrod_putt_status_text);
        m_travelSwitch_status_text = findViewById(R.id.m_travelSwitch_status_text);
        m_near_status_text = findViewById(R.id.m_near_status_text);
        putt_pushrod_button = findViewById(R.id.putt_pushrod_button);
        back_pushrod_button = findViewById(R.id.back_pushrod_button);
        automode_button = findViewById(R.id.automode_button);
    }

    /**
     * 初始化监听器，实现推出推杆，收回推杆的逻辑实现，与自动模式的状态切换。
     */
    private void initListeners() {
        putt_pushrod_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushRodPush();
            }
        });
        back_pushrod_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushRodBack();
            }
        });
        automode_button.setOnClickListener(new View.OnClickListener() {
            // 切换自动模式的状态，开启和关闭用enableAutoMode这个变量表示
            // 自动模式：当推杆完全伸出时，自动收回；当推杆完全缩回时，自动伸出。
            @Override
            public void onClick(View view) {
                if (!enableAutoMode) {
                    enableAutoMode = true;
                    automode_button.setText("自动模式：开");
                } else {
                    enableAutoMode = false;
                    automode_button.setText("自动模式：关");
                }
            }
        });
    }

    /**
     * 电动推杆拥有两个执行器，一个为伸出一个为缩回；
     * 当伸出触发但缩回未触发时，电动推杆会伸出；
     * 当伸出未触发但缩回触发时，电动推杆会缩回；
     * 如果两个执行器都触发或者都不触发，电动推杆会保持原状。
     * 此方法让电动推杆最终的状态变更为推出，并修改文字描述状态为推出。
     */
    private void pushRodPush() {
        fastControl(162602, "m_pushrod_putt", 1);
        fastControl(162602, "m_pushrod_back", 0);
        pushrod_putt_status_text.setText("推出");
    }

    /**
     * 电动推杆拥有两个执行器，一个为伸出一个为缩回；
     * 当伸出触发但缩回未触发时，电动推杆会伸出；
     * 当伸出未触发但缩回触发时，电动推杆会缩回；
     * 如果两个执行器都触发或者都不触发，电动推杆会保持原状。
     * 此方法让电动推杆最终的状态变更为缩回，并修改文字描述状态为缩回。
     */
    private void pushRodBack() {
        fastControl(162602, "m_pushrod_putt", 0);
        fastControl(162602, "m_pushrod_back", 1);
        pushrod_putt_status_text.setText("缩回");
    }

    /**
     * 从云平台获取接近开关和行程开关的数值，
     * 并将其转化为全局布尔值变量以监测按钮是否被触发
     * 不要使用云平台的getSensor方法，此方法存在非常高的延迟因此不推荐使用
     */
    private void updateSensorValue() {
        // 获取一个项目中所有的传感器的所有数据
        cloudService.getSensorsRealTimeData(136630, new CloudServiceListener<BaseResponseEntity<List<SensorReaTimeData>>>() {
            @Override
            protected void onResponse(BaseResponseEntity<List<SensorReaTimeData>> listBaseResponseEntity) {
                // 获取所有传感器的数据
                List<SensorReaTimeData> sensorReaTimeData = listBaseResponseEntity.getResultObj();
                // 采用增强型for循环遍历所有传感器，找到传感器apiTag为m_near和m_travelSwitch的传感器
                // 并将这两个传感器的值（0或者1）转化为布尔值（代表是否触发）并更新到全局变量中
                for (SensorReaTimeData sensorReaTimeDatum : sensorReaTimeData) {
                    if ("m_near".equals(sensorReaTimeDatum.getApiTag())) {
                        m_near_detected = Integer.parseInt(sensorReaTimeDatum.getValue()) != 0;
                    }
                    if ("m_travelSwitch".equals(sensorReaTimeDatum.getApiTag())) {
                        m_travelSwitch_detected = Integer.parseInt(sensorReaTimeDatum.getValue()) != 0;
                    }
                }
            }
        });
    }

    /**
     * 不要使用云平台的getSensor方法，此方法存在非常高的延迟因此不推荐使用
     */
    @Deprecated
    private void doNotUseThisMethod() {
        cloudService.getSensor(0, "", new CloudServiceListener<BaseResponseEntity<SensorInfo>>() {
            @Override
            protected void onResponse(BaseResponseEntity<SensorInfo> sensorInfoBaseResponseEntity) {
            }
        });
    }

    /**
     * 更新视图
     */
    private void updateViews() {
        // 更新视图
        m_near_status_text.setText(m_near_detected ? "触发" : "未触发");
        m_travelSwitch_status_text.setText(m_travelSwitch_detected ? "触发" : "未触发");
    }

    /**
     * 快速控制方法，因为我们并不需要使用到这里的回调函数，
     * 所以只要让他发一个信号就好了，而不关心他发完之后做什么事情
     */
    private void fastControl(int deviceId, String apiTag, Object data) {
        cloudService.control(deviceId, apiTag, data, new CloudServiceListener<BaseResponseEntity>() {
            @Override
            protected void onResponse(BaseResponseEntity baseResponseEntity) {
            }
        });
    }

    /**
     * 更新所有应该更新的东西
     */
    private void updateAll() {
        updateViews();
        updateSensorValue();
        // 如果没有开启自动模式，则不执行下面的代码
        if (!enableAutoMode) {
            return;
        }
        // 如果接近开关和行程开关都被触发了，证明推杆已经完全伸出，此时需要收回。
        if (m_near_detected && m_travelSwitch_detected) {
            pushRodBack();
            return;
        }
        // 如果接近开关和行程开关都没有被触发，证明推杆已经完全收回，此时需要伸出。
        if (!m_near_detected && !m_travelSwitch_detected) {
            pushRodPush();
        }
    }

    /**
     * 页面关闭时移除回调，避免内存泄露（比赛时可不写）
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoUpdate);
    }
}
