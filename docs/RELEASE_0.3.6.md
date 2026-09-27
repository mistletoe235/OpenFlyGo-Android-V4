# Android V4 0.3.6 — 2026-09-27

Signed survey-only Release APK: `com.openfly.go.v4`, version `0.3.6-v4`, versionCode `10`.

## Changes / 更新

- Fix the capture-feedback overlay being hidden behind the fullscreen survey map: it now has explicit elevation above the map/planner layers.
- Add a layout regression test for feedback placement and stacking.
- Add Chinese/English FAQs for checking the captured indicator, aircraft photo storage, stationary simulator cooling, and restarting the aircraft after interrupted simulation.
- Keep the existing cloud route/point-cloud workflow, schemas 1–14, and default-off extra phone image archives.
- 地图页“已拍摄”提示层级修复；中英文文档同步。本批不改 V4 飞行控制或拍照阈值。

## Verification / 验证

- Release tests: **319 passed, 1 skipped, 0 failed**; signed APK and ZIP alignment verified.
- This version was previously installed on Xiaomi 12S; this GitHub publication rebuilds the same functional changes with the published source version defaults.
- No new real-flight acceptance is claimed. Rehearse capture, pause/resume and completion before flight.
- Installer excludes MNN/model inference and terrain-following workflows. Cloud upload may retain required retry files; aircraft SD-card capture is unchanged.
- Preserve application data when updating. Different signing certificates cannot overwrite each other; do not erase data to bypass errors.

Reference aircraft: DJI Mini 2 with compatible MSDK V4 hardware. See the README for DJI support links and model limitations.
