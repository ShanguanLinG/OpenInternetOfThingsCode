package org.shanguanling.project;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.SensorReaTimeData;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.CloudService;
import cn.com.newland.nle_sdk.util.CloudServiceListener;

public class MainActivity extends AppCompatActivity {

    // 本类未测试，仅供学习参考

    ImageView fanImage;
    ImageView lampImage;
    CloudService cloudService;
    Handler handler = new Handler();
    Runnable autoUpdate = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 1000);
            updateAll();
        }
    };
    int co2Value;
    TextView co2ValueText;
    boolean lampOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("主界面");
        initViews();
        cloudService = new CloudService("http://192.168.0.138/");
        cloudService.signIn(new SignIn("18912345635", "123456"), new CloudServiceListener<BaseResponseEntity<User>>() {
            @Override
            protected void onResponse(BaseResponseEntity<User> userBaseResponseEntity) {
                cloudService.setAccessToken(userBaseResponseEntity.getResultObj().getAccessToken());
                initListeners();
                handler.post(autoUpdate);
            }
        });
    }

    private void initViews() {
        fanImage = findViewById(R.id.fanImage);
        lampImage = findViewById(R.id.lampImage);
        co2ValueText = findViewById(R.id.co2ValueText);
    }


    private void initListeners() {
        lampImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lampOn = !lampOn;
                if (lampOn) {
                    lampImage.setImageResource(R.drawable.lamp_on);
                    fastControl(123456, "m_lamp", 1);
                } else {
                    lampImage.setImageResource(R.drawable.lamp_off);
                    fastControl(123456, "m_lamp", 0);
                }
            }
        });
    }

    private void fastControl(int deviceId, String apiTag, Object data) {
        cloudService.control(deviceId, apiTag, data, new CloudServiceListener<BaseResponseEntity>() {
            @Override
            protected void onResponse(BaseResponseEntity baseResponseEntity) {
            }
        });
    }

    private void updateValue() {
        cloudService.getSensorsRealTimeData(136630, new CloudServiceListener<BaseResponseEntity<List<SensorReaTimeData>>>() {
            @Override
            protected void onResponse(BaseResponseEntity<List<SensorReaTimeData>> listBaseResponseEntity) {
                for (SensorReaTimeData sensorReaTimeData : listBaseResponseEntity.getResultObj()) {
                    if (!"m_co2".equals(sensorReaTimeData.getApiTag())) continue;
                    co2Value = Integer.parseInt(sensorReaTimeData.getValue());
                    updateViews();
                    return;
                }
            }
        });
    }

    private void updateViews() {
        co2ValueText.setText(co2Value + "");
        Drawable drawable = fanImage.getDrawable();
        if (!(drawable instanceof AnimationDrawable)) return;
        if (co2Value >= 100) {
            fastControl(123456, "m_fan", 1);
            ((AnimationDrawable) drawable).start();
            return;
        }
        ((AnimationDrawable) drawable).stop();
    }

    private void updateAll() {
        updateValue();
    }
}
