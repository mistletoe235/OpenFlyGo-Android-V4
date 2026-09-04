#!/usr/bin/env bash
set -euo pipefail

PACKAGE="com.openfly.go.v4"
ACTIVITY="edu.playground.djivln.mini2.Mini2CameraActivity"

usage() {
  echo "Usage: $0 --serial SERIAL [--apk PATH] [--restart-app] [--confirm-grounded] [--output DIR] [--non-interactive]" >&2
  echo "Collects evidence only; it never arms Virtual Stick, starts motors, or changes Simulator state." >&2
  exit 2
}

serial=""
apk=""
output=""
interactive=1
restart_app=0
confirmed_grounded=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial) serial="${2:-}"; shift 2 ;;
    --apk) apk="${2:-}"; shift 2 ;;
    --output) output="${2:-}"; shift 2 ;;
    --non-interactive) interactive=0; shift ;;
    --restart-app) restart_app=1; shift ;;
    --confirm-grounded) confirmed_grounded=1; shift ;;
    -h|--help) usage ;;
    *) usage ;;
  esac
done
[[ -n "$serial" ]] || usage
if [[ -n "$apk" || "$restart_app" -eq 1 ]]; then
  [[ "$confirmed_grounded" -eq 1 ]] || {
    echo "Install/restart can interrupt flight. Confirm aircraft grounded and no active control with --confirm-grounded." >&2
    exit 7
  }
fi

ADB=(adb -s "$serial")
state="$("${ADB[@]}" get-state 2>/dev/null || true)"
[[ "$state" == "device" ]] || { echo "ADB target is not ready: $serial ($state)" >&2; exit 3; }
qemu="$("${ADB[@]}" shell getprop ro.kernel.qemu 2>/dev/null | tr -d '\r')"
[[ "$qemu" != "1" ]] || { echo "Refusing emulator $serial; real-device evidence is required." >&2; exit 4; }

safe_serial="$(printf '%s' "$serial" | tr -c 'A-Za-z0-9._-' '_')"
stamp="$(date +%Y%m%d-%H%M%S)"
output="${output:-build/reports/v4-real/${stamp}-${safe_serial}}"
mkdir -p "$output/screens" "$output/state" "$output/public"
printf '%s\n' \
  $'gate\tresult\tevidence\tnotes' \
  $'ground_link\tUNREVIEWED\t\t' \
  $'dji_account\tUNREVIEWED\t\t' \
  $'rc_device_direction\tUNREVIEWED\t\t' \
  $'paused_simulator_switch\tUNREVIEWED\t\t' \
  $'simulator_raw_projection\tUNREVIEWED\t\t' \
  $'camera_lens_identity\tUNREVIEWED\t\t' \
  $'gimbal_settle_capture\tUNREVIEWED\t\t' \
  $'trigger_frame_exif\tUNREVIEWED\t\t' \
  $'dsm_takeoff_reference\tUNREVIEWED\t\t' \
  $'intervention_checkpoint\tUNREVIEWED\t\t' \
  $'v86_offline_sfm\tUNREVIEWED\t\t' > "$output/review.tsv"

if [[ -n "$apk" ]]; then
  [[ -f "$apk" ]] || { echo "APK not found: $apk" >&2; exit 5; }
  shasum -a 256 "$apk" > "$output/apk-sha256.txt"
  "${ADB[@]}" install -r "$apk" | tee "$output/install.txt"
fi
"${ADB[@]}" shell pm path "$PACKAGE" > "$output/package-path.txt" || {
  echo "$PACKAGE is not installed on $serial" >&2
  exit 6
}

{
  echo "captured_at=$(date '+%Y-%m-%dT%H:%M:%S%z')"
  echo "serial=$serial"
  echo "manufacturer=$("${ADB[@]}" shell getprop ro.product.manufacturer | tr -d '\r')"
  echo "model=$("${ADB[@]}" shell getprop ro.product.model | tr -d '\r')"
  echo "device=$("${ADB[@]}" shell getprop ro.product.device | tr -d '\r')"
  echo "android=$("${ADB[@]}" shell getprop ro.build.version.release | tr -d '\r')"
  echo "sdk=$("${ADB[@]}" shell getprop ro.build.version.sdk | tr -d '\r')"
  echo "abi=$("${ADB[@]}" shell getprop ro.product.cpu.abilist | tr -d '\r')"
} > "$output/device.txt"
"${ADB[@]}" shell dumpsys package "$PACKAGE" > "$output/package.txt"
"${ADB[@]}" shell dumpsys battery > "$output/state/battery-start.txt"
if [[ "$restart_app" -eq 1 ]]; then
  "${ADB[@]}" shell am force-stop "$PACKAGE"
  "${ADB[@]}" shell am start -W -n "$PACKAGE/$ACTIVITY" > "$output/start.txt"
