package com.example.firstcourse.ui.dashboard;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.example.firstcourse.data.model.DeviceStatus;

import java.util.Locale;

public class AnimatedDashboardView extends View {

    private static final int COLOR_BG_DARK = Color.parseColor("#1A2236");
    private static final int COLOR_BG_LIGHT = Color.parseColor("#FDF7D6");
    private static final int COLOR_PANEL = Color.parseColor("#EAF0F6");
    private static final int COLOR_WATER = Color.parseColor("#66B7E8");
    private static final int COLOR_TANK = Color.parseColor("#1B1F28");

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rect = new RectF();
    private final Path path = new Path();

    private float targetLight;
    private float currentLight;
    private float targetTemp;
    private float currentTemp;
    private float targetHumidity;
    private float currentHumidity;
    private float targetFlow1;
    private float currentFlow1;
    private float targetFlow2;
    private float currentFlow2;
    private final float[] targetMoisture = new float[4];
    private final float[] currentMoisture = new float[4];

    private boolean pump1Active;
    private boolean pump2Active;

    private float fan1Angle;
    private float fan2Angle;
    private long lastFrameMs;

    private final ValueAnimator frameAnimator = ValueAnimator.ofFloat(0f, 1f);

    public AnimatedDashboardView(Context context) {
        super(context);
        init();
    }

    public AnimatedDashboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedDashboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(Color.parseColor("#172035"));
        textPaint.setTextAlign(Paint.Align.LEFT);

        accentPaint.setStyle(Paint.Style.FILL);

