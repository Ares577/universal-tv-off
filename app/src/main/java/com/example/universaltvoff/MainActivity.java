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
                int sent = 0;
                for (IrDatabase.Code code : IrDatabase.POWER) {
                    int[] pattern = patternFor(code);
                    if (pattern != null) {
                        ir.transmit(carrierFor(code.p), pattern);
                        sent++;
                        pause(3);
                    }
                }
                result = sent + " TV güç profili gönderildi.";
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

    private int carrierFor(String protocol) {
        return protocol.startsWith("SIRC") ? 40000 : protocol.startsWith("RC") ? 36000 : 38000;
    }

    private int[] patternFor(IrDatabase.Code code) {
        switch (code.p) {
            case "NEC": return nec(code.a & 255, code.c & 255);
            case "NECext": return necExt(code.a, code.c & 255);
            case "Samsung32": return samsung32(code.a, code.c);
            case "RC5": return rc5(code.a, code.c);
            case "SIRC": return sony12(code.c & 127, code.a & 31);
            case "SIRC15": return sony15(code.c & 127, code.a & 255);
            case "SIRC20": return sony20(code.c & 127, code.a & 255);
            default: return null;
        }
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

    private int[] necExt(int address, int command) {
        int[] p = new int[67]; int i = 0;
        p[i++] = 9000; p[i++] = 4500;
        int data = (address & 0xffff) | ((command & 255) << 16) | ((command ^ 255) << 24);
        for (int bit = 0; bit < 32; bit++) { p[i++] = 560; p[i++] = (data & (1 << bit)) != 0 ? 1690 : 560; }
        p[i] = 560; return p;
    }

    private int[] samsung32(int address, int command) {
        return nec(address & 255, command & 255);
    }

    private int[] sony15(int command, int device) {
        return sonyGeneric(command, device, 15);
    }

    private int[] sony20(int command, int device) {
        return sonyGeneric(command, device, 20);
    }

    private int[] sonyGeneric(int command, int device, int bits) {
        int[] p = new int[2 + bits * 2 + 1]; int i = 0;
        p[i++] = 2400; p[i++] = 600;
        int data = (command & 127) | ((device & 255) << 7);
        for (int bit = 0; bit < bits; bit++) { p[i++] = 600; p[i++] = (data & (1 << bit)) != 0 ? 1200 : 600; }
        p[i] = 600; return p;
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
