# Android V4 real-device acceptance

This is the remaining hardware gate for the V4 parity goal. A green build, emulator screenshot,
or DJI callback acceptance alone is not a pass. The evidence directory must identify the physical
Android device, aircraft/product, RC, camera/lens, app build SHA-256, and V86 service revision.

## Safe capture command

```bash
scripts/capture_v4_real_acceptance.sh \
  --serial <physical-adb-serial> \
  --apk app/build/outputs/apk/debug/app-debug.apk \
  --restart-app --confirm-grounded
```

The script rejects emulators. Installation (`--apk`) and restart (`--restart-app`) require the
operator to confirm that the aircraft is grounded and has no active control using `--confirm-grounded`.
By default it neither restarts the app nor clears logcat. It records
logcat/screenshots/memory/location/checkpoint preferences, and pulls public logs and trigger frames.
It never enables Virtual Stick, starts motors, changes Simulator state, executes RTH, or calls V86.
Those actions remain explicit operator steps. `--non-interactive` captures only an immediate baseline.

## Acceptance matrix

| Gate | Required observation | Reject when |
|---|---|---|
| Ground link | Live video, product, GPS, RC signal, aircraft/RC battery and endurance assessment update without flicker | Any field is fabricated from another signal, camera remains stale after component reconnect, or UI crashes |
| Account | DJI account reaches the MSDK4 authorized state and remains correct after Activity restart | A WebView close is reported as login success, or the login button remains permanently disabled |
| RC/device direction | RC GPS/course is preferred; fresh phone location/rotation-vector is used only as fallback and clearly changes with device direction | No marker on RCs without GPS, stale heading remains visible, or map/button taps create route vertices |
| Paused Simulator switch | Route stays `PAUSED`; `survey-session.xml` retains the same mission/checkpoint/waypoint before and after Simulator off/on | Checkpoint disappears, route resumes automatically, another controller acquires VS, or sockets are needlessly closed |
| RAW projection | When FC GPS is unavailable, fresh RAW origin+N/E and RAW yaw keep a continuous marker; stale/future/invalid RAW disappears fail-closed | Coarse connection flicker teleports/hides fresh RAW, yaw uses unrelated stale telemetry, or old moving pose persists |
| Camera identity | Consumer single camera is accepted; M30/M30T only an explicit Wide lens may use its calibrated profile | Zoom/thermal/ambiguous lens is treated as Wide, or primary video is claimed trigger-aligned without source proof |
| Gimbal/capture | DJI command is accepted, actual pitch is within 3°, then at least 1.2 s elapses before capture | Capture occurs from callback acceptance alone, retry generation leaks across waypoint/resume, or timeout continues flying |
| Trigger frame/EXIF | Saved JPEG is a decoded frame strictly newer than photo-issued time and within 500 ms; GPS age ≤2 s; GPSAltitude equals ASL; relative altitude remains in sidecar | A pre-trigger/stale frame is saved, `(0,0)`/stale GPS is written, or relative barometric height is standard GPSAltitude |
| DSM/takeoff reference | Current ROI has complete finite coverage; Home/aircraft reference is captured with the takeoff point and reverified within 15 m horizontal/3 m elevation | ROI changes bypass coverage check, failed import deletes the previous valid DSM, or takeoff datum mismatch arms execution |
| Intervention | Stick/RTH/landing/VS-send failure produces zero command, releases ownership, pauses and persists recovery point | Aircraft continues the old vector, checkpoint is cleared, or recovery resumes without operator action |
| V86/offline SfM | Queue survives process restart, only one pump uploads, sequence PUT is idempotent, finalize requires nonempty drained queue, PLY cache opens, unsafe mission is rejected | Corrupt pair is ignored, batch crosses session, Bearer leaves origin, Release uses HTTP, or `safe_to_execute=false` activates |

## Artifact checks

For every accepted trigger-frame pair, inspect the pulled JPEG and JSON sidecar together. On macOS:

```bash
exiftool -GPSLatitude -GPSLongitude -GPSAltitude -DateTimeOriginal \
  -Make -Model -UserComment <image.jpg>
```

Compare EXIF GPS/ASL/time with the matching sidecar and flight log. Record the photo-issued to rendered
frame delay, actual gimbal pitch, GPS age, camera profile label, and whether the source-match gate allowed
or intentionally skipped the mobile frame. A deliberate enterprise multi-camera skip is a safe result,
not a parity failure, until MSDK4 provides trustworthy feed/lens identity on that model.

## Pass accounting

`summary.txt` only indexes evidence. Final acceptance requires a human-reviewed row for every applicable
gate, with `PASS`, `FAIL`, or `NOT APPLICABLE`, plus the evidence filename and timestamp. Simulator evidence
cannot replace camera/RC callbacks on the target aircraft, and a real flight cannot replace the V86 service
idempotency/restart test.
