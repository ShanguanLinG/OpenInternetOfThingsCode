package org.shanguanling.project;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.SensorReaTimeData;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.CloudService;
import cn.com.newland.nle_sdk.util.CloudServiceListener;

public class FirstActivity extends AppCompatActivity {
    CloudService cloudService;
    EditText editText;
    Handler handler = new Handler();
    Runnable autoUpdate = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 1000);
            updateAll();
        }
    };
    String ticketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("首界面");
        initViews();
        cloudService = new CloudService("http://192.168.0.138/");
        cloudService.signIn(new SignIn("18912345635", "123456"), new CloudServiceListener<BaseResponseEntity<User>>() {
            @Override
            protected void onResponse(BaseResponseEntity<User> userBaseResponseEntity) {
                cloudService.setAccessToken(userBaseResponseEntity.getResultObj().getAccessToken());
                handler.post(autoUpdate);
            }
        });
    }

    private void initViews() {
        editText = findViewById(R.id.editText);
    }

    private void updateValue() {
        cloudService.getSensorsRealTimeData(136630, new CloudServiceListener<BaseResponseEntity<List<SensorReaTimeData>>>() {
            @Override
            protected void onResponse(BaseResponseEntity<List<SensorReaTimeData>> listBaseResponseEntity) {
                for (SensorReaTimeData sensorReaTimeData : listBaseResponseEntity.getResultObj()) {
                    if (!"uhf".equals(sensorReaTimeData.getApiTag())) continue;
                    ticketId = sensorReaTimeData.getValue();
                    updateViews();
                    return;
                }
            }
        });
    }

    private void updateViews() {
        editText.setText(ticketId);
        boolean ticketIdActivated = isTicketIdActivated(editText.getText());
        if (ticketIdActivated) {
            Toast.makeText(FirstActivity.this, "刷票成功，即将进入主界面。", Toast.LENGTH_LONG).show();
            handler.removeCallbacks(autoUpdate);
            return;
        }
        Toast.makeText(FirstActivity.this, "刷票失败，请先购票。", Toast.LENGTH_LONG).show();
    }

    private boolean isTicketIdActivated(Editable text) {
        Set<String> activateTicketIds = SharedPreferencesManager.getSets("activate_ticket_id", new HashSet<String>());
        if (text == null || activateTicketIds == null) return false;
        for (String activateTicketId : activateTicketIds) {
            if (text.toString().equals(activateTicketId)) {
                return true;
            }
        }
        return false;
    }

    private void updateAll() {
        updateValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoUpdate);
    }
}
