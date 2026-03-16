package com.example.myapplication

import cn.com.newland.nle_sdk.requestEntity.SignIn
import cn.com.newland.nle_sdk.responseEntity.SensorReaTimeData
import cn.com.newland.nle_sdk.responseEntity.User
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity
import cn.com.newland.nle_sdk.util.CloudService
import cn.com.newland.nle_sdk.util.CloudServiceListener

// 已测试

object NleCloudSdkExample {

    var cloudService: CloudService? = null

    fun load() {
        cloudService = CloudService("http://www.nlecloud.com/")
        cloudService!!.signIn(
            SignIn("18451016824", "ender666"),
            object : CloudServiceListener<BaseResponseEntity<User>>() {
                override fun onResponse(user: BaseResponseEntity<User>?) {
                    cloudService!!.setAccessToken(user?.resultObj?.accessToken)
                    println("登录成功。")
                    printSensorData(1322935, "m_Infrared")
                    control(1390229, "m_fan", true)
                }
            }
        )

    }

    private fun printSensorData(projectId: Int, apiTag: String) {
        cloudService?.getSensorsRealTimeData(
            projectId,
            object : CloudServiceListener<BaseResponseEntity<List<SensorReaTimeData>>>() {
                override fun onResponse(data: BaseResponseEntity<List<SensorReaTimeData>>?) {
                    for (sensorReaTimeData in data?.resultObj!!) {
                        if (apiTag != sensorReaTimeData.apiTag) continue
                        println(sensorReaTimeData.value)
                    }
                }
            }
        )

    }

    private fun control(deviceId: Int, apiTag: String, data: Any) {
        cloudService?.control(deviceId, apiTag, data, object :
            CloudServiceListener<BaseResponseEntity<Any>>() {
            override fun onResponse(data: BaseResponseEntity<Any>?) {
            }
        })
    }
}