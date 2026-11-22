package com.example.universaltvoff;

import android.app.Activity;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private TextView status;
    private Button button;
    private final Handler handler = new Handler();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        button = findViewById(R.id.offButton);
        ir = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
        button.setOnClickListener(v -> sendPowerSweep());
    }

    private void sendPowerSweep() {
        boolean hasEmitter = false;
        try {
            hasEmitter = ir != null && ir.hasIrEmitter();
        } catch (Throwable ignored) {
            hasEmitter = false;
        }
        if (!hasEmitter) {
            status.setText("Bu telefonda IR verici bulunamadı.");
            return;
        }
        button.setEnabled(false);
        status.setText("Güç kodları gönderiliyor...");
        new Thread(() -> {
            String result = "Komutlar gönderildi.";
            try {
                // NEC family: common TV device addresses and power command variants.
                int[] addresses = {0x00, 0x04, 0x07, 0x08, 0x10, 0x20, 0x40, 0x80};
                int[] commands = {0x02, 0x08, 0x0C, 0x0D, 0x12, 0x40, 0x46, 0x47, 0x4C, 0x5A};
                for (int address : addresses) for (int command : commands) {
                    ir.transmit(38000, nec(address, command));
                    pause(35);
                }
                ir.transmit(40000, sony12(0x15, 1));
                pause(50);
                ir.transmit(36000, rc5(0, 0x0C));
            } catch (Throwable error) {
                result = "IR gönderimi desteklenmiyor: " + error.getClass().getSimpleName();
            }
            String finalResult = result;
            runOnUiThread(() -> {
                button.setEnabled(true);
                status.setText(finalResult);
            });
        }).start();
    }

    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private int[] nec(int address, int command) {
        int[] p = new int[2 + 32 * 2 + 1];
        int i = 0; p[i++] = 9000; p[i++] = 4500;
        int data = (address & 255) | ((address ^ 255) << 8) | ((command & 255) << 16) | ((command ^ 255) << 24);
        for (int bit = 0; bit < 32; bit++) {
            p[i++] = 560; p[i++] = ((data & (1 << bit)) != 0) ? 1690 : 560;
        }
        p[i] = 560;
        return p;
    }

    private int[] sony12(int command, int device) {
        int[] p = new int[2 + 12 * 2 + 1];
        int i = 0; p[i++] = 2400; p[i++] = 600;
        int data = (command & 127) | ((device & 31) << 7);
        for (int bit = 0; bit < 12; bit++) {
            p[i++] = 600; p[i++] = ((data & (1 << bit)) != 0) ? 1200 : 600;
        }
        p[i] = 600;
        return p;
    }

    private int[] rc5(int address, int command) {
        int[] p = new int[2 + 14 * 2];
        int i = 0; p[i++] = 889; p[i++] = 889;
        int data = 0x3000 | ((address & 31) << 6) | (command & 63);
        for (int bit = 13; bit >= 0; bit--) {
            boolean one = (data & (1 << bit)) != 0;
            p[i++] = one ? 889 : 1778;
            p[i++] = one ? 1778 : 889;
        }
        return p;
    }
}
