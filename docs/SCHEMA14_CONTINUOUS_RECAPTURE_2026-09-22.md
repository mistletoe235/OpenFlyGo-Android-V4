# Android V4 schema 14 continuous reacquisition

[English](SCHEMA14_CONTINUOUS_RECAPTURE_2026-09-22.md) · [Chinese reference](SCHEMA14_CONTINUOUS_RECAPTURE_2026-09-22.zh-CN.md)

Date: September 22, 2026. This validation record covers the private and
source-release Android V4 projects. iOS also gained schema 14 support later the
same day; its separate implementation and validation are documented in the iOS
repository. Earlier Android-only scope statements do not describe current iOS
compatibility.

## Protocol and operation

- V4 accepts schemas 1–14. Schema 14 requires a known `recapture_flight_mode`.
- `CONTINUOUS_EXPERIMENTAL` also requires `active_mapping`. Unknown/missing modes, mode fields inserted into schema 13 and versions above 14 are rejected without silent downgrade.
- Ordinary and `STOP_AND_CAPTURE` missions still export schema 13 without a mode field.
- Creating a cloud session requests continuous mode only when the experimental checkbox is selected; it defaults off. Sessions advertise `supported_mission_schemas=[13,14]`, and the server must negotiate compatible exports.
- Import follows existing preflight/start steps. Downloading or selecting an option never takes off. `execution_review` validation remains required; read-only cloud preview does not authorize execution.
- Saving/reloading a mission preserves its recapture mode.

## Continuous execution

The client uses app-side Virtual Stick control, not the V5 KMZ backend. Keep the
app foreground, the controller connected and control authority available. This
is not an offline onboard mission that can run after closing the app.

Only eligible intermediate capture points use continuous following:

- The previous/current/next points are captures with the same view direction.
- Where pass metadata is present, all three belong to the same region and have the `SURVEY` role; reconstruction bridge points are excluded. `pass_index` identifies individual points, not a shared continuous segment.
- Both adjacent horizontal legs are at least 3 m, with a turn of at most 30 degrees.
- Adjacent yaw differences are at most 5 degrees, gimbal-pitch differences at most 3 degrees, and altitude differences at most 0.5 m.
- Endpoints, transit/resume legs and ineligible turns retain stopped-capture behavior.

Bounded lookahead follows adjacent segments, with speed capped by leg length and
the camera's minimum capture interval. This does not reproduce V5 KMZ turn curves
or introduce a global trajectory optimizer.

## Capture confirmation and failure handling

- Flight/gimbal telemetry must be at most one second old and not future-dated, with a valid connection.
- Capture requires yaw error at most 3 degrees, altitude error at most 0.4 m and the existing gimbal readback/stability checks.
- The aircraft must enter the horizontal 1.2 m capture window. Continuous mode does not require zero horizontal speed.
- Only one capture request remains pending at a time. The mission advances only after a successful callback.
- Following may continue while awaiting acknowledgement, but lookahead is bounded by the next segment's midpoint. A slow camera can still cause slowing or stopping.
- A missed capture window, invalid waiting pose, capture failure or eight-second timeout pauses execution rather than marking a missing photo successful. Resume retains the existing preflight/recovery path.

A successful DJI capture-command callback does not establish precise exposure
time or verify the SD-card photo. Motion blur, actual exposure position, camera
cadence and flight tracking require hardware acceptance, not just software tests.

## Recorded validation

Both V4 projects ran:

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew \
  :app:testDebugUnitTest :app:testReleaseUnitTest \
  :app:assembleDebug :app:assembleRelease \
  -Pkotlin.incremental=false -Pkapt.incremental.apt=false
```

| Project | Debug unit tests | Release unit tests | APK build |
| --- | --- | --- | --- |
| Private V4 | 308 passed | 308 passed | Debug and signed Release successful |
| Source-release V4 | 305 passed, 1 skipped | 305 passed, 1 skipped | Debug and unsigned Release successful |

The skipped online-service test requires explicit environment configuration; the
run did not create real cloud sessions. Nine `RecaptureFlightModeTest` cases cover
protocol round trips/rejection, review, capability advertisement, geometry/region
eligibility, telemetry freshness and capture windows, including 64 deterministic
spacing/speed/callback-delay combinations. These are JVM/simplified-kinematics
tests, not DJI Simulator or flight acceptance.

Private Release: `0.3.0-v4`, versionCode 4; APK v2 signing and zipalign passed.
Source template: `0.3.0-v4`, versionCode 3; provide valid keys and signing before
installation/distribution. The validation run did not control an aircraft, execute
a mission, install on a phone, commit or push source.
