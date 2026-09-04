#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODE="${MODE:-offline}"
SERIAL="${ANDROID_SERIAL:-emulator-5554}"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE="com.openfly.go.v4"
ACTIVITY="$PACKAGE/edu.playground.djivln.mini2.Mini2CameraActivity"

if [[ "$MODE" == "offline" ]]; then
  ACTION="com.openfly.go.v4.action.HIL_OFFLINE_REGRESSION"
  RESULT_PATTERN="HIL_OFFLINE RESULT="
  TIMEOUT_SECONDS=20
elif [[ "$MODE" == "raw" ]]; then
  ACTION="com.openfly.go.v4.action.HIL_SIMULATOR_REGRESSION"
  RESULT_PATTERN="HIL_REGRESSION RESULT="
  TIMEOUT_SECONDS=120
elif [[ "$MODE" == "raw-reconnect" ]]; then
  ACTION="com.openfly.go.v4.action.HIL_RECONNECT_SIMULATOR_REGRESSION"
  RESULT_PATTERN="HIL_REGRESSION RESULT="
  TIMEOUT_SECONDS=130
else
  echo "MODE must be offline, raw, or raw-reconnect" >&2
  exit 2
fi

"$ROOT/gradlew" -p "$ROOT" :app:testDebugUnitTest :app:assembleDebug >/dev/null
adb -s "$SERIAL" install -r "$APK" >/dev/null
adb -s "$SERIAL" logcat -c
if [[ "$MODE" == "raw" || "$MODE" == "raw-reconnect" ]]; then
  adb -s "$SERIAL" shell input keyevent KEYCODE_WAKEUP || true
  adb -s "$SERIAL" shell wm dismiss-keyguard || true
  adb -s "$SERIAL" shell svc power stayon true || true
fi
adb -s "$SERIAL" shell am force-stop "$PACKAGE"
adb -s "$SERIAL" shell am start -n "$ACTIVITY" -a "$ACTION" >/dev/null

REPORT_DIR="$ROOT/build/reports/hil"
mkdir -p "$REPORT_DIR"
REPORT_FILE="$REPORT_DIR/${MODE}-$(date +%Y%m%d-%H%M%S).log"

for ((second = 0; second < TIMEOUT_SECONDS; second += 1)); do
  result="$(adb -s "$SERIAL" logcat -d -v brief DjiVln:I '*:S' | grep "$RESULT_PATTERN" | tail -1 || true)"
  if [[ -n "$result" ]]; then
    echo "$result"
    adb -s "$SERIAL" logcat -d -v time DjiVln:I '*:S' \
      | grep -E 'HIL_OFFLINE|HIL_REGRESSION' > "$REPORT_FILE" || true
    echo "report=$REPORT_FILE"
    [[ "$result" == *"RESULT=PASS"* ]]
    exit
  fi
  sleep 1
done

adb -s "$SERIAL" logcat -d -v brief DjiVln:I '*:S' | grep -E 'HIL_OFFLINE|HIL_REGRESSION' | tail -80
adb -s "$SERIAL" logcat -d -v time DjiVln:I '*:S' \
  | grep -E 'HIL_OFFLINE|HIL_REGRESSION' > "$REPORT_FILE" || true
echo "report=$REPORT_FILE"
echo "Timed out waiting for $RESULT_PATTERN" >&2
exit 1
