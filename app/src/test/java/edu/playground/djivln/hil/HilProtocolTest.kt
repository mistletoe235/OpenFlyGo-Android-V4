package edu.playground.djivln.hil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HilProtocolTest {
    @Test
    fun poseRoundTripsWithoutCoordinateReinterpretation() {
        val pose = HilProtocol.Pose(
            sampleMonotonicNanos = 1234L,
            originLatitudeDegrees = 31.0,
            originLongitudeDegrees = 121.0,
            eastMeters = 4.0,
            northMeters = 5.0,
            upMeters = 6.0,
            rollDegrees = 1.0,
            pitchDegrees = 2.0,
            headingDegreesClockwiseFromNorth = 90.0,
            velocityNorthMetersPerSecond = 0.5,
            velocityEastMetersPerSecond = 0.6,
            velocityUpMetersPerSecond = 0.7,
            gimbalPitchDegrees = -30.0,
            commandForwardMetersPerSecond = 1.1,
            commandRightMetersPerSecond = 1.2,
            commandUpMetersPerSecond = 1.3,
            commandYawRateDegreesPerSecond = 15.0,
            flightStateAgeMillis = 80,
            measuredSimulatorHz = 99.5f,
            stateFlags = HilProtocol.POSE_FLAG_FLYING,
        )
        val datagram = HilProtocol.decode(HilProtocol.encodePose(7L, 9L, pose))

        assertEquals(HilProtocol.TYPE_POSE, datagram.header.type)
        assertEquals(7L, datagram.header.sessionId)
        assertEquals(9L, datagram.header.sequence)
        assertEquals(pose, HilProtocol.decodePose(datagram.payload))
    }

    @Test
    fun eventRoundTripsUtf8Reason() {
        val event = HilProtocol.Event(77L, HilProtocol.EVENT_COLLISION, 0.91, 42L, "树干碰撞")
        val decoded = HilProtocol.decode(HilProtocol.encodeEvent(1L, 2L, event))
        assertEquals(event, HilProtocol.decodeEvent(decoded.payload))
    }

    @Test
    fun eventRejectsInvalidKindInfiniteScoreAndMalformedUtf8() {
        fun encoded(reason: String = "xx") = HilProtocol.encodeEvent(
            1L, 2L, HilProtocol.Event(77L, HilProtocol.EVENT_STOP, 0.8, 42L, reason),
        )

        val badKind = encoded()
        ByteBuffer.wrap(badKind).order(ByteOrder.BIG_ENDIAN).putInt(40, 99)
        assertThrows(IllegalArgumentException::class.java) {
            HilProtocol.decodeEvent(HilProtocol.decode(badKind).payload)
        }

        val infiniteScore = encoded()
        ByteBuffer.wrap(infiniteScore).order(ByteOrder.BIG_ENDIAN)
            .putDouble(44, Double.POSITIVE_INFINITY)
        assertThrows(IllegalArgumentException::class.java) {
            HilProtocol.decodeEvent(HilProtocol.decode(infiniteScore).payload)
        }

        val malformedUtf8 = encoded()
        malformedUtf8[64] = 0xC3.toByte()
        malformedUtf8[65] = 0x28
        assertThrows(IllegalArgumentException::class.java) {
            HilProtocol.decodeEvent(HilProtocol.decode(malformedUtf8).payload)
        }
    }

    @Test
    fun corruptHeaderIsRejected() {
        val bytes = HilProtocol.encodePing(1L, 2L, HilProtocol.Ping(3L))
        bytes[0] = 0
        assertThrows(IllegalArgumentException::class.java) { HilProtocol.decode(bytes) }
    }

    @Test
    fun unknownMessageTypeIsRejected() {
        val bytes = HilProtocol.encodePing(1L, 2L, HilProtocol.Ping(3L))
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putShort(6, 99)
        assertThrows(IllegalArgumentException::class.java) { HilProtocol.decode(bytes) }
    }
}
