package org.shanguanling.b;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.SensorReaTimeData;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.CloudService;
import cn.com.newland.nle_sdk.util.CloudServiceListener;

public class CloudClient {
    private CloudService cloudService;
    private String account;
    private String password;
    private IDataUpdateListener listener;
    private HashMap<String, String> data;
    private Handler handler = new Handler(Looper.getMainLooper());

    public CloudClient(String account, String password) {
        this.account = account;
        this.password = password;
        data = new HashMap<>();
    }

    public void setListener(IDataUpdateListener listener) {
        this.listener = listener;
    }

    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        cloudService = new CloudService("http://www.nlecloud.com/");
        cloudService.signIn(new SignIn(this.account, this.password), new CloudServiceListener<BaseResponseEntity<User>>() {
            @Override
            protected void onResponse(BaseResponseEntity<User> userBaseResponseEntity) {
                cloudService.setAccessToken(userBaseResponseEntity.getResultObj().getAccessToken());
                System.out.println("登录成功。");
                completableFuture.complete(null);
            }
        });
        return completableFuture;
    }

    public void startPolling(int pollerProjectId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getAllSensorValue(pollerProjectId).thenAccept(t -> handler.postDelayed(this, 1000));
            }
        });
    }

    public void stopPolling() {
        handler.removeCallbacksAndMessages(null);
    }

    private CompletableFuture<Void> getAllSensorValue(int projectId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        cloudService.getSensorsRealTimeData(projectId, new CloudServiceListener<BaseResponseEntity<List<SensorReaTimeData>>>() {
            @Override
            protected void onResponse(BaseResponseEntity<List<SensorReaTimeData>> listBaseResponseEntity) {
                List<SensorReaTimeData> resultObj = listBaseResponseEntity.getResultObj();
                for (SensorReaTimeData sensorReaTimeData : resultObj) {
                    String apiTag = sensorReaTimeData.getApiTag();
                    String value = sensorReaTimeData.getValue();
                    data.put(apiTag, value);
                }
                if (listener != null) listener.onDataUpdate(data);
                completableFuture.complete(null);
            }
        });
        return completableFuture;
    }

    public void control(int deviceId, String apiTag, Object data) {
        cloudService.control(deviceId, apiTag, data, new CloudServiceListener<BaseResponseEntity>() {
            @Override
            protected void onResponse(BaseResponseEntity baseResponseEntity) {
            }
        });
    }
}
