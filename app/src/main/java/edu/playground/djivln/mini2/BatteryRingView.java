package edu.playground.djivln.mini2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** DJI Fly 风格的环形电量表：彩色圆环扫过角度 + 中心百分比数字。 */
public class BatteryRingView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private int level = -1;

    public BatteryRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(0x33FFFFFF);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void setLevel(int percent) {
        int clamped = Math.max(-1, Math.min(100, percent));
        if (clamped == level) return;
        level = clamped;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        float size = Math.min(getWidth(), getHeight());
        float stroke = size * 0.11f;
        trackPaint.setStrokeWidth(stroke);
        ringPaint.setStrokeWidth(stroke);
        arcBounds.set(stroke, stroke, size - stroke, size - stroke);
        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint);
        if (level >= 0) {
            ringPaint.setColor(level > 50 ? 0xFF82E6B4 : level > 20 ? 0xFFF2A33C : 0xFFFF8080);
            canvas.drawArc(arcBounds, -90f, level * 3.6f, false, ringPaint);
        }
        textPaint.setTextSize(size * 0.28f);
        String text = level >= 0 ? String.valueOf(level) : "--";
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(text, size / 2f, size / 2f - (metrics.ascent + metrics.descent) / 2f, textPaint);
    }
}
