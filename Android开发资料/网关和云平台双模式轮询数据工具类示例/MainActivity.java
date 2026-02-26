package org.shanguanling.b;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * @author: ShanguanLinG
     * @since 2026/01/29 /12:50
     */

    CloudClient cloudClient;
    GateWayClient gateWayClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        startCloudClient();
    }

    private void startCloudClient() {
        cloudClient = new CloudClient("account", "password");
        cloudClient.connect().thenRun(() -> cloudClient.startPolling(1322935));
        cloudClient.setListener(data -> System.out.println(data.get("l_co")));
        gateWayClient = new GateWayClient("172.18.23.16", 57500);
        gateWayClient.connect().thenRun(() -> gateWayClient.startPoller(new String[]{"uhf"}));
        gateWayClient.setListener(data -> System.out.println(data.get("uhf")));
    }

    private void initViews() {
    }
}
