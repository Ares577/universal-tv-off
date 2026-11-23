package com.example.universaltvoff;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

public final class VantaRemoteView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Runnable action;
    private String status = "Sinyal başarıyla hazırlandı.";
    private boolean busy;
    private float sx = 1f, sy = 1f;

    public VantaRemoteView(Context c, Runnable action) {
        super(c); this.action = action; p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        setFocusable(true);
    }

    public void setBusy(boolean value) { busy = value; invalidate(); }
    public void setStatus(String value) { status = value; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        sx = getWidth() / 844f; sy = getHeight() / 1826f;
        c.save(); c.scale(sx, sy);
        Paint bg = new Paint();
        bg.setShader(new LinearGradient(0, 0, 0, 1826, Color.rgb(1,3,8), Color.BLACK, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 844, 1826, bg);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(Color.rgb(8,76,180));
        c.drawArc(new RectF(-180,-230,1020,470), 15, 150, false, p);
        p.setStyle(Paint.Style.FILL);
        text(c, "V", 229, 204, 72, Color.rgb(15,119,255), true);
        text(c, "ANTA", 332, 204, 65, Color.WHITE, true);
        text(c, "IR Universal Remote", 304, 254, 25, Color.rgb(145,157,179), false);

        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4); p.setColor(Color.rgb(16,113,255));
        r.set(157,414,692,670); c.drawRect(r,p);
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(8,14,26)); c.drawRect(160,417,689,666,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(10); p.setColor(Color.rgb(15,119,255));
        c.drawArc(new RectF(383,495,469,581), 45, 270, false, p);
        c.drawLine(426, 485, 426, 530, p);
        p.setStyle(Paint.Style.FILL);
        text(c, "Tüm Televizyonları Kapat", 176, 824, 40, Color.WHITE, true);
        text(c, "IR sinyali ile tüm TV’leri tek seferde kapatın.", 102, 874, 25, Color.rgb(145,157,179), false);

        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(Color.rgb(12,92,220));
        c.drawCircle(422, 1160, 184, p); c.drawCircle(422,1160,213,p);
        p.setStrokeWidth(14); p.setColor(Color.rgb(20,124,255));
        c.drawArc(new RectF(305,1043,539,1277), 45, 270, false, p);
        p.setStrokeWidth(12); p.setColor(Color.WHITE); c.drawLine(422,1028,422,1110,p);
        p.setStyle(Paint.Style.FILL);

        p.setColor(Color.rgb(11,19,34)); r.set(32,1510,812,1635); c.drawRoundRect(r,28,28,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(Color.rgb(38,58,93)); c.drawRoundRect(r,28,28,p);
        p.setStyle(Paint.Style.FILL);
        text(c, "◉", 71, 1587, 42, Color.rgb(16,119,255), false);
        text(c, "IR Modu " + (busy ? "Gönderiyor" : "Aktif"), 194, 1568, 23, Color.WHITE, true);
        p.setColor(Color.rgb(25,220,102)); c.drawCircle(385,1561,8,p);
        text(c, busy ? "Sinyaller gönderiliyor..." : status, 194, 1606, 19, Color.rgb(145,157,179), false);
        text(c, "V", 342, 1740, 28, Color.rgb(16,119,255), true);
        text(c, "ANTA", 374, 1740, 20, Color.rgb(150,160,178), false);
        c.restore();
    }

    private void text(Canvas c, String value, float x, float y, float size, int color, boolean bold) {
        p.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        p.setTextSize(size); p.setColor(color); p.setStyle(Paint.Style.FILL); c.drawText(value, x, y, p);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            float x = e.getX() / sx, y = e.getY() / sy;
            if (!busy && x > 110 && x < 735 && y > 940 && y < 1370) { action.run(); return true; }
        }
        return true;
    }
}
