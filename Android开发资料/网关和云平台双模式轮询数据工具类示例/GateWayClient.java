package org.shanguanling.b;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nle.mylibrary.claimer.connector.ConnectorListener;
import com.nle.mylibrary.databus.DataBus;
import com.nle.mylibrary.databus.DataBusFactory;
import com.nle.mylibrary.device.GenericConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class GateWayClient {
    private String ip;
    private int port;
    private GenericConnector genericConnector;
    private IDataUpdateListener listener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private HashMap<String, String> data = new HashMap<>();

    public GateWayClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void setListener(IDataUpdateListener listener) {
        this.listener = listener;
    }

    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        DataBus dataBus = DataBusFactory.newSocketDataBus(ip, port);
        dataBus.setReciveDataListener(bytes -> null);
        genericConnector = new GenericConnector(dataBus, b -> {
            System.out.println(b ? "连接成功。" : "连接失败。");
            completableFuture.complete(null);
        });
        return completableFuture;
    }

    public void startPoller(String[] pollerApiTags) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<CompletableFuture<String>> futures = new ArrayList<>();
                for (String apiTag : pollerApiTags) {
                    CompletableFuture<String> future =
                            getSensorData(apiTag).thenApply(value -> {
                                if (value != null) {
                                    synchronized (data) {
                                        data.put(apiTag, value);
                                    }
                                }
                                return value;
                            });
                    futures.add(future);
                }
                CompletableFuture
                        .allOf(futures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> {
                            if (listener != null) {
                                listener.onDataUpdate(new HashMap<>(data));
                            }
                            handler.postDelayed(this, 1000);
                        });
            }
        });
    }

    public void stopPoller() {
        handler.removeCallbacksAndMessages(null);
    }


    private CompletableFuture<String> getSensorData(String apiTag) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        try {
            genericConnector.sendGateWaySearch(apiTag, new ConnectorListener() {
                @Override
                public void onSuccess(boolean b) {
                    String gateWayResultData = genericConnector.getGateWayResultData();
                    String valueFromJson = getValueFromJson(gateWayResultData, apiTag);
                    completableFuture.complete(valueFromJson);
                }

                @Override
                public void onFail(Exception e) {
                    completableFuture.complete(null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completableFuture;
    }

    public void control(String apiTag, Object value) {
        try {
            genericConnector.sendGateWayControl(apiTag, "0", value, new ConnectorListener() {
                @Override
                public void onSuccess(boolean b) {
                }

                @Override
                public void onFail(Exception e) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getValueFromJson(String json, String apiTag) {
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