package com.floatnotes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLaunch = findViewById(R.id.btn_launch);
        Button btnStop = findViewById(R.id.btn_stop);
        TextView tvStatus = findViewById(R.id.tv_status);

        updateStatus(tvStatus);

        btnLaunch.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission();
            } else {
                startFloatingService();
            }
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, FloatingNoteService.class));
            tvStatus.setText("Service stopped. Tap Launch to start again.");
        });

        // Auto-launch if permission already granted
        if (Settings.canDrawOverlays(this)) {
            startFloatingService();
            finish(); // Go back to launcher, service runs in background
        }
    }

    private void updateStatus(TextView tv) {
        if (Settings.canDrawOverlays(this)) {
            tv.setText("✓ Overlay permission granted\nTap Launch to start floating notes");
        } else {
            tv.setText("⚠ Overlay permission required\nTap Launch and grant permission");
        }
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
    }

    private void startFloatingService() {
        Intent intent = new Intent(this, FloatingNoteService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "Float Notes started! Look for the bubble.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingService();
            } else {
                Toast.makeText(this, "Permission denied. Cannot show floating notes.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