fi

"${ADB[@]}" logcat -T 1 -v threadtime > "$output/logcat.txt" 2>&1 &
logcat_pid=$!
cleanup() {
  kill "$logcat_pid" 2>/dev/null || true
  wait "$logcat_pid" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

capture_phase() {
  local phase="$1"
  "${ADB[@]}" exec-out screencap -p > "$output/screens/${phase}.png" || true
  "${ADB[@]}" shell dumpsys meminfo "$PACKAGE" > "$output/state/${phase}-meminfo.txt" || true
  "${ADB[@]}" shell dumpsys activity activities > "$output/state/${phase}-activity.txt" || true
  "${ADB[@]}" shell dumpsys location > "$output/state/${phase}-location.txt" || true
  "${ADB[@]}" shell run-as "$PACKAGE" cat shared_prefs/survey-session.xml \
    > "$output/state/${phase}-survey-session.xml" 2>/dev/null || true
}

prompt_phase() {
  local phase="$1"
  local instruction="$2"
  if [[ "$interactive" -eq 1 ]]; then
    echo
    echo "[$phase] $instruction"
    read -r -p "Complete the manual step, leave the app on the relevant screen, then press Enter... " _
  fi
  capture_phase "$phase"
}

capture_phase "00-start"
prompt_phase "01-ground-link" \
  "Connect RC and aircraft on the ground. Verify DJI login, live video, GPS/signal, aircraft/RC battery, endurance bar, Home and camera identity. Do not start motors."
prompt_phase "02-map-heading" \
  "Open the planner. Verify aircraft/RC-or-phone marker position and direction, Home direction, map follow, and that overlay/button taps do not add polygon vertices."
prompt_phase "03-paused-simulator" \
  "With props removed or on an approved bench, use DJI Simulator: start a short route, pause it, switch Simulator off/on, and confirm the route remains PAUSED with the same checkpoint. Do not resume automatically."
prompt_phase "04-camera-gimbal" \
  "On the ground, issue nadir and oblique survey captures. Confirm the gimbal settles before capture; for enterprise multi-lens payloads record which lens/feed is active."
prompt_phase "05-terrain" \
  "Import the intended DSM, generate for the current ROI, then move/expand the ROI and verify incomplete coverage/NoData is rejected while the previous valid DSM remains available."
prompt_phase "06-recovery" \
  "In Simulator or an approved low-risk test only, trigger manual takeover/RTH/landing intervention and verify checkpointed PAUSE, zero command, and explicit manual resume."
prompt_phase "07-v86" \
  "Using the test V86 endpoint, create a session, queue images, background/reopen the app, drain uploads, finalize, open the PLY, and verify safe_to_execute=false cannot activate a mission."

"${ADB[@]}" shell dumpsys battery > "$output/state/battery-end.txt" || true
"${ADB[@]}" shell dumpsys meminfo "$PACKAGE" > "$output/state/meminfo-end.txt" || true
"${ADB[@]}" pull /sdcard/Download/DJI-VLN/logs "$output/public/logs" >/dev/null 2>&1 || true
"${ADB[@]}" pull /sdcard/Download/DJI-VLN/trigger-frames "$output/public/trigger-frames" >/dev/null 2>&1 || true
cleanup
trap - EXIT INT TERM

count_matches() {
  { rg -o "$1" "${@:2}" 2>/dev/null || true; } | wc -l | tr -d ' '
}
fatal_count="$(count_matches 'FATAL EXCEPTION|ANR in com\.openfly\.go\.v4' "$output/logcat.txt")"
checkpoint_count="$(count_matches 'checkpoint retained|checkpoint preserved' "$output/logcat.txt" "$output/public/logs")"
trigger_saved="$(count_matches 'TRIGGER_FRAME image=' "$output/logcat.txt" "$output/public/logs")"
trigger_skipped="$(count_matches 'TRIGGER_FRAME skipped' "$output/logcat.txt" "$output/public/logs")"
gimbal_accepted="$(count_matches 'gimbal command accepted' "$output/logcat.txt" "$output/public/logs")"
{
  echo "This summary is evidence indexing, not an automatic flight-safety PASS."
  echo "fatal_or_anr=${fatal_count:-0}"
  echo "checkpoint_retained_markers=${checkpoint_count:-0}"
  echo "trigger_frames_saved=${trigger_saved:-0}"
  echo "trigger_frames_skipped=${trigger_skipped:-0}"
  echo "gimbal_command_accepted_markers=${gimbal_accepted:-0}"
  echo "report=$output"
} | tee "$output/summary.txt"

echo "Evidence captured at: $output"
