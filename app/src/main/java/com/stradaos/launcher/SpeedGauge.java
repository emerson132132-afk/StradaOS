package com.stradaos.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Velocímetro de arco con marcas (estilo de la referencia). Solo dibuja; el número lo pone Main. */
public class SpeedGauge extends View {
    private static final float START = 135f;   // grados: abajo a la izquierda
    private static final float SWEEP = 270f;   // el arco queda abierto por abajo
    private static final int TICKS = 46;       // marcas (la última cae justo en el final del arco)

    private final float s;
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint prog = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();
    private float max = 220f;
    private float target, shown;

    public SpeedGauge(Context c, float scale) {
        super(c);
        s = scale;
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeCap(Paint.Cap.ROUND);
        prog.setStyle(Paint.Style.STROKE);
        prog.setStrokeCap(Paint.Cap.ROUND);
        tick.setStyle(Paint.Style.STROKE);
        tick.setStrokeCap(Paint.Cap.ROUND);
    }

    /** Velocidad que se muestra y escala máxima (220 km/h o 140 mph). */
    public void setValue(float v, float maxValue) {
        max = Math.max(1f, maxValue);
        target = Math.max(0f, Math.min(v, max));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float cx = w / 2f, cy = h / 2f;
        float r = Math.min(w, h) / 2f - 10 * s;
        if (r <= 0) return;

        // el número sube y baja con suavidad en vez de dar saltos
        float diff = target - shown;
        if (Math.abs(diff) < 0.4f) shown = target; else shown += diff * 0.22f;
        float frac = shown / max;

        // arco de fondo y arco de progreso
        float ra = r - 14 * s;
        box.set(cx - ra, cy - ra, cx + ra, cy + ra);
        track.setColor(Main.BTN);
        track.setStrokeWidth(5 * s);
        c.drawArc(box, START, SWEEP, false, track);
        prog.setColor(Main.RED);
        prog.setStrokeWidth(5 * s);
        if (frac > 0.004f) c.drawArc(box, START, SWEEP * frac, false, prog);

        // marcas hacia afuera del arco; las ya alcanzadas se encienden con el acento
        for (int i = 0; i <= TICKS; i++) {
            float f = i / (float) TICKS;
            double a = Math.toRadians(START + SWEEP * f);
            boolean major = i % 5 == 0;
            float r1 = r - (major ? 9 : 5) * s;
            float r2 = r;
            tick.setStrokeWidth((major ? 2.4f : 1.6f) * s);
            tick.setColor(f <= frac ? Main.RED : Main.MUTED);
            tick.setAlpha(f <= frac ? 255 : (major ? 150 : 90));
            c.drawLine(cx + (float) Math.cos(a) * r1, cy + (float) Math.sin(a) * r1,
                       cx + (float) Math.cos(a) * r2, cy + (float) Math.sin(a) * r2, tick);
        }

        if (Math.abs(target - shown) >= 0.4f) postInvalidateDelayed(16);
    }
}
