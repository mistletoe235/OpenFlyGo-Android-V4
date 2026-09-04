# Android HIL automated regression

The app has two deliberately separate debug-only regression sources. A result is valid only
when its source label matches the layer being tested.

## Offline Android regression

Action:

```text
com.openfly.go.v4.action.HIL_OFFLINE_REGRESSION
```

Source label:

```text
source=MOCK_RC+MOCK_DYNAMICS
```

This mode requires no DJI aircraft, remote controller, UE process, hotspot, or LAN peer. It
starts a dedicated HIL controller on loopback ports `30120/30121/30122`, starts an in-process
mock UE UDP peer, sends a deterministic 100 Hz pose stream, decodes every POSE packet, replies
to heartbeat and PING packets, and executes these mock stick phases:

1. armed idle;
2. climb;
3. hover;
4. forward flight;
5. neutral-stick braking;
6. yaw.

The run passes only if Android sends and decodes at least 500 POSE packets, measured send rate
is at least 85 Hz, the loopback peer is fresh, takeoff and forward displacement occur, and the
mock braking response produces at least 3 degrees of nose-up pitch.

For throughput acceptance, run it on a physical Android device (no aircraft is required):

```bash
ANDROID_SERIAL=<physical-device-serial> MODE=offline tools/run_hil_android_regression.sh
```

This mode validates Android scheduling, command mapping, protocol encoding/decoding, heartbeat,
RTT, and lifecycle. It does **not** validate DJI flight-controller dynamics and must never be
reported as `DJI SimulatorState`.

An AVD may still be used for functional smoke testing, but its `10.0.2.2` UDP NAT path is not a
throughput authority. On 2026-08-28 the same APK measured 58.6 Hz through AVD-to-Mac NAT and
99.9 Hz with the in-process peer on physical Android (`M332BF`, 719 sent / 720 decoded). The
physical evidence is stored in `build/reports/hil/offline-20260828-114820.log`. Never lower the
85 Hz gate to make an emulator-NAT run pass.

## DJI RAW Simulator regression

Action:

```text
com.openfly.go.v4.action.HIL_SIMULATOR_REGRESSION
```

Source label:

```text
source=DJI SimulatorState RAW synthetic=false
```

This mode requires a connected supported DJI aircraft. It automatically opens HIL, starts DJI
Simulator with timeout/retry handling, waits for a raw state sample newer than 500 ms, enables
Virtual Stick only while Simulator remains active, and runs this sequence twice inside one DJI
Simulator session:

1. capture the raw baseline and measured callback rate;
2. keep Virtual Stick disabled, allow the flight mode to settle, and request `startTakeoff()`;
3. wait up to seven seconds for raw motor, flying, and altitude-change evidence;
4. after the takeoff transition completes, enable Virtual Stick;
5. send a bounded two-second forward Mode 2 stick pulse and then neutralize all axes;
6. evaluate DJI RAW state and the Android-to-UE loopback independently;
7. release Virtual Stick, land until raw `motorsOn=false` and `flying=false`, then repeat from a
   fresh raw baseline without stopping or restarting Simulator;
8. after the second successful landing, stop the owned Simulator and HIL loopback.

The pulses pass through the same tested stick-to-body-velocity mapping before Virtual Stick
transmission. Every command is blocked immediately if Simulator deactivates or raw state becomes
stale. It never substitutes mock or integrated poses.

Simulator startup retries both callback failures and six-second callback timeouts, up to three
attempts. Start and stop completion use `simulator.isSimulatorActive` as the authoritative state,
so an SDK error accompanied by a real state transition is logged accurately and an owned
Simulator is still stopped during final cleanup. Keeping both cycles in one Simulator session
also avoids relying on a second SDK Simulator start immediately after landing, which can be
rejected by some aircraft/firmware combinations even though the preceding cleanup succeeded.

Run it with the phone's current wireless ADB serial:

```bash
ANDROID_SERIAL=<phone-ip:port> MODE=raw tools/run_hil_android_regression.sh
```

The script wakes the display, dismisses a non-secure keyguard, and keeps the device awake so
MIUI/Android lifecycle suspension cannot silently pause the regression. If no DJI product is
connected after 10 seconds, the run ends explicitly as unavailable. Every run writes its
evidence log under `build/reports/hil/`.

The DJI RAW portion passes only when all of these are true:

- raw callback rate reaches at least 50% of the requested rate, with a 10 Hz floor;
- raw `motorsOn` and `flying` have both been observed;
- raw altitude increases by at least 0.05 m;
- forward displacement reaches at least 0.05 m;
- attitude changes by at least 0.5 degrees during the forward pulse.

The UE link portion passes only when the loopback peer is fresh, Android sends at least 20 POSE
packets, the peer decodes at least 20 POSE packets, and measured POSE rate reaches at least 50% of
the configured rate, with a 10 Hz floor. The final result is `PASS` only when both portions pass.
The run logs baseline and post-command XYZ/RPY, motor and flying flags, callback frequency,
Virtual Stick enable result, takeoff result, threshold evidence, each `CYCLE_PASS`, and one final
`PASS` or `FAIL` line. Final `PASS` requires `cycles=2/2` and successful final cleanup.

## Runtime recovery safeguards

- An active airborne Simulator is adopted as-is; Android never stops or restarts it automatically.
- A grounded Simulator with fresh RAW state but invalid GPS/Home is observed for 10 seconds before
  Android performs one controlled stop/start recovery. This avoids resetting normal GPS warm-up.
- HIL network shutdown or reconfiguration preserves the Simulator session.
- DJI `Undefined Error code=255` trips a command fuse immediately. Further takeoff retries are
  blocked until a new DJI product connection is observed, preventing repeated commands against a
  poisoned flight-controller Simulator state.
- Repeated UDP transport failures are rate-limited in logs while status reporting continues.

## Provenance rule

- `MOCK_RC+MOCK_DYNAMICS` proves only the Android/HIL transport layer.
- `DJI SimulatorState RAW` proves only values received from the DJI SDK callback.
- No automatic fallback is allowed between these sources.
- A failed or unavailable RAW test remains failed or unavailable; it is never replaced with a
  mock success.
