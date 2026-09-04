package edu.playground.djivln.mini2

data class OpenFlyRelativeAction(
    val forwardMeters: Double,
    val rightMeters: Double,
    val upMeters: Double,
    val yawDegrees: Double,
    val stopScore: Double,
) {
    fun shouldStop(threshold: Double): Boolean = UAVFlowPolicyContract.shouldStop(stopScore, threshold)
}

object OpenFlyActionSemantics {
    /** Legacy OpenFly response: model [x,y,z,stop] -> body FRU [x,-y,z]. */
    @JvmStatic
    fun map(x: Double, y: Double, z: Double, stop: Double): OpenFlyRelativeAction {
        require(listOf(x, y, z, stop).all(Double::isFinite)) { "non-finite OpenFly action" }
        return OpenFlyRelativeAction(x, -y, z, 0.0, stop)
    }

    /** StarVLA UAVFlow already outputs body FRU deltas and an explicit dyaw. */
    @JvmStatic
    fun mapUavFlow(x: Double, y: Double, z: Double, yawDegrees: Double, stop: Double): OpenFlyRelativeAction {
        require(listOf(x, y, z, yawDegrees, stop).all(Double::isFinite)) { "non-finite UAVFlow action" }
        return OpenFlyRelativeAction(x, y, z, yawDegrees, stop)
    }
}
