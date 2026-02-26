package org.shanguanling.project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.SensorReaTimeData;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.CloudService;
import cn.com.newland.nle_sdk.util.CloudServiceListener;

public class SellActivity extends AppCompatActivity {
    String ACTIVATE_TICKET_ID_KEY = "activate_ticket_id";
    CloudService cloudService;
    EditText editText;
    Button readButton;
    Button activateButton;
    TextView sellText;
    String ticketId;
    HashSet<String> set;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("售票系统");
        initViews();
        initSP();
        cloudService = new CloudService("http://192.168.0.138/");
        cloudService.signIn(new SignIn("18912345635", "123456"), new CloudServiceListener<BaseResponseEntity<User>>() {
            @Override
            protected void onResponse(BaseResponseEntity<User> userBaseResponseEntity) {
                cloudService.setAccessToken(userBaseResponseEntity.getResultObj().getAccessToken());
                initListeners();
            }
        });
    }

    private void initViews() {
        editText = findViewById(R.id.editText);
        readButton = findViewById(R.id.readButton);
        activateButton = findViewById(R.id.activateButton);
        sellText = findViewById(R.id.sellText);
    }

    private void initListeners() {
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue();
            }
        });
        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                set.add(ticketId);
                SharedPreferencesManager.putSet(ACTIVATE_TICKET_ID_KEY, set);
                Toast.makeText(SellActivity.this, "已激活：" + ticketId, Toast.LENGTH_LONG).show();
            }
        });
        sellText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellActivity.this, FirstActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initSP() {
        SharedPreferencesManager.init(SellActivity.this);
        SharedPreferencesManager.removeSets(ACTIVATE_TICKET_ID_KEY);
        set = new HashSet<>();
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
    }
}
