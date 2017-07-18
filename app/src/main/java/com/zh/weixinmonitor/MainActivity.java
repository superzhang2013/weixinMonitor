package com.zh.weixinmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView startMonitorView;

    private TextView stopMonitorView;

    private TextView showTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startMonitorView = (TextView) findViewById(R.id.start_permission_view);
        stopMonitorView = (TextView) findViewById(R.id.stop_permission_view);
        showTextView = (TextView) findViewById(R.id.ready_monitor_view);

        startMonitorView.setOnClickListener(this);
        stopMonitorView.setOnClickListener(this);
        if (isAccessibilitySettingsOn()) {
            Intent intent = new Intent(this, MyAccessibilityService.class);
            startService(intent);
        }
    }

    private boolean isAccessibilitySettingsOn() {
        int accessibiltyEnabled = 0;
        String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibiltyEnabled = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ex) {
            Log.e(this.toString(), "settings not found exception ");
        }
        TextUtils.SimpleStringSplitter mStringSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibiltyEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringSplitter.setString(settingValue);
                while (mStringSplitter.hasNext()) {
                    String accessibiltyService = mStringSplitter.next();
                    if (accessibiltyService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            String mAction = Settings.ACTION_ACCESSIBILITY_SETTINGS;
            Intent intent = new Intent(mAction);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_permission_view: {
                if (isAccessibilitySettingsOn()) {
                    Intent intent = new Intent(this, MyAccessibilityService.class);
                    startService(intent);
                    showTextView.setText("正在监听");
                }
            }
            break;
            case R.id.stop_permission_view: {
                Intent intent = new Intent(this, MyAccessibilityService.class);
                stopService(intent);
                showTextView.setText("停止监听");
            }
            break;
        }
    }
}
