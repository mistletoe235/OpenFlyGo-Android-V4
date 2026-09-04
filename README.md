# OpenFly Go for Android — MSDK V4

OpenFly Go is an open-source mobile ground application for low-cost DJI aircraft. This repository
contains the Android client based on DJI Mobile SDK V4, with camera preview and capture, map-based
survey planning, simulator/HIL integration, resumable mission execution, and reconstruction-service
handoff.

The current public release intentionally excludes VLN, on-device model inference, remote inference,
model distribution, and their native runtimes. Those features are planned for a later release.

## Verified hardware

| Client | SDK | Physically verified aircraft |
| --- | --- | --- |
| Android V4 | DJI MSDK 4.16.4 | DJI Mini 2 |

“Verified” means that connection, telemetry, live video, camera operations and the OpenFly survey
workflow have been exercised on real hardware. It does not imply compatibility with every aircraft
listed by DJI for the same SDK generation.

## Features

- DJI connection, telemetry, battery, signal, camera and gimbal status;
- live camera preview, photo/video controls and aircraft-media browser;
- Baidu map integration with aircraft, controller/device and home-point presentation;
- polygon survey planning, route preview, camera-aware spacing and mission-time estimation;
- fixed-altitude execution, checkpoints, safe pause/resume and DJI return-to-home handoff;
- UE/AirSim HIL transport and DJI Simulator validation tools;
- trigger-aligned frame metadata and V86 image-stream/reconstruction client;
- optional DSM/building-height planning retained as experimental functionality.

Terrain following is **off by default**. Enabling it requires an explicit user action, valid surface
data and a new safety review. A generated route is not evidence that obstacle clearance is safe.

## Requirements

- Android Studio with Android SDK 35;
- JDK 17 or newer supported by the included Gradle toolchain;
- an Android device with `arm64-v8a`;
- a DJI Developer account and an MSDK V4 App Key;
- a Baidu Maps Android key for the same application ID and signing-certificate SHA1.

## Configuration

```bash
cp local.properties.example local.properties
```

Set the following values only in the untracked `local.properties` file:

```properties
sdk.dir=/path/to/Android/sdk
OPENFLY_APPLICATION_ID_V4=com.example.openflygo.v4
AIRCRAFT_API_KEY_V4=your_dji_app_key
BAIDU_MAP_AK_V4=your_baidu_map_key
V86_DEFAULT_ENDPOINT=http://127.0.0.1:55000
```

The DJI App Key must match `OPENFLY_APPLICATION_ID_V4`. The Baidu key must match both that package
and the certificate used to sign the APK. Never commit real keys, tokens, keystores or passwords.

## Build and test

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Debug builds use Android's
automatically generated debug certificate. For a release build, copy
`release-signing.properties.example` to `release-signing.properties` and point it at your own
keystore. Official OpenFly Go APKs are signed by the maintainers in a private build environment.

## Architecture

```text
DJI MSDK V4 ── telemetry / camera / gimbal / control
       │
       ├── flight HUD and map
       ├── survey planner and safety policies
       ├── Simulator / UE HIL bridge
       └── capture metadata ── V86 reconstruction service
```

## Safety

This is research software, not a replacement for the remote pilot, DJI flight-safety systems,
airspace authorization, site inspection or legal compliance. Keep visual line of sight, maintain a
manual takeover path and validate changes in simulation before any real flight. Unsupported or stale
telemetry must fail closed.

Useful documentation:

- [Survey simulator and regression workflow](docs/SURVEY_SIMULATOR.md)
- [UE bridge](docs/UE_BRIDGE.md)
- [HIL protocol](docs/UE_HIL_PROTOCOL.md)
- [V86 reconstruction client](docs/V86_ANDROID_V4_CLIENT.md)
- [Global surface data](docs/global-surface-data.md)

## Contributing and license

See [CONTRIBUTING.md](CONTRIBUTING.md) and [SECURITY.md](SECURITY.md) before submitting flight-control
changes. OpenFly Go is licensed under the [Apache License 2.0](LICENSE). Third-party components retain
their own terms; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
