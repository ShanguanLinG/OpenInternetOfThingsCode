package com.example.myapplication

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object JsonUtil {
    /**
     * 从给定的 JSON 字符串中提取出指定apiTag对应的数据值。
     * 该方法首先会解析输入的JSON字符串，检查是否包含datas字段，
     * 然后查找指定的apiTag，如果存在，则返回对应的值。如果任何步骤出错或字段不存在，则返回null
     *
     * @param json   原始的 JSON 字符串，包含了一个datas字段以及不同的API标签（如 uhf_2）。
     * 示例：{"datas":{"uhf_2":"E2 00 47 0F B9 60 64 26 A7 22 01 0C"}}
     * @param apiTag 在datas字段中的键（API 标签），例如"uhf_2"
     * 该方法将根据此apiTag获取对应的值。
     * 示例："uhf_2"
     * @return 如果成功找到apiTag对应的数据，返回该数据值（如E2 00 47 ...）。
     * 如果datas或apiTag不存在，返回null。
     */
    fun getDataValue(json: String, apiTag: String): String? {
        val jsonParser = JsonParser()
        val root: JsonObject = jsonParser.parse(json).asJsonObject
        if (!root.has("datas")) return null
        val datas: JsonObject = root.getAsJsonObject("datas")
        if (!datas.has(apiTag)) return null
        val jsonElement: JsonElement = datas.get(apiTag)
        return if (jsonElement.isJsonNull) null else jsonElement.asString
    }
}