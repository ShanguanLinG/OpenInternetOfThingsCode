package org.shanguanling.project;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.nle.mylibrary.claimer.connector.ConnectorListener;
import com.nle.mylibrary.databus.DataBus;
import com.nle.mylibrary.databus.DataBusFactory;
import com.nle.mylibrary.device.GenericConnector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 物联网应用开发赛题第6套
 * 子任务2-4 停车场车辆进出管理系统
 * <p>
 * 功能概述：
 * 1. 通过RFID读取车辆信息
 * 2. 控制道闸设备（红绿灯、推杆等）
 * 3. 管理车辆进出状态和计费
 * 4. 使用TTS语音播报提示信息
 * 5. 实时更新UI显示车辆信息
 * <p>
 * 系统架构：
 * - 使用NLE库与网关设备通信
 * - 异步任务处理设备控制
 * - 状态机管理车辆进出流程
 *
 * @author ShanguanLinG
 * @since 2026/01/14
 */
public class MainActivity extends AppCompatActivity {
    // 网关连接器，用于与硬件设备通信
    GenericConnector genericConnector;

    // 文本转语音引擎，用于语音播报提示信息
    TextToSpeech tts;

    // UI控件 - 显示车辆相关信息
    TextView carIdText;      // 显示车牌号
    TextView startTimeText;  // 显示入场时间
    TextView endTimeText;    // 显示出场时间
    TextView timeText;       // 显示停车时长
    TextView priceText;      // 显示停车费用

    // 车辆信息列表，存储所有已注册车辆的信息
    List<CarIdInfo> carIdInfoList = new ArrayList<>();

    // 确认按钮，用于触发车辆出场流程
    Button confirmButton;

    // 主线程处理器，用于在UI线程执行任务
    Handler handler = new Handler();

    // 网关数据缓存，存储从硬件设备读取的数据
    GateWayResultData gateWayResultData = new GateWayResultData();

    // 当前读取到的RFID标签
    String rfid;

    // 行程开关触发状态（用于检测推杆位置）
    boolean travelSwitchTrigger;

    // 接近开关触发状态（用于检测车辆位置）
    boolean nearSwitchTrigger;

    // 日志标签
    final String TAG = "停车场车辆进出管理系统";


    /**
     * Activity创建时的初始化方法
     * <p>
     * 初始化流程：
     * 1. 调用父类onCreate方法
     * 2. 设置布局文件
     * 3. 初始化网关连接器（连接硬件设备）
     * 4. 初始化UI控件引用
     * 5. 初始化事件监听器
     * 6. 初始化数据（添加示例车辆信息）
     * 7. 初始化TTS语音引擎
     *
     * @param savedInstanceState 保存的状态数据，用于Activity恢复
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGateWayConnector();  // 连接硬件网关
        initViews();             // 初始化UI控件
        initListeners();         // 设置事件监听
        initDatas();             // 初始化数据
        initTTS();               // 初始化语音引擎
    }

    /**
     * 初始化车辆数据
     * <p>
     * 功能：向车辆信息列表中添加示例车辆数据
     * 在实际应用中，这里应该从数据库或网络加载车辆信息
     * <p>
     * 示例车辆信息：
     * - RFID标签：E2 00 47 0F B9 60 64 26 A7 22 01 0C
     * - 车牌号：京A 234567
     * - 初始状态：0（CarState.None）
     * - 初始价格：0元
     */
    private void initDatas() {
        carIdInfoList.add(new CarIdInfo(
                "E2 00 47 0F B9 60 64 26 A7 22 01 0C",  // RFID标签
                "京A 234567",                            // 车牌号
                0,                                       // 初始状态
                0                                        // 初始价格
        ));
    }

    /**
     * 初始化UI控件
     * <p>
     * 功能：通过findViewById获取布局文件中定义的各个控件引用
     * 这些控件用于显示车辆信息和接收用户操作
     * <p>
     * 控件说明：
     * - carIdText: 显示车牌号的TextView
     * - startTimeText: 显示入场时间的TextView
     * - endTimeText: 显示出场时间的TextView
     * - timeText: 显示停车时长的TextView
     * - priceText: 显示停车费用的TextView
     * - confirmButton: 确认出场按钮
     */
    private void initViews() {
        carIdText = findViewById(R.id.carIdText);        // 车牌号显示
        startTimeText = findViewById(R.id.startTimeText); // 入场时间显示
        endTimeText = findViewById(R.id.endTimeText);    // 出场时间显示
        timeText = findViewById(R.id.timeText);          // 停车时长显示
        priceText = findViewById(R.id.priceText);        // 停车费用显示
        confirmButton = findViewById(R.id.confirmButton); // 确认按钮
    }

