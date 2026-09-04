package edu.playground.djivln.mini2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Compact phone-style RC battery with the percentage rendered inside. */
public final class RemoteControllerBatteryView extends View {
    private final Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF body = new RectF();
    private final RectF fill = new RectF();
    private final RectF terminal = new RectF();
    private int level = -1;

    public RemoteControllerBatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        outline.setStyle(Paint.Style.STROKE);
        outline.setStrokeCap(Paint.Cap.ROUND);
        outline.setStrokeJoin(Paint.Join.ROUND);
        outline.setColor(0x88FFFFFF);
        textPaint.setColor(0xF2FFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        setContentDescription(getContext().getString(R.string.rc_battery_unknown));
    }

    public void setLevel(int percent) {
        int next = Math.max(-1, Math.min(100, percent));
        if (next == level) return;
        level = next;
        setContentDescription(level >= 0
                ? getContext().getString(R.string.rc_battery_percent, level)
                : getContext().getString(R.string.rc_battery_unknown));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        float stroke = Math.max(1.5f, height * 0.075f);
        float terminalWidth = Math.max(2.5f, width * 0.065f);
        outline.setStrokeWidth(stroke);
        body.set(stroke, stroke, width - terminalWidth - stroke * 1.6f, height - stroke);
        float radius = height * 0.18f;
        canvas.drawRoundRect(body, radius, radius, outline);
        terminal.set(body.right + stroke * 1.1f, height * 0.34f,
                width - stroke * 0.25f, height * 0.66f);
        fillPaint.setColor(0x88FFFFFF);
        canvas.drawRoundRect(terminal, stroke, stroke, fillPaint);
        if (level >= 0) {
            fillPaint.setColor(level > 50 ? 0xFF55D99A : level > 20 ? 0xFFF2A33C : 0xFFFF6262);
            float inset = stroke * 1.7f;
            float available = body.width() - inset * 2f;
            fill.set(body.left + inset, body.top + inset,
                    body.left + inset + available * level / 100f, body.bottom - inset);
            if (fill.width() > 0f) canvas.drawRoundRect(fill, radius * 0.55f,
                    radius * 0.55f, fillPaint);
        }
        textPaint.setTextSize(height * 0.40f);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(level >= 0 ? String.valueOf(level) : "--",
                (body.left + body.right) / 2f,
                height / 2f - (metrics.ascent + metrics.descent) / 2f, textPaint);
    }
}
