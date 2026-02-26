package org.shanguanling.project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nle.mylibrary.claimer.connector.ConnectorListener;
import com.nle.mylibrary.databus.DataBus;
import com.nle.mylibrary.databus.DataBusFactory;
import com.nle.mylibrary.databus.ReciveData;
import com.nle.mylibrary.device.GenericConnector;
import com.nle.mylibrary.device.listener.ConnectResultListener;


public class MainActivity extends AppCompatActivity {
    /**
     * @author ShanguanLinG
     * @since 2026/01/12
     * nle_mylibrary包控制网关设备与获取网关设备数据示例
     * <p>
     * 需要保证平板的IP配置正确，且AndroidManifest.xml中写了
     * <uses-permission android:name="android.permission.INTERNET" />
     */
    private String TAG = "TAG";
    GenericConnector genericConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 创建 DataBus（Socket 模式）
         * - Socket 模式：DataBusFactory.newSocketDataBus(ip, port)
         * - 串口模式：DataBusFactory.newSerialDataBus(serialPortIndex, baudRate)
         *
         * 本示例使用 Socket 连接网关。
         */
        DataBus dataBus = DataBusFactory.newSocketDataBus("172.18.8.16", 57500);
        /**
         * 绑定接收数据解析监听
         *
         * getReciveData(byte[] bytes) 会在收到网关数据时回调：
         * - bytes 为接收到的原始字节数组
         * - 你可以在这里按协议解析（如 ZigBee/UWB/自定义报文等）
         * - 也可以选择不处理，仅用于控制执行器或读取传感器数据
         *
         * 建议：一个 ReciveData 回调中只解析一种协议/一种报文类型，避免长度不一致导致异常。
         */
        dataBus.setReciveDataListener(new ReciveData() {
            @Override
            public String getReciveData(byte[] bytes) {
                return null;
            }
        });
        genericConnector = new GenericConnector(dataBus, new ConnectResultListener() {
            @Override
            public void onConnectResult(boolean b) {
                try {
                    /**
                     * 下发“继电器/执行器”控制命令示例。
                     * 尽量确保在回调函数中执行，防止genericConnector未连接到网关就直接发送命令
                     * 如果以按钮形式触发的话，请将初始化写在程序最开始的地方（setContentView(R.layout.activity_main);后面）
                     *
                     * "m_pushrod_putt"：网关执行器的标识名称
                     * "1"：命令ID，随意填写即可
                     * false：发送的命令内容，根据不同的执行器可以填写不同的类型，比如int或者String
                     */
                    genericConnector.sendGateWayControl(
                            "m_pushrod_putt",
                            "1",
                            false,
                            new ConnectorListener() {
                                @Override
                                public void onSuccess(boolean b) {
                                    Log.d(TAG, "onSuccess: 发送成功。");
                                }

                                @Override
                                public void onFail(Exception e) {

                                }
                            });
                    genericConnector.sendGateWaySearch("uhf_2", new ConnectorListener() {
                        @Override
                        public void onSuccess(boolean b) {
                            Log.d(TAG, "onSuccess: 获取成功。");
                            // 用getGateWayResultData也是一样的
                            // 获取到的信息是类似这样的一长串：
                            // {"t":4, "msgid":12311,"status": 0, "datatype":1,"datas":{"uhf_2":"E2 00 47 0F B9 60 64 26 A7 22 01 0C"}}
                            // 其中 E2 00 47 0F B9 60 64 26 A7 22 01 0C 是我们要拿到的数据，我们要将其进行转换
                            String gateWayResultData = genericConnector.getGateWayResultData();
                            Log.d(TAG, "onSuccess: " + gateWayResultData);
                            Log.d(TAG, "onSuccess: " + getDataValue(gateWayResultData, "uhf_2"));
                        }

                        @Override
                        public void onFail(Exception e) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 从给定的 JSON 字符串中提取出指定apiTag对应的数据值。
     * 该方法首先会解析输入的JSON字符串，检查是否包含datas字段，
     * 然后查找指定的apiTag，如果存在，则返回对应的值。如果任何步骤出错或字段不存在，则返回null
     *
     * @param json   原始的 JSON 字符串，包含了一个datas字段以及不同的API标签（如 uhf_2）。
     *               示例：{"datas":{"uhf_2":"E2 00 47 0F B9 60 64 26 A7 22 01 0C"}}
     * @param apiTag 在datas字段中的键（API 标签），例如"uhf_2"
     *               该方法将根据此apiTag获取对应的值。
     *               示例："uhf_2"
     * @return 如果成功找到apiTag对应的数据，返回该数据值（如E2 00 47 ...）。
     * 如果datas或apiTag不存在，返回null。
     */
    private String getDataValue(String json, String apiTag) {
        JsonParser jsonParser = new JsonParser();
        JsonObject root = jsonParser.parse(json).getAsJsonObject();
        if (!root.has("datas")) return null;
        JsonObject datas = root.getAsJsonObject("datas");
        if (!datas.has(apiTag)) return null;
        JsonElement jsonElement = datas.get(apiTag);
        return jsonElement.isJsonNull() ? null : jsonElement.getAsString();
    }
}