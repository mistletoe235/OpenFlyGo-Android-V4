package edu.playground.djivln.mini2

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface

internal class PhoneHeadingSource(
    private val activity: Activity,
    private val listener: Listener,
) : SensorEventListener, AutoCloseable {
    fun interface Listener { fun onHeadingChanged(heading: Heading) }
    data class Heading(val degrees: Double, val accuracy: Int, val timestampNanos: Long)

    private val manager = activity.applicationContext.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    private val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: manager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    private val rotation = FloatArray(9)
    private val remapped = FloatArray(9)
    private val orientation = FloatArray(3)
    private var running = false
    private var lastPublishedNanos = 0L
    private var filtered = Double.NaN

    fun start(): Boolean {
        if (running) return sensor != null
        val source = sensor ?: return false
        running = manager.registerListener(this, source, SensorManager.SENSOR_DELAY_GAME, 0)
        return running
    }

    fun stop() {
        if (running) manager.unregisterListener(this)
        running = false
        lastPublishedNanos = 0L
        filtered = Double.NaN
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != sensor?.type ||
            lastPublishedNanos > 0L && event.timestamp - lastPublishedNanos < MIN_INTERVAL_NANOS) return
        val heading = runCatching {
            SensorManager.getRotationMatrixFromVector(rotation, event.values)
            val axes = screenAxes(displayRotation())
            check(SensorManager.remapCoordinateSystem(rotation, axes.first, axes.second, remapped))
            SensorManager.getOrientation(remapped, orientation)
            normalize(Math.toDegrees(orientation[0].toDouble()))
        }.getOrNull() ?: return
        filtered = smooth(filtered, heading)
        lastPublishedNanos = event.timestamp
        listener.onHeadingChanged(Heading(filtered, event.accuracy, event.timestamp))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun close() = stop()

    @Suppress("DEPRECATION")
    private fun displayRotation(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.display?.rotation ?: Surface.ROTATION_0
    } else {
        activity.windowManager.defaultDisplay.rotation
    }

    companion object {
        private const val MIN_INTERVAL_NANOS = 50_000_000L
        private const val FILTER_ALPHA = 0.22
        const val MAX_HEADING_AGE_NANOS = 2_000_000_000L

        @JvmStatic fun isDisplayable(heading: Heading?, nowNanos: Long): Boolean = heading != null &&
            heading.degrees.isFinite() && nowNanos >= heading.timestampNanos &&
            nowNanos - heading.timestampNanos <= MAX_HEADING_AGE_NANOS

        fun normalize(value: Double): Double = (value % 360.0 + 360.0) % 360.0
        fun smooth(previous: Double, current: Double): Double {
            if (!previous.isFinite()) return normalize(current)
            val delta = (current - previous + 540.0) % 360.0 - 180.0
            return normalize(previous + delta * FILTER_ALPHA)
        }

        private fun screenAxes(rotation: Int): Pair<Int, Int> = when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
    }
}
