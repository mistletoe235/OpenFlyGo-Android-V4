#!/usr/bin/env bash
set -euo pipefail

serial="${1:-}"
shots="${2:-6}"
periods_ms="${3:-}"

adb_cmd=(adb)
if [[ -n "$serial" ]]; then
  adb_cmd+=( -s "$serial" )
fi

component="com.openfly.go.v4/edu.playground.djivln.mini2.Mini2CameraActivity"
action="com.openfly.go.v4.action.CAMERA_CADENCE_TEST"

"${adb_cmd[@]}" get-state >/dev/null
"${adb_cmd[@]}" shell am start -n "$component" >/dev/null
sleep 3
"${adb_cmd[@]}" logcat -c
start_args=(shell am start -n "$component" -a "$action" --ei shots "$shots")
if [[ -n "$periods_ms" ]]; then
  start_args+=(--es periods_ms "$periods_ms")
fi
"${adb_cmd[@]}" "${start_args[@]}" >/dev/null

echo "Camera cadence test started: periods=${periods_ms:-2000,1600,1400,1200,1000}ms shots_per_stage=$shots"
echo "Safety gate: aircraft must be connected, grounded, not recording, and no survey/control may be active."

for _ in $(seq 1 60); do
  output="$("${adb_cmd[@]}" logcat -d -s DjiVln:I '*:S' | grep 'CAMERA_CADENCE' || true)"
  if grep -qE 'CAMERA_CADENCE (COMPLETE|RESULT=(BLOCKED|ABORTED))' <<<"$output"; then
    printf '%s\n' "$output"
    exit 0
  fi
  sleep 2
done

"${adb_cmd[@]}" logcat -d -s DjiVln:I '*:S' | grep 'CAMERA_CADENCE' || true
echo "Timed out waiting for test completion." >&2
exit 1
