package edu.playground.djivln.survey

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import edu.playground.djivln.mini2.R

/** Compact, map-oriented DSM alignment preview; it never controls flight. */
class TerrainPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var info: TerrainRasterInfo? = null
    private var grid: DoubleArray? = null
    private var columns = 0
    private var rows = 0
    private var mission: SurveyMission? = null

    fun showTerrain(info: TerrainRasterInfo, grid: DoubleArray, columns: Int, rows: Int) {
        require(grid.size == columns * rows)
        this.info = info
        this.grid = grid
        this.columns = columns
        this.rows = rows
        invalidate()
    }

    fun showMission(mission: SurveyMission?) {
        this.mission = mission
        invalidate()
    }

    fun clearTerrain() {
        info = null
        grid = null
        columns = 0
        rows = 0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(25, 29, 34))
        val values = grid ?: return drawEmpty(canvas)
        val valid = values.filter { it.isFinite() }
        if (valid.isEmpty()) return drawEmpty(canvas)
        val min = valid.min()
        val max = valid.max()
        val mapBottom = height.toFloat()
        val cellW = width.toFloat() / columns
        val cellH = mapBottom / rows
        paint.style = Paint.Style.FILL
        for (row in 0 until rows) for (column in 0 until columns) {
            val value = values[row * columns + column]
            paint.color = if (!value.isFinite()) Color.rgb(70, 30, 35) else terrainColor(
                if (max <= min) 0.5 else (value - min) / (max - min),
            )
            canvas.drawRect(column * cellW, row * cellH,
                (column + 1) * cellW + 1, (row + 1) * cellH + 1, paint)
        }
        drawMapOverlays(canvas, mapBottom)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(190, 15, 18, 22)
        canvas.drawRoundRect(5f, 3f, 245f, 33f, 6f, 6f, paint)
        paint.color = Color.WHITE
        paint.textSize = 22f
        val surfaceLabel = if (info?.displayName?.contains("DEM", ignoreCase = true) == true) {
            context.getString(R.string.terrain_label_dem)
        } else {
            context.getString(R.string.terrain_label_dsm)
        }
        canvas.drawText("$surfaceLabel ${"%.1f".format(min)}–${"%.1f".format(max)} m", 10f, 25f, paint)
        drawLegend(canvas, min, max)
    }

    private fun drawMapOverlays(canvas: Canvas, mapBottom: Float) {
        val raster = info ?: return
        val current = mission ?: return
        fun x(longitude: Double) = ((longitude - raster.minimumLongitude) /
            (raster.maximumLongitude - raster.minimumLongitude) * width).toFloat()
        fun y(latitude: Double) = ((raster.maximumLatitude - latitude) /
            (raster.maximumLatitude - raster.minimumLatitude) * mapBottom).toFloat()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.WHITE
        val roi = Path()
        current.roi.forEachIndexed { index, point ->
            if (index == 0) roi.moveTo(x(point.longitude), y(point.latitude))
            else roi.lineTo(x(point.longitude), y(point.latitude))
        }
        roi.close()
        canvas.drawPath(roi, paint)
        paint.strokeWidth = 2f
        paint.color = Color.CYAN
        current.surveyPasses().forEach { pass ->
            val path = Path()
            pass.waypoints.forEachIndexed { index, waypoint ->
                val px = x(waypoint.point.longitude)
                val py = y(waypoint.point.latitude)
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawLegend(canvas: Canvas, min: Double, max: Double) {
        paint.style = Paint.Style.FILL
        val left = 10f
        val right = width - 10f
        val top = height - 27f
        val bottom = height - 9f
        paint.color = Color.argb(190, 15, 18, 22)
        canvas.drawRoundRect(left - 5f, top - 16f, right + 5f, bottom + 5f, 6f, 6f, paint)
        val steps = 48
        for (step in 0 until steps) {
            paint.color = terrainColor(step.toDouble() / (steps - 1))
            val x0 = left + (right - left) * step / steps
            val x1 = left + (right - left) * (step + 1) / steps
            canvas.drawRect(x0, top, x1 + 1f, bottom, paint)
        }
        paint.color = Color.WHITE
        paint.textSize = 16f
        canvas.drawText(context.getString(R.string.terrain_low_meters, min), left, top - 2f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.terrain_high_meters, max), right, top - 2f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawEmpty(canvas: Canvas) {
        paint.color = Color.LTGRAY
        paint.textSize = 24f
        canvas.drawText(context.getString(R.string.terrain_preview_import_hint), 16f, height / 2f, paint)
    }

    private fun terrainColor(value: Double): Int {
        val t = value.coerceIn(0.0, 1.0).toFloat()
        return Color.HSVToColor(floatArrayOf(220f - 220f * t, 0.75f, 0.95f))
    }
}
