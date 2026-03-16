package com.example.myapplication

import android.util.Log
import com.nle.mylibrary.claimer.connector.ConnectorListener
import com.nle.mylibrary.databus.DataBus
import com.nle.mylibrary.databus.DataBusFactory
import com.nle.mylibrary.device.GenericConnector

// 未测试，无环境（应该没问题）

object NleHardWareExample {

    private val TAG = this.javaClass.classes.toString()
    private var genericConnector: GenericConnector? = null
    private val dataBus: DataBus = DataBusFactory.newSocketDataBus("172.18.8.16", 57500)

    fun load() {
        genericConnector = GenericConnector(dataBus) {
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
                genericConnector?.sendGateWayControl(
                    "m_pushrod_putt",
                    "1",
                    false,
                    object : ConnectorListener {
                        override fun onSuccess(b: Boolean) {
                            Log.d(TAG, "onSuccess: 发送成功。")
                        }

                        override fun onFail(e: Exception) {}
                    })
                genericConnector?.sendGateWaySearch("uhf_2", object : ConnectorListener {
                    override fun onSuccess(b: Boolean) {
                        Log.d(TAG, "onSuccess: 获取成功。")
                        // 用getGateWayResultData也是一样的
                        // 获取到的信息是类似这样的一长串：
                        // {"t":4, "msgid":12311,"status": 0, "datatype":1,"datas":{"uhf_2":"E2 00 47 0F B9 60 64 26 A7 22 01 0C"}}
                        // 其中 E2 00 47 0F B9 60 64 26 A7 22 01 0C 是我们要拿到的数据，我们要将其进行转换
                        val gateWayResultData: String = genericConnector!!.gateWayResultData
                        Log.d(TAG, "onSuccess: $gateWayResultData")
                        Log.d(
                            TAG,
                            "onSuccess: " + JsonUtil.getDataValue(gateWayResultData, "uhf_2")
                        )
                    }

                    override fun onFail(e: Exception) {}
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}