    /**
     * 初始化网关连接器
     * <p>
     * 功能：创建与硬件网关的Socket连接，并设置连接监听器
     * <p>
     * 连接参数：
     * - IP地址：172.18.8.16（网关设备IP）
     * - 端口号：57500（默认通信端口）
     * <p>
     * 连接流程：
     * 1. 创建Socket数据总线
     * 2. 设置数据接收监听器（当前为空实现）
     * 3. 创建通用连接器并设置连接回调
     * 4. 连接成功后启动数据更新循环
     */
    private void initGateWayConnector() {
        // 创建Socket数据总线，连接到指定的IP和端口
        DataBus dataBus = DataBusFactory.newSocketDataBus("172.18.8.16", 57500);

        // 设置数据接收监听器（当前为空实现，可根据需要添加数据处理逻辑）
        dataBus.setReciveDataListener(bytes -> null);

        // 创建通用连接器，并设置连接结果回调
        genericConnector = new GenericConnector(dataBus, b -> {
            // 记录连接结果日志
            Log.d(TAG, "onConnectResult: " + (b ? "连接成功。" : "连接失败。"));

            // 连接成功后启动数据更新循环
            if (b) {
                update();
            }
        });
    }

    /**
     * 根据RFID标签查找车辆信息
     * <p>
     * 功能：在车辆信息列表中查找指定RFID对应的车辆信息
     *
     * @param rfid 要查找的RFID标签字符串
     * @return 找到的CarIdInfo对象，如果未找到则返回null
     * <p>
     * 查找逻辑：
     * 1. 遍历车辆信息列表
     * 2. 比较每个车辆的RFID标签
     * 3. 找到匹配项则返回该车辆信息
     * 4. 遍历完成未找到则返回null
     */
    private CarIdInfo findCarIdInfoByRfid(String rfid) {
        // 遍历车辆信息列表
        for (CarIdInfo idInfo : carIdInfoList) {
            // 比较RFID标签是否匹配
            if (rfid.equals(idInfo.getRfid())) {
                return idInfo;  // 找到匹配的车辆信息
            }
        }
        return null;  // 未找到匹配的车辆信息
    }

