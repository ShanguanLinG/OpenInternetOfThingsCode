package org.shanguanling.project;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tplink.sdk.tpopensdk.TPOpenSDK;
import com.tplink.sdk.tpopensdk.TPPlayer;
import com.tplink.sdk.tpopensdk.TPSDKContext;
import com.tplink.sdk.tpopensdk.common.TPSDKCommon;
import com.tplink.sdk.tpopensdk.openctx.IPCDevice;
import com.tplink.sdk.tpopensdk.openctx.IPCDeviceContext;
import com.tplink.sdk.tpopensdk.openctx.IPCReqListener;
import com.tplink.sdk.tpopensdk.openctx.IPCReqResponse;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.CloudService;
import cn.com.newland.nle_sdk.util.CloudServiceListener;

public class MainActivity extends AppCompatActivity {

    /**
     * 物联网应用开发赛题第1套 子任务2-4 远程监控应用开发
     * 暂时还未实现定时开关路灯功能，未来会添加
     *
     * @author: ShanguanLinG
     * 创建时间 2025/12/19 16:06
     * 最后一次修改 2025/12/20 15:20
     */

    FrameLayout monitor_layout;
    LinearLayout auto_settings_layout;
    ImageView monitor_switch_image;
    ImageView lamp_image;
    ImageView lamp_switch_image;
    ImageView up_image;
    ImageView down_image;
    ImageView left_image;
    ImageView right_image;
    TPSDKContext sdkContext;
    TPPlayer player;
    IPCDeviceContext devCtx;
    IPCDevice ipcDevice;
    boolean openedMonitor = false;
    boolean openedLamp = false;
    CloudService cloudService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化视图
        initViews();
        // 初始化TPOpenSDK
        initTPOpenSDK();
        // 注册设置监听器
        registerListener();
        // 初始化云平台
        cloudService = new CloudService("http://192.168.0.138/");
        // 云平台登录
        cloudService.signIn(new SignIn("18912345619", "18912345619"), new CloudServiceListener<BaseResponseEntity<User>>() {
            @Override
            protected void onResponse(BaseResponseEntity<User> userBaseResponseEntity) {
                // 回调函数，如果触发了代表上面的代码执行成功了
                // 在登录成功时设置AccessToken
                cloudService.setAccessToken(userBaseResponseEntity.getResultObj().getAccessToken());
            }
        });
    }

    private void initViews() {
        // 初始化视图方法
        monitor_layout = findViewById(R.id.monitor_layout);
        auto_settings_layout = findViewById(R.id.auto_settings_layout);
        monitor_switch_image = findViewById(R.id.monitor_switch_image);
        lamp_image = findViewById(R.id.lamp_image);
        lamp_switch_image = findViewById(R.id.lamp_switch_image);
        up_image = findViewById(R.id.up_image);
        down_image = findViewById(R.id.down_image);
        left_image = findViewById(R.id.left_image);
        right_image = findViewById(R.id.right_image);
    }

    private void initTPOpenSDK() {
        // 初始化TPOpenSDK方法
        // 创建TPOpenSDK的实例和SDKContext的实例，均为单例模式
        sdkContext = TPOpenSDK.getInstance().getSDKContext();
        // 使用SDKContext启动app底层模块，必须在其他语句之前执行，不然会报错，并且报错几乎无法定位与溯源（jni直接爆炸）
        sdkContext.appReqStart(true, null);
        // 创建一个视频播放器对象，用于后面播放画面
        player = TPOpenSDK.getInstance().createPlayer(MainActivity.this);
        // 获取设备模块上下文对象，同样是单例模式
        devCtx = sdkContext.getDevCtx();
        // 从设备模块上下文对象中登录，获取地址指针
        long pointer = devCtx.initDev("172.18.19.13", 80);
        // 创建一个IPCDevice对象，传入地址指针
        ipcDevice = new IPCDevice(pointer);
        // 让设备模块访问地址并使用密码尝试进行登录
        devCtx.reqLogin(ipcDevice, "12345678a", new IPCReqListener() {
            // 回调函数，如果触发了代表上面的代码执行成功了
            @Override
            public int callBack(IPCReqResponse ipcReqResponse) {
                // 尝试进行连接
                devCtx.reqConnectDev(ipcDevice, new IPCReqListener() {
                    // 回调函数，如果触发了代表上面的代码执行成功了
                    @Override
                    public int callBack(IPCReqResponse ipcReqResponse) {
                        // 让设备模块获取用于播放的设备端口号
                        devCtx.reqGetVideoPort(ipcDevice, new IPCReqListener() {
                            @Override
                            public int callBack(IPCReqResponse ipcReqResponse) {
                                // 回调函数，如果触发了代表上面的代码执行成功了，
                                // 代码运行到此说明所有初始化操作都成功了，提示连接成功。
                                System.out.println("连接成功。");
                                return 0;
                            }
                        });
                        return 0;
                    }
                });
                return 0;
            }
        });
    }

    // 镇压警告注解，仅屏蔽编译器警告
    @SuppressLint("ClickableViewAccessibility")
    // 注册监听器
    private void registerListener() {
        // 设置监控按钮的监听器
        monitor_switch_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果监控处于打开状态时（被点击了）
                if (openedMonitor) {
                    // 进行关闭监控流程
                    // 隐藏方向面板（上下左右键）
                    hideDirectionPanel();
                    // 设置打开关闭监控按钮的图片显示为开启状态
                    // （当前监控处于关闭状态，按钮状态为开启，这个按钮是用来提示用户点击开启按钮时可以开启监控，并不是代表监控的状态，注意不要混淆）
                    monitor_switch_image.setImageResource(R.drawable.btn_start_normal);
                    // 显示路灯控制按钮的图片，代表此时可控制
                    lamp_switch_image.setVisibility(View.VISIBLE);
                    // 隐藏自动设置布局
                    auto_settings_layout.setVisibility(View.GONE);
                    // 移除所有视图
                    monitor_layout.removeAllViews();
                    // 摄像头静音
                    player.turnOffSound();
                    // 停止播放
                    player.stop();
                    // 关闭路灯
                    closeLamp();
                    // 改变监控状态变量为false
                    openedMonitor = false;
                } else {
                    // 如果监控不处于开启状态时，即关闭状态时（被点击了）
                    // 进行打开监控流程
                    // 显示方向面板（上下左右键）
                    showDirectionPanel();
                    // 设置打开关闭监控按钮的图片显示为关闭状态
                    // （当前监控处于开启状态，按钮状态为关闭，这个按钮是用来提示用户点击关闭按钮时可以关闭监控，并不是代表监控的状态，注意不要混淆）
                    monitor_switch_image.setImageResource(R.drawable.btn_closed_normal);
                    // 隐藏路灯控制按钮的图片，代表此时不可控制
                    lamp_switch_image.setVisibility(View.GONE);
                    // 显示自动设置布局
                    auto_settings_layout.setVisibility(View.VISIBLE);
                    // 设置视频播放器对象应该在哪个布局中播放监控画面（此处为monitor_layout）
                    player.setViewHolder(monitor_layout);
                    // 让视频播放器对象开始预览，清晰度为清晰（QUALITY_CLEAR）
                    player.startRealPlay(ipcDevice, TPSDKCommon.Quality.QUALITY_CLEAR);
                    // 改变监控状态变量为true
                    openedMonitor = true;
                }

            }
        });
        // 设置路灯按钮的监听器
        lamp_switch_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果路灯开启时（按下按钮）
                if (openedLamp) {
                    // 关闭路灯
                    closeLamp();
                } else {
                    // 否则开启路灯
                    openLamp();
                }
            }
        });
        up_image.setOnTouchListener(new View.OnTouchListener() {
            // 设置向上按钮的监听器
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // 如果当前监听器的动作为按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 移动电机（摄像头）相对位置（角度为90，代表上）
                    devCtx.reqMotorMoveStep(ipcDevice, null, 90, -1);
                    return true;
                }
                // 如果当前监听器的动作为抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 停止电机转动
                    devCtx.reqMotorMoveStop(ipcDevice, null, -1);
                    return true;
                }
                return false;
            }
        });
        down_image.setOnTouchListener(new View.OnTouchListener() {
            // 设置向下按钮的监听器
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 移动电机（摄像头）相对位置（角度为270，代表下）
                    devCtx.reqMotorMoveStep(ipcDevice, null, 270, -1);
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 停止电机转动
                    devCtx.reqMotorMoveStop(ipcDevice, null, -1);
                    return true;
                }
                return false;
            }
        });
        left_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 移动电机（摄像头）相对位置（角度为270，代表左）
                    devCtx.reqMotorMoveStep(ipcDevice, null, 180, -1);
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 停止电机转动
                    devCtx.reqMotorMoveStop(ipcDevice, null, -1);
                    return true;
                }
                return false;
            }
        });
        right_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 移动电机（摄像头）相对位置（角度为0，代表右）
                    devCtx.reqMotorMoveStep(ipcDevice, null, 0, -1);
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 停止电机转动
                    devCtx.reqMotorMoveStop(ipcDevice, null, -1);
                    return true;
                }
                return false;
            }
        });
    }

    private void showDirectionPanel() {
        // 显示方向面板（上下左右键）
        up_image.setVisibility(View.VISIBLE);
        down_image.setVisibility(View.VISIBLE);
        left_image.setVisibility(View.VISIBLE);
        right_image.setVisibility(View.VISIBLE);
    }

    private void hideDirectionPanel() {
        // 隐藏方向面板（上下左右键）
        up_image.setVisibility(View.GONE);
        down_image.setVisibility(View.GONE);
        left_image.setVisibility(View.GONE);
        right_image.setVisibility(View.GONE);
    }

    private void openLamp() {
        // 打开路灯
        // 设置路灯的按钮图片状态为on
        lamp_switch_image.setImageResource(R.drawable.btn_switch_on);
        // 设置路灯图片为关闭状态
        lamp_image.setImageResource(R.drawable.pic_lamp_on);
        // 控制云平台路灯打开
        fastControl(159611, "m_lamp", 1);
        // 设置路灯的状态变量为true，代表已打开
        openedLamp = true;
    }

    private void closeLamp() {
        // 设置路灯的按钮图片状态为off
        lamp_switch_image.setImageResource(R.drawable.btn_switch_off);
        // 设置路灯图片为关闭状态
        lamp_image.setImageResource(R.drawable.pic_lamp_off);
        // 控制云平台路灯关闭
        fastControl(159611, "m_lamp", 0);
        // 设置路灯的状态变量为false，代表已关闭
        openedLamp = false;
    }

    /**
     * 封装云平台的control方法，仅用于发送命令，
     * 忽略了原生命令中的回调函数，因为发送完指令后不需要做任何事情
     *
     * @param deviceId 设备Id
     * @param apiTag   执行器标识名
     * @param data     发送的数据
     */
    private void fastControl(int deviceId, String apiTag, Object data) {
        cloudService.control(deviceId, apiTag, data, new CloudServiceListener<BaseResponseEntity>() {
            @Override
            protected void onResponse(BaseResponseEntity baseResponseEntity) {
            }
        });
    }
}