        frameAnimator.setDuration(1000L);
        frameAnimator.setRepeatCount(ValueAnimator.INFINITE);
        frameAnimator.setInterpolator(new LinearInterpolator());
        frameAnimator.addUpdateListener(animation -> updateFrame());
    }

    public void setStatus(DeviceStatus status) {
        if (status == null) {
            return;
        }

        targetLight = clamp(status.getLightIntensity(), 0f, 100f);
        targetTemp = (float) status.getTemperature();
        targetHumidity = clamp((float) status.getHumidity(), 0f, 100f);
        targetFlow1 = Math.max(0f, (float) status.getFlowSensor1());
        targetFlow2 = Math.max(0f, (float) status.getFlowSensor2());

        targetMoisture[0] = clamp((float) status.getMoisture1(), 0f, 100f);
        targetMoisture[1] = clamp((float) status.getMoisture2(), 0f, 100f);
        targetMoisture[2] = clamp((float) status.getMoisture3(), 0f, 100f);
        targetMoisture[3] = clamp((float) status.getMoisture4(), 0f, 100f);

        pump1Active = status.isPump1();
        pump2Active = status.isPump2();

        if (!frameAnimator.isRunning() && isAttachedToWindow()) {
            lastFrameMs = SystemClock.uptimeMillis();
            frameAnimator.start();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!frameAnimator.isRunning()) {
            lastFrameMs = SystemClock.uptimeMillis();
            frameAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (frameAnimator.isRunning()) {
            frameAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    private void updateFrame() {
        long now = SystemClock.uptimeMillis();
        float dt = (now - lastFrameMs) / 1000f;
        if (dt < 0f || dt > 0.08f) {
            dt = 0.016f;
        }
        lastFrameMs = now;

        currentLight = smooth(currentLight, targetLight, 0.10f);
        currentTemp = smooth(currentTemp, targetTemp, 0.10f);
        currentHumidity = smooth(currentHumidity, targetHumidity, 0.10f);
        currentFlow1 = smooth(currentFlow1, targetFlow1, 0.14f);
        currentFlow2 = smooth(currentFlow2, targetFlow2, 0.14f);

        for (int i = 0; i < currentMoisture.length; i++) {
            currentMoisture[i] = smooth(currentMoisture[i], targetMoisture[i], 0.12f);
        }

        fan1Angle = (fan1Angle + flowToDegreesPerSecond(currentFlow1) * dt) % 360f;
        fan2Angle = (fan2Angle + flowToDegreesPerSecond(currentFlow2) * dt) % 360f;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float lightFactor = clamp01(currentLight / 100f);

        canvas.drawColor(interpolateColor(COLOR_BG_DARK, COLOR_BG_LIGHT, lightFactor));
        drawBackgroundGrid(canvas, width, height, 28f);

        float leftPadding = dp(12f);
        float topPadding = dp(12f);
        float panelLeft = width * 0.58f;

        drawSun(canvas, leftPadding + dp(54f), topPadding + dp(42f), dp(28f), lightFactor);
        // Move the tank slightly down to avoid overlapping the sun
        float tankTopOffset = topPadding + dp(110f);
        drawTankAndFlow(canvas, leftPadding, tankTopOffset, panelLeft - leftPadding - dp(12f), height - tankTopOffset - dp(28f));
        // Ensure pumps are drawn lower and aligned with the pipeline targets
        float pumpBaseY = height - dp(72f);
        drawPumpUnits(canvas, leftPadding + dp(30f), pumpBaseY, pump1Active, pump2Active);
        drawTempHumidity(canvas, leftPadding + dp(8f), height - dp(18f));

        // Reduce moisture panel height so pumps' water spray does not overlap the bars
        float desiredMoistureBottom = height - dp(12f);
        float safeBottom = pumpBaseY - dp(48f); // leave space above pumps
        float moistureBottom = Math.min(desiredMoistureBottom, safeBottom);
        drawMoisturePanel(canvas, panelLeft, dp(12f), width - dp(12f), moistureBottom);
    }

    private void drawBackgroundGrid(Canvas canvas, float width, float height, float step) {
        strokePaint.setColor(Color.argb(28, 70, 80, 95));
        strokePaint.setStrokeWidth(dp(1f));
        for (float x = 0f; x <= width; x += step) {
            canvas.drawLine(x, 0f, x, height, strokePaint);
        }
        for (float y = 0f; y <= height; y += step) {
            canvas.drawLine(0f, y, width, y, strokePaint);
        }
    }

    private void drawSun(Canvas canvas, float cx, float cy, float radius, float lightFactor) {
        float ringRadius = radius + dp(16f);

        // vis controls visibility from 0 (dark) to 1 (bright). Use slight easing for nicer fade.
        float vis = clamp01(lightFactor);

        int baseAlpha = Math.max(24, (int) (255f * vis));

        strokePaint.setColor(Color.argb((int) (120f * vis), 255, 210, 40));
        strokePaint.setStrokeWidth(dp(3f));
        canvas.drawCircle(cx, cy, ringRadius, strokePaint);

        int fillB = (int) (100 + 80 * lightFactor);
        fillPaint.setColor(Color.argb(baseAlpha, 255, 230, fillB));
        canvas.drawCircle(cx, cy, radius, fillPaint);

        strokePaint.setColor(Color.argb((int) (200f * vis), 220, 170, 0));
        strokePaint.setStrokeWidth(dp(2f));
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            float x1 = cx + (float) Math.cos(angle) * (radius + dp(8f));
            float y1 = cy + (float) Math.sin(angle) * (radius + dp(8f));
            float x2 = cx + (float) Math.cos(angle) * (radius + dp(18f));
            float y2 = cy + (float) Math.sin(angle) * (radius + dp(18f));
            canvas.drawLine(x1, y1, x2, y2, strokePaint);
        }
    }

    private void drawTankAndFlow(Canvas canvas, float left, float top, float width, float height) {
        float tankHeight = height * 0.64f;

        rect.set(left, top, left + width * 0.55f, top + tankHeight);
        fillPaint.setColor(COLOR_TANK);
        canvas.drawRoundRect(rect, dp(12f), dp(12f), fillPaint);

        strokePaint.setColor(Color.argb(180, 0, 0, 0));
        strokePaint.setStrokeWidth(dp(2f));
        canvas.drawRoundRect(rect, dp(12f), dp(12f), strokePaint);

        float fanX = rect.right + dp(18f);
        float fan1Y = rect.top + tankHeight * 0.42f;
        float fan2Y = fan1Y + dp(42f);
        drawFan(canvas, fanX, fan1Y, dp(17f), fan1Angle, currentFlow1);
        drawFan(canvas, fanX, fan2Y, dp(17f), fan2Angle, currentFlow2);

        float outX = rect.right;
        float outY = rect.top + tankHeight * 0.56f;

        strokePaint.setColor(Color.argb(220, 140, 200, 235));
        strokePaint.setStrokeWidth(dp(8f));
        canvas.drawLine(outX, outY, fanX - dp(20f), fan1Y, strokePaint);
        canvas.drawLine(outX, outY + dp(24f), fanX - dp(20f), fan2Y, strokePaint);

        strokePaint.setColor(Color.argb(210, 110, 155, 190));
        strokePaint.setStrokeWidth(dp(2f));
        canvas.drawLine(outX, outY, fanX - dp(20f), fan1Y, strokePaint);
        canvas.drawLine(outX, outY + dp(24f), fanX - dp(20f), fan2Y, strokePaint);

        float pipelineStartX = fanX + dp(18f);
        float pipelineMidX = left + width * 0.86f;
        float pipelineEndX = pipelineMidX + dp(6f);
        // shift crops to the right and further down so water lands on them
        float cropShift = dp(28f);
        float outletX1 = pipelineEndX + cropShift;
        float outletX2 = pipelineEndX + dp(6f) + cropShift;

        strokePaint.setColor(Color.argb(220, 175, 220, 245));
        strokePaint.setStrokeWidth(dp(9f));

        // First pipeline from fan1
        float pipelineStartY1 = fan1Y;
        float pipelineEndY1 = pipelineStartY1 + dp(72f);
        path.reset();
        path.moveTo(pipelineStartX, pipelineStartY1);
        path.cubicTo(pipelineMidX, pipelineStartY1 + dp(6f), pipelineMidX, pipelineEndY1 - dp(10f), outletX1, pipelineEndY1);
        canvas.drawPath(path, strokePaint);
        strokePaint.setColor(Color.argb(200, 90, 135, 165));
        strokePaint.setStrokeWidth(dp(2f));
        canvas.drawPath(path, strokePaint);
        drawWaterSpray(canvas, outletX1, pipelineEndY1 + dp(6f), pump1Active);
        drawCrop(canvas, outletX1, pipelineEndY1 + dp(28f), 1f);

        // Second pipeline from fan2
        strokePaint.setColor(Color.argb(220, 175, 220, 245));
        strokePaint.setStrokeWidth(dp(9f));
        float pipelineStartY2 = fan2Y;
        float pipelineEndY2 = pipelineStartY2 + dp(72f);
        path.reset();
        path.moveTo(pipelineStartX, pipelineStartY2);
        path.cubicTo(pipelineMidX, pipelineStartY2 + dp(6f), pipelineMidX, pipelineEndY2 - dp(10f), outletX2, pipelineEndY2);
        canvas.drawPath(path, strokePaint);
        strokePaint.setColor(Color.argb(200, 90, 135, 165));
        strokePaint.setStrokeWidth(dp(2f));
        canvas.drawPath(path, strokePaint);
        drawWaterSpray(canvas, outletX2, pipelineEndY2 + dp(6f), pump2Active);
        drawCrop(canvas, outletX2, pipelineEndY2 + dp(28f), 1f);
    }

    private void drawFan(Canvas canvas, float cx, float cy, float radius, float angleDeg, float flow) {
        fillPaint.setColor(Color.argb(220, 233, 240, 246));
        canvas.drawCircle(cx, cy, radius, fillPaint);

        strokePaint.setColor(Color.argb(200, 120, 130, 145));
        strokePaint.setStrokeWidth(dp(2f));
        canvas.drawCircle(cx, cy, radius, strokePaint);

        float bladeLen = radius * 0.75f;
        strokePaint.setColor(Color.argb(220, 90, 105, 120));
        strokePaint.setStrokeWidth(dp(2.2f));

        canvas.save();
        canvas.rotate(angleDeg, cx, cy);
        for (int i = 0; i < 3; i++) {
            canvas.drawLine(cx, cy, cx + bladeLen, cy, strokePaint);
            canvas.rotate(120f, cx, cy);
        }
        canvas.restore();

        float flowNorm = clamp01(flow / 8f);
        fillPaint.setColor(Color.argb((int) (80 + 120 * flowNorm), 70, 150, 200));
        canvas.drawCircle(cx, cy, dp(3f), fillPaint);
    }

    private void drawWaterSpray(Canvas canvas, float originX, float originY, boolean active) {
        if (!active) {
            return;
        }

        float t = (SystemClock.uptimeMillis() % 900L) / 900f;

        strokePaint.setColor(Color.argb(210, 95, 175, 230));
        strokePaint.setStrokeWidth(dp(2.5f));

        // Emit droplets that travel mostly downward; small horizontal jitter for realism
        for (int i = 0; i < 6; i++) {
            float phase = (t + i * 0.14f) % 1f;
            float fall = dp(60f) * phase; // how far the drop has fallen
            float jitterX = (float) Math.sin((phase + i * 0.9f) * Math.PI * 2.0f) * dp(6f);
            float rad = dp(2f) * (1f - phase * 0.6f);
            canvas.drawCircle(originX + jitterX, originY + fall, rad, strokePaint);
        }
    }

    private void drawPumpUnits(Canvas canvas, float startX, float centerY, boolean pump1On, boolean pump2On) {
        drawSinglePump(canvas, startX, centerY, pump1On, 0f);
        drawSinglePump(canvas, startX + dp(60f), centerY, pump2On, 0.6f);
    }

    private void drawSinglePump(Canvas canvas, float cx, float cy, boolean active, float phaseOffset) {
        float pulse = active
                ? 1f + 0.12f * (float) Math.sin((SystemClock.uptimeMillis() / 220.0) + phaseOffset)
                : 1f;

        float radius = dp(16f) * pulse;
        fillPaint.setColor(active ? Color.argb(230, 92, 182, 236) : Color.argb(185, 165, 175, 182));
        canvas.drawCircle(cx, cy, radius, fillPaint);

        strokePaint.setColor(Color.argb(180, 75, 84, 92));
        strokePaint.setStrokeWidth(dp(2f));
        canvas.drawCircle(cx, cy, radius, strokePaint);

        fillPaint.setColor(Color.argb(active ? 240 : 170, 240, 246, 250));
        rect.set(cx - dp(4f), cy - dp(4f), cx + dp(4f), cy + dp(4f));
        canvas.drawOval(rect, fillPaint);
    }

    private void drawTempHumidity(Canvas canvas, float x, float y) {
        textPaint.setTextSize(dp(12f));
        textPaint.setColor(Color.argb(225, 24, 34, 54));
        String info = String.format(Locale.getDefault(), "T %.1f C   H %.1f%%", currentTemp, currentHumidity);
        canvas.drawText(info, x, y, textPaint);
    }

    private void drawMoisturePanel(Canvas canvas, float left, float top, float right, float bottom) {
        rect.set(left, top, right, bottom);
        fillPaint.setColor(Color.argb(170, 242, 247, 252));
        canvas.drawRoundRect(rect, dp(10f), dp(10f), fillPaint);

        strokePaint.setColor(Color.argb(120, 125, 145, 165));
        strokePaint.setStrokeWidth(dp(1.5f));
        canvas.drawRoundRect(rect, dp(10f), dp(10f), strokePaint);

        float titleY = top + dp(20f);
        textPaint.setTextSize(dp(11f));
        textPaint.setColor(Color.argb(220, 40, 50, 65));
        canvas.drawText("MOISTURE", left + dp(12f), titleY, textPaint);

        float barAreaTop = top + dp(34f);
        float barAreaBottom = bottom - dp(22f);
        float barCount = 4f;
        float gap = dp(14f);
        float availableWidth = (right - left) - dp(24f) - gap * (barCount - 1f);
        float barWidth = availableWidth / barCount;

        for (int i = 0; i < 4; i++) {
            float barLeft = left + dp(12f) + i * (barWidth + gap);
            float barRight = barLeft + barWidth;

            rect.set(barLeft, barAreaTop, barRight, barAreaBottom);
            fillPaint.setColor(Color.argb(130, 223, 228, 234));
            canvas.drawRoundRect(rect, dp(6f), dp(6f), fillPaint);

            float moisture = clamp(currentMoisture[i], 0f, 100f);
            float fillHeight = (barAreaBottom - barAreaTop) * (moisture / 100f);
            float fillTop = barAreaBottom - fillHeight;

            int wetColor = interpolateColor(Color.parseColor("#8A6D3B"), Color.parseColor("#4FAE63"), moisture / 100f);
            rect.set(barLeft, fillTop, barRight, barAreaBottom);
            fillPaint.setColor(wetColor);
            canvas.drawRoundRect(rect, dp(6f), dp(6f), fillPaint);

            textPaint.setTextSize(dp(10f));
            textPaint.setColor(Color.argb(230, 35, 42, 56));
            canvas.drawText("M" + (i + 1), barLeft, barAreaBottom + dp(14f), textPaint);
            canvas.drawText(String.format(Locale.getDefault(), "%.0f%%", moisture), barLeft, barAreaTop - dp(6f), textPaint);
        }
    }

    private float flowToDegreesPerSecond(float flow) {
        float clampedFlow = clamp(flow, 0f, 12f);
        float rpm = 12f + clampedFlow * 18f;
        return rpm * 6f;
    }

    private void drawCrop(Canvas canvas, float cx, float groundY, float scale) {
        // groundY is where the crop base should be placed (slightly below spray)
        float stemHeight = dp(18f) * scale;
        float stemTopY = groundY - stemHeight;

        // Draw soil patch
        rect.set(cx - dp(14f) * scale, groundY, cx + dp(14f) * scale, groundY + dp(8f) * scale);
        fillPaint.setColor(Color.argb(200, 85, 65, 45));
        canvas.drawRoundRect(rect, dp(6f) * scale, dp(6f) * scale, fillPaint);

        // Stem
        strokePaint.setColor(Color.argb(220, 40, 120, 60));
        strokePaint.setStrokeWidth(dp(2f) * scale);
        canvas.drawLine(cx, groundY, cx, stemTopY, strokePaint);

        // Leaves
        fillPaint.setColor(Color.argb(220, 86, 153, 74));
        path.reset();
        path.moveTo(cx, stemTopY + dp(6f) * scale);
        path.quadTo(cx - dp(10f) * scale, stemTopY + dp(4f) * scale, cx - dp(12f) * scale, stemTopY + dp(12f) * scale);
        path.quadTo(cx - dp(6f) * scale, stemTopY + dp(16f) * scale, cx, stemTopY + dp(12f) * scale);
        canvas.drawPath(path, fillPaint);

        path.reset();
        path.moveTo(cx, stemTopY + dp(6f) * scale);
        path.quadTo(cx + dp(10f) * scale, stemTopY + dp(4f) * scale, cx + dp(12f) * scale, stemTopY + dp(12f) * scale);
        path.quadTo(cx + dp(6f) * scale, stemTopY + dp(16f) * scale, cx, stemTopY + dp(12f) * scale);
        canvas.drawPath(path, fillPaint);

        // Tomato fruit
        fillPaint.setColor(Color.argb(230, 220, 60, 70));
        canvas.drawCircle(cx, stemTopY + dp(6f) * scale, dp(6f) * scale, fillPaint);
        strokePaint.setColor(Color.argb(200, 140, 30, 40));
        strokePaint.setStrokeWidth(dp(1f) * scale);
        canvas.drawCircle(cx, stemTopY + dp(6f) * scale, dp(6f) * scale, strokePaint);
    }

    private float smooth(float current, float target, float factor) {
        return current + (target - current) * factor;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float clamp01(float value) {
        return clamp(value, 0f, 1f);
    }

    private int interpolateColor(int start, int end, float amount) {
        float t = clamp01(amount);

        int a = (int) (Color.alpha(start) + (Color.alpha(end) - Color.alpha(start)) * t);
        int r = (int) (Color.red(start) + (Color.red(end) - Color.red(start)) * t);
        int g = (int) (Color.green(start) + (Color.green(end) - Color.green(start)) * t);
        int b = (int) (Color.blue(start) + (Color.blue(end) - Color.blue(start)) * t);

        return Color.argb(a, r, g, b);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
