package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UAVFlowPolicyContractTest {
    @Test
    fun stopThresholdIsPointSevenAndInclusive() {
        assertFalse(UAVFlowPolicyContract.shouldStop(0.699999))
        assertTrue(UAVFlowPolicyContract.shouldStop(0.7))
        assertTrue(UAVFlowPolicyContract.shouldStop(0.9))
        assertFalse(UAVFlowPolicyContract.shouldStop(Double.NaN))
        assertFalse(UAVFlowPolicyContract.shouldStop(0.7, 0.8))
        assertTrue(UAVFlowPolicyContract.shouldStop(0.8, 0.8))
    }

    @Test
    fun decodesCentimeterTrainingActionsAsMeterFlightCommandsAndRadiansForYaw() {
        val normalized = FloatArray(50)
        normalized[0] = 1f
        normalized[1] = -1f
        normalized[2] = 0.5f
        normalized[3] = 1f
        normalized[4] = 0.73f
        val decoded = UAVFlowPolicyContract.decode(normalized)
        assertEquals(10, decoded.size)
        val first = decoded.first()
        assertEquals(0.094089436531, first.forwardMeters, 1e-7)
        assertEquals(-0.045518369060, first.rightMeters, 1e-7)
        assertEquals(0.0098619115165, first.upMeters, 1e-7)
        assertEquals(4.9204188666, first.yawDegrees, 1e-5)
        assertEquals(0.73, first.stopScore, 1e-6)
    }

    @Test
    fun executesFiveRowsWhenH10HasNoStopCrossing() {
        val actions = actionsWithStops(DoubleArray(10) { 0.1 })
        assertEquals(5, UAVFlowPolicyContract.executionRows(actions).size)
    }

    @Test
    fun executesSelectablePrefixFromH1ThroughH10() {
        val actions = actionsWithStops(DoubleArray(10) { 0.1 })
        assertEquals(1, UAVFlowPolicyContract.executionRows(actions, 1).size)
        assertEquals(7, UAVFlowPolicyContract.executionRows(actions, 7).size)
        assertEquals(10, UAVFlowPolicyContract.executionRows(actions, 10).size)
    }

    @Test
    fun keepsCrossingRowOnlyAsControllerStopSentinel() {
        val actions = actionsWithStops(doubleArrayOf(0.05, 0.75, 0.7, 0.4, 0.2, 0.1, 0.0, 0.0, 0.0, 0.0))
        val rows = UAVFlowPolicyContract.executionRows(actions)
        assertEquals(2, rows.size)
        assertFalse(rows[0].let { UAVFlowPolicyContract.shouldStop(it.stopScore) })
        assertTrue(rows[1].let { UAVFlowPolicyContract.shouldStop(it.stopScore) })
    }

    private fun actionsWithStops(stops: DoubleArray) = stops.mapIndexed { index, stop ->
        UAVFlowPolicyContract.Action(index.toDouble(), 0.0, 0.0, 0.0, stop)
    }
}