    /**
     * 初始化事件监听器
     * <p>
     * 功能：设置确认按钮的点击事件监听器，处理车辆出场流程
     * <p>
     * 出场流程验证：
     * 1. 获取当前RFID标签
     * 2. 验证RFID不为空
     * 3. 查找对应的车辆信息
     * 4. 验证车辆状态为RELEASED（已计费完成）
     * <p>
     * 出场控制流程（异步执行）：
     * 1. 点击确认放行按钮
     * 2. 推杆收回（道闸开启）（红灯灭，绿灯亮）
     * 3. 等待道闸完全开启
     * 4. 红灯灭，绿灯亮
     * 5. 语音播报"出入平安"
     * 6. 等待5秒（车辆通过时间）
     * 7. 红灯亮，绿灯灭
     * 8. 推杆伸出（关闭道闸）
     */
    private void initListeners() {
        confirmButton.setOnClickListener(view -> {
            this.rfid = gateWayResultData.getFmtData("uhf_2");
            if (rfid == null) return;
            CarIdInfo carIdInfoByRfid = findCarIdInfoByRfid(rfid);
            if (carIdInfoByRfid == null) return;
            if (carIdInfoByRfid.getState() != CarState.RELEASED) return;
            new Thread(() -> {
                try {
                    // 出场控制流程
                    // 使用CompletableFuture链式调用，确保上一个任务完成后再进行下一步操作
                    // 不要使用传统回调嵌套，容易造成回调地狱
                    // （当然你要是看得清那么多层嵌套或者写起来真的很快的话当我没说）
                    controlActuator("m_multi_red", true)
                            .thenCompose(v -> controlActuator("m_multi_green", false))
                            .thenCompose(v -> controlActuator("m_pushrod_putt", false))
                            .thenCompose(v -> controlActuator("m_pushrod_back", true))
                            .thenCompose(v -> waitForBackComplete())
                            .thenCompose(v -> controlActuator("m_multi_red", false))
                            .thenCompose(v -> controlActuator("m_multi_green", true))
                            .thenRunAsync(() -> tts.speak("出入平安。", TextToSpeech.QUEUE_FLUSH, null, null))
                            .thenCompose(v -> delaySeconds(5))
                            .thenCompose(v -> controlActuator("m_multi_red", true))
                            .thenCompose(v -> controlActuator("m_multi_green", false))
                            .thenCompose(v -> controlActuator("m_pushrod_putt", true))
                            .thenCompose(v -> controlActuator("m_pushrod_back", false));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    /**
     * 延迟指定秒数
     * <p>
     * 功能：创建一个异步延迟任务，用于在控制流程中等待
     *
     * @param seconds 要延迟的秒数
     * @return CompletableFuture<Void> 延迟完成后完成的Future
     * <p>
     * 实现原理：
     * 1. 创建CompletableFuture对象
     * 2. 在新线程中执行延迟
     * 3. 使用Thread.sleep实现延迟
     * 4. 延迟完成后标记为完成，通知CompletableFuture
     * <p>
     * 使用场景：
     * - 车辆通过道闸的等待时间
     * - 设备状态稳定的等待时间
     */
    private CompletableFuture<Void> delaySeconds(long seconds) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Log.d(TAG, "delaySeconds: 延迟" + seconds + "秒。");
                Thread.sleep(seconds * 1000);  // 转换为毫秒并延迟
                completableFuture.complete(null);  // 延迟完成，标记Future为完成状态
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        return completableFuture;
    }

    /**
     * 控制执行器（继电器、电机等）
     * <p>
     * 功能：向网关发送控制指令，控制指定的执行器设备
     *
     * @param apiTag 设备API标签，标识要控制的设备类型
     * @param data   控制数据，true表示开启/激活，false表示关闭/停止
     * @return CompletableFuture<Void> 控制完成后完成的Future
     * <p>
     * 常见设备API标签：
     * - "m_multi_red": 多路红灯控制
     * - "m_multi_green": 多路绿灯控制
     * - "m_pushrod_putt": 推杆伸出控制
     * - "m_pushrod_back": 推杆收回控制
     * <p>
     * 控制流程：
     * 1. 创建CompletableFuture对象
     * 2. 通过网关连接器发送控制指令
     * 3. 在原本回调方法中将任务标记为完成，通知CompletableFuture
     */
    private CompletableFuture<Void> controlActuator(String apiTag, boolean data) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            // 发送网关控制指令
            genericConnector.sendGateWayControl(apiTag, "0", data, new ConnectorListener() {
                @Override
                public void onSuccess(boolean b) {
                    // 控制成功，记录日志并完成Future
                    Log.d(TAG, "onSuccess: " + apiTag + ": " + data);
                    completableFuture.complete(null);
                }

                @Override
                public void onFail(Exception e) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completableFuture;
    }

    /**
     * 等待推杆完全收回
     * <p>
     * 功能：等待推杆完全收回的完成信号
     *
     * @return CompletableFuture<Void> 推杆完全收回后完成的Future
     * <p>
     * 检测逻辑：
     * 1. 循环检查接近开关的触发状态
     * 2. 如果接近开关被触发，说明推杆还未完全收回
     * 3. 等待100毫秒后继续检查
     * <p>
     * 开关说明：
     * - nearSwitchTrigger: 接近开关，检测推杆是否接近目标位置（推杆缩回到极限才不会触发该开关，这是底层装设备的应该知道的事情）
     * <p>
     */
    private CompletableFuture<Void> waitForBackComplete() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        new Thread(() -> {
            while (true) try {
                // 检查开关状态
                if (this.nearSwitchTrigger) {
                    Thread.sleep(100);  // 等待100毫秒后继续检查
                    continue;  // 继续循环检查
                }
                // 推杆完全收回，记录日志并完成Future
                Log.d(TAG, "waitForBackComplete: " + "完全开启。");
                completableFuture.complete(null);
                break;  // 退出循环
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        return completableFuture;
    }

    /**
     * 获取传感器数据
     * <p>
     * 功能：向网关发送查询指令，获取指定传感器的数据
     *
     * @param apiTag 传感器API标签，标识要查询的传感器类型
     * @return CompletableFuture<Void> 数据获取完成后完成的Future
     * <p>
     * 常见传感器API标签：
     * - "uhf_2": UHF RFID读取器，用于读取车辆RFID标签
     * - "m_near": 接近开关传感器，检测推杆是否接近目标位置
     * <p>
     * 数据获取流程：
     * 1. 创建CompletableFuture对象
     * 2. 通过网关连接器发送查询指令
     * 3. 在成功回调中将数据添加到缓存
     * 4. 完成Future
     * 5. 在失败回调中忽略异常
     */
    private CompletableFuture<Void> fetchSensorData(String apiTag) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            // 发送网关查询指令
            genericConnector.sendGateWaySearch(apiTag, new ConnectorListener() {
                @Override
                public void onSuccess(boolean b) {
                    // 查询成功，将数据添加到缓存
                    gateWayResultData.addData(genericConnector.getGateWayResultData(), apiTag);
                    completableFuture.complete(null);  // 完成Future
                }

                @Override
                public void onFail(Exception e) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completableFuture;
    }

    /**
     * 更新缓存数据
     * <p>
     * 功能：从网关数据缓存中读取最新数据，更新到成员变量中
     * <p>
     * 更新数据包括：
     * 1. RFID标签：从"uhf_2"传感器读取
     * 2. 接近开关状态：从"m_near"传感器读取，"1"表示触发
     * <p>
     * 数据格式说明：
     * - 传感器返回的数据是字符串格式
     * - "1"表示开关被触发/激活
     * - 其他值或null表示开关未触发
     */
    private void updateCacheData() {
        // 更新RFID标签
        this.rfid = gateWayResultData.getFmtData("uhf_2");
        // 更新接近开关状态（"1"表示触发）
        this.nearSwitchTrigger = "1".equals(gateWayResultData.getFmtData("m_near"));
    }

    /**
     * 初始化文本转语音（TTS）引擎
     * <p>
     * 功能：创建TextToSpeech实例，用于语音播报提示信息
     * <p>
     * 使用场景：
     * 1. 车辆入场时播报欢迎信息
     * 2. 车辆出场时播报"出入平安"
     * 3. 系统状态提示
     */
    private void initTTS() {
        tts = new TextToSpeech(MainActivity.this, i -> {
            // 到达这里说明初始化完毕，可以进行使用了
            // 实际上这个过程很快，所以代码中直接进行使用
        });
    }

    /**
     * 数据更新循环
     * <p>
     * 功能：周期性获取传感器数据，并更新车辆状态和UI显示
     * <p>
     * 更新流程：
     * 1. 获取RFID数据（uhf_2）
     * 2. 获取接近开关数据（m_near）
     * 3. 更新缓存数据
     * 4. 处理车辆状态转换
     * 5. 更新UI显示
     * 6. 递归调用自身，形成更新循环
     * <p>
     * 车辆状态机：
     * None -> ENTERED: 车辆首次识别，记录入场时间，播报欢迎信息
     * ENTERED -> RELEASED: 车辆再次识别，记录出场时间，更新UI显示
     * <p>
     * 注意：这是一个递归调用（最后有一个handler.post），会形成无限循环，直到Activity销毁
     */
    private void update() {
        // 异步获取传感器数据链
        fetchSensorData("uhf_2")                // 1. 获取RFID数据
                .thenCompose(v -> fetchSensorData("m_near"))        // 2. 获取接近开关数据
                .thenRun(this::updateCacheData)                     // 3. 更新缓存数据
                .thenRun(
                        () -> {
                            // 4. 处理车辆状态转换
                            for (CarIdInfo idInfo : carIdInfoList) {
                                if (idInfo == null || this.rfid == null) return;
                                // 检查RFID是否匹配当前车辆
                                if (this.rfid.equals(idInfo.getRfid())) {
                                    // 状态转换：None -> ENTERED（车辆入场）
                                    if (idInfo.getState() == CarState.None) {
                                        idInfo.setState(CarState.ENTERED);  // 更新状态为已入场
                                        tts.speak("欢迎入场，" + this.rfid, TextToSpeech.QUEUE_FLUSH, null, null);  // 语音播报
                                        idInfo.setStartTime(new Date().getTime());  // 记录入场时间
                                    }
                                    // 状态转换：ENTERED -> RELEASED（车辆出场）
                                    if (idInfo.getState() == CarState.ENTERED) {
                                        idInfo.setEndTime(new Date().getTime());  // 记录出场时间
                                        // 6. 在UI线程中更新显示
                                        runOnUiThread(() -> {
                                            carIdText.setText(idInfo.getCarId());                    // 显示车牌号
                                            startTimeText.setText(idInfo.getFormatStartTime());      // 显示入场时间
                                            endTimeText.setText(idInfo.getFormatEndTime());          // 显示出场时间
                                            timeText.setText(idInfo.getFormatTime());                // 显示停车时长
                                            priceText.setText(idInfo.getPrice() + "元");             // 显示停车费用
                                        });

                                        idInfo.setState(CarState.RELEASED);  // 更新状态为已释放（可出场）
                                    }
                                }
                            }
                        }
                ).thenRun(() -> handler.post(MainActivity.this::update));  // 7. 递归调用，形成更新循环
    }
}
