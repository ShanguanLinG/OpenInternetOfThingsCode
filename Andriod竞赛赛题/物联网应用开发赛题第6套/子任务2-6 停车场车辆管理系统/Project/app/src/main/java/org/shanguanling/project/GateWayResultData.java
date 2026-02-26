package org.shanguanling.project;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.ConcurrentHashMap;

public class GateWayResultData {
    ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();

    public GateWayResultData() {
    }

    public void addData(String gateWayResult, String apiTag) {
        data.put(apiTag, gateWayResult);
    }

    private String getData(String apiTag) {
        return data.get(apiTag);
    }

    public String getFmtData(String apiTag) {
        return getValueFromJson(getData(apiTag), apiTag);
    }

    private String getValueFromJson(String json, String apiTag) {
        if (json == null) return "";
        JsonParser jsonParser = new JsonParser();
        JsonObject asJsonObject = jsonParser.parse(json).getAsJsonObject();
        if (!asJsonObject.has("datas")) {
            return null;
        }
        JsonObject datas = asJsonObject.get("datas").getAsJsonObject();
        if (!datas.has(apiTag)) {
            return null;
        }
        return datas.get(apiTag).getAsString();
    }
}
