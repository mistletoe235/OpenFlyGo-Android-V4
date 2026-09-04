package edu.playground.djivln.mini2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Four-bar air-link indicator matching the compact V5 HUD. */
public final class SignalQualityView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bar = new RectF();
    private int level = -1;

    public SignalQualityView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setContentDescription(getContext().getString(R.string.signal_quality_unknown));
    }

    public void setLevel(int percent) {
        int next = Math.max(-1, Math.min(100, percent));
        if (next == level) return;
        level = next;
        setContentDescription(level >= 0
                ? getContext().getString(R.string.signal_quality_percent, level)
                : getContext().getString(R.string.signal_quality_unknown));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        float width = getWidth(), height = getHeight();
        float gap = width * 0.08f;
        float barWidth = (width - gap * 5f) / 4f;
        float bottom = height * 0.86f;
        int active = level < 0 ? 0 : level < 40 ? 1 : level <= 60 ? 2 : level <= 80 ? 3 : 4;
        int color = level < 40 ? 0xFFFF8080 : level <= 60 ? 0xFFF2A33C : 0xFF82E6B4;
        for (int index = 0; index < 4; index++) {
            float barHeight = height * (0.24f + index * 0.17f);
            float left = gap + index * (barWidth + gap);
            bar.set(left, bottom - barHeight, left + barWidth, bottom);
            paint.setColor(index < active ? color : 0x33FFFFFF);
            canvas.drawRoundRect(bar, barWidth * 0.3f, barWidth * 0.3f, paint);
        }
    }
}
