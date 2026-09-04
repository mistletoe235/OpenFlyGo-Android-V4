package edu.playground.djivln.mini2

internal object UAVFlowPolicyContract {
    const val DEFAULT_STOP_THRESHOLD = 0.7
    const val HORIZON = 10
    const val DEFAULT_EXECUTED_PREFIX = 5
    const val ACTION_DIMENSION = 5

    data class Pose(
        /** UAVFlow training/evaluation stores episode-local translation in centimeters. */
        val x: Double,
        val y: Double,
        val z: Double,
        val yawDegrees: Double,
    ) {
        fun toFloatArray() = floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat(), yawDegrees.toFloat())
    }

    data class Action(
        val forwardMeters: Double,
        val rightMeters: Double,
        val upMeters: Double,
        val yawDegrees: Double,
        val stopScore: Double,
    ) {
        fun toDoubleArray() = doubleArrayOf(
            forwardMeters,
            rightMeters,
            upMeters,
            yawDegrees,
            stopScore,
        )
    }

    private val actionMean = doubleArrayOf(
        1.6570024490,
        -0.0083255153,
        -0.0117614884,
        -0.0025964733,
    )
    private val actionStandardDeviation = doubleArrayOf(
        7.7519412041,
        4.5435113907,
        1.9959052801,
        0.0884739831,
    )

    fun decode(normalizedTrajectory: FloatArray): List<Action> {
        require(normalizedTrajectory.size == HORIZON * ACTION_DIMENSION) {
            "UAVFlow output must contain H10x5 values"
        }
        require(normalizedTrajectory.all(Float::isFinite)) { "UAVFlow output contains non-finite values" }
        return List(HORIZON) { step ->
            val offset = step * ACTION_DIMENSION
            val physical = DoubleArray(4) { channel ->
                actionMean[channel] + actionStandardDeviation[channel] * normalizedTrajectory[offset + channel]
            }
            Action(
                forwardMeters = physical[0] / CENTIMETERS_PER_METER,
                rightMeters = physical[1] / CENTIMETERS_PER_METER,
                upMeters = physical[2] / CENTIMETERS_PER_METER,
                yawDegrees = Math.toDegrees(physical[3]),
                stopScore = normalizedTrajectory[offset + 4].toDouble(),
            ).also { action ->
                require(action.toDoubleArray().all(Double::isFinite)) {
                    "UAVFlow denormalized action contains non-finite values"
                }
            }
        }
    }

    fun shouldStop(score: Double, threshold: Double = DEFAULT_STOP_THRESHOLD): Boolean =
        score.isFinite() && threshold.isFinite() && score >= threshold

    /**
     * Return at most the selected H1-H10 prefix. A crossing row is retained only as a stop
     * sentinel; the controller checks it before issuing any motion command.
     */
    fun executionRows(
        actions: List<Action>,
        executedPrefix: Int = DEFAULT_EXECUTED_PREFIX,
    ): List<Action> {
        require(actions.size == HORIZON) { "UAVFlow execution planning requires H10" }
        require(executedPrefix in 1..HORIZON) { "UAVFlow executed prefix must be in H1..H10" }
        val stopIndex = actions.indexOfFirst { shouldStop(it.stopScore) }.takeIf { it >= 0 }
        return actions.take(minOf(executedPrefix, stopIndex?.plus(1) ?: HORIZON))
    }

    fun wrapDegrees(value: Double): Double {
        var wrapped = (value + 540.0) % 360.0 - 180.0
        if (wrapped < -180.0) wrapped += 360.0
        return wrapped
    }

    const val CENTIMETERS_PER_METER = 100.0
}
