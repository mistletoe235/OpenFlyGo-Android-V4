# OpenFly HIL protocol

This document describes the public hardware-in-the-loop boundary shared by the OpenFly mobile
clients and a UE/AirSim host. It carries simulator state, survey commands and an optional virtual
camera. It does not define or transport model-inference requests.

## Transport

- UDP `30020`: simulator pose/state from the host to Android;
- UDP `30021`: bounded survey-control state from Android to the host;
- TCP `30022`: length-delimited virtual-camera frames;
- all numeric pose and velocity fields use SI units;
- world vectors use ENU; body vectors use forward-right-up.

The mobile client supports direct LAN addressing and Android-hotspot peer discovery. A session must
lock to one peer and reject packets from another peer until the current session is explicitly reset.

## Lifecycle and safety

1. The user explicitly starts HIL and selects the connection mode.
2. Both peers exchange a versioned handshake before state or frames are accepted.
3. Sequence numbers and monotonic timestamps reject replayed or out-of-order packets.
4. Stale pose, stale camera data, disconnect, backgrounding, collision, emergency stop or manual
   takeover pauses survey execution and emits a zero command.
5. Reconnection never resumes a paused mission automatically.

The virtual camera may be used for preview, recording and survey-capture testing. It is not injected
into DJI aircraft obstacle sensors and does not activate DJI's onboard avoidance system.

## Validation

Run unit tests before integration:

```bash
./gradlew testDebugUnitTest
```

Then verify packet freshness, ENU axes, reconnect behavior, zero-command release and simulator-only
interlocks before connecting a real aircraft.
