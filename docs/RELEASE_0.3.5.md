# Android V4 0.3.5 release — 2026-09-25

Signed survey-only Release APK: `com.openfly.go.v4`, version `0.3.5-v4`, versionCode `9`.
No MNN/model inference or terrain-following workflow is included in this installer.

## Changes since the previous GitHub preview

- Allow explicitly configured HTTP workstation connections in Release; use HTTPS outside a trusted network because HTTP does not encrypt access codes or images.
- Treat unverified camera geometry as an advisory rather than rejecting an otherwise executable route; retain known incompatible-camera checks.
- Accept cloud mission schemas through 14 and retain cloud point-cloud viewing, mission import and upload workflows.
- Disable extra phone-side downlink image archives by default without disabling aircraft SD-card photos or required upload retry storage.
- Remove the stopped-speed/800 ms pose dwell from ordinary strip starts. The waypoint follower still requires heading alignment within 3 degrees before starting capture, and point captures retain stopped-pose checks.
- Keep duplicate pause operations idempotent and verify recovery-point preservation.

V4 does not include V5's new per-photo moving-pose dwell gate; the flight backends are different.

## Verification and limits

Release unit tests: 318 passed, 1 skipped, 0 failed. The signed APK was installed on Xiaomi 12S.
This does not establish flight safety or compatibility with every SDK-listed aircraft. Rehearse
mission execution and pause/resume in the simulator before real use; retain pilot supervision.

The released APK is built from this public source with privately injected application/map keys and
the maintainer signing certificate. To select the release version in your own build:

```sh
./gradlew :app:testReleaseUnitTest :app:assembleRelease -POPENFLY_VERSION_CODE=9 -POPENFLY_VERSION_NAME=0.3.5-v4
```

Source builds require your own SDK keys and signing configuration; they are not expected to be
byte-identical to the maintainer APK. Preserve missions and do not clear app data to bypass update errors.
