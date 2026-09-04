package edu.playground.djivln.mini2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import edu.playground.djivln.mini2.R

internal data class FlightEnduranceBarState(
    val connected: Boolean,
    val chargePercent: Int,
    val goHomePercent: Int?,
    val landPercent: Int?,
    val remainingSeconds: Int?,
) {
    val timeLabel: String get() = FlightEnduranceBarPolicy.formatTime(remainingSeconds)
}

internal object FlightEnduranceBarPolicy {
    fun from(snapshot: Mini2AircraftBridge.Snapshot) = FlightEnduranceBarState(
        connected = snapshot.connected,
        chargePercent = snapshot.aircraftBattery.validPercent() ?: 0,
        goHomePercent = snapshot.batteryNeededToGoHomePercent.validPercent(),
        landPercent = snapshot.batteryNeededToLandPercent.validPercent(),
        remainingSeconds = snapshot.remainingFlightTimeSeconds.takeIf { it >= 0 },
    )

    fun formatTime(seconds: Int?): String {
        val value = seconds?.takeIf { it > 0 } ?: return "--:--"
        val hours = value / 3_600
        val minutes = value % 3_600 / 60
        val rest = value % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, rest)
        else "%02d:%02d".format(minutes, rest)
    }

    private fun Int?.validPercent(): Int? = this?.takeIf { it in 0..100 }
}

/** Compact DJI-style remaining-flight-time scale backed only by normalized MSDK4 telemetry. */
class FlightEnduranceBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val density = resources.displayMetrics.density
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        strokeWidth = dp(5f)
    }
    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(26, 31, 36)
        textAlign = Paint.Align.CENTER
        textSize = dp(10f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private var state = FlightEnduranceBarState(false, 0, null, null, null)

    fun render(snapshot: Mini2AircraftBridge.Snapshot) {
        val next = FlightEnduranceBarPolicy.from(snapshot)
        if (next == state && visibility == if (next.connected) VISIBLE else GONE) return
        state = next
        visibility = if (next.connected) VISIBLE else GONE
        contentDescription = buildString {
            append(context.getString(R.string.endurance_remaining_time, next.timeLabel))
            append(context.getString(R.string.endurance_battery, next.chargePercent))
            next.goHomePercent?.let { append(context.getString(R.string.endurance_rth_needed, it)) }
            next.landPercent?.let { append(context.getString(R.string.endurance_landing_needed, it)) }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!state.connected || width <= 0 || height <= 0) return
        val left = dp(4f)
        val right = width - dp(4f)
        val y = height / 2f
        val usable = (right - left).coerceAtLeast(1f)
        fun x(percent: Int) = left + usable * percent.coerceIn(0, 100) / 100f
        barPaint.color = Color.argb(145, 208, 216, 224)
        canvas.drawLine(left, y, right, y, barPaint)
        val charge = state.chargePercent
        val land = state.landPercent?.coerceAtMost(charge) ?: 0
        val home = state.goHomePercent?.coerceIn(land, charge) ?: land
        drawSegment(canvas, left, x(land), y, Color.rgb(244, 67, 54))
        drawSegment(canvas, x(land), x(home), y, Color.rgb(255, 193, 7))
        drawSegment(canvas, x(home), x(charge), y, Color.rgb(42, 201, 108))
        state.goHomePercent?.let { percent ->
            val markerX = x(percent)
            markerPaint.color = Color.WHITE
            canvas.drawCircle(markerX, y, dp(7f), markerPaint)
            markerPaint.color = Color.rgb(38, 43, 48)
            markerPaint.textSize = dp(8f)
            canvas.drawText("H", markerX, y + dp(2.8f), markerPaint)
        }
        val label = state.timeLabel
        val pillWidth = (textPaint.measureText(label) + dp(14f)).coerceAtLeast(dp(48f))
        val center = x(charge).coerceIn(left + pillWidth / 2f, right - pillWidth / 2f)
        markerPaint.color = Color.WHITE
        canvas.drawRoundRect(RectF(center - pillWidth / 2f, y - dp(9f),
            center + pillWidth / 2f, y + dp(9f)), dp(9f), dp(9f), markerPaint)
        canvas.drawText(label, center, y + dp(3.5f), textPaint)
    }

    private fun drawSegment(canvas: Canvas, start: Float, end: Float, y: Float, color: Int) {
        if (end <= start) return
        barPaint.color = color
        canvas.drawLine(start, y, end, y, barPaint)
    }

    private fun dp(value: Float) = value * density
}
