#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"

APP_ID="com.breathe"
MAIN_ACTIVITY=".MainActivity"
APK_PATH="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"
AVD_NAME="${BREATHE_AVD:-Breathe_Pixel}"
WATCH_LOGS=1
COLD_BOOT=0
LOG_DIR="$ANDROID_DIR/logs"
LOG_FILE="$LOG_DIR/logcat-$(date +%Y%m%d-%H%M%S).txt"

while (($# > 0)); do
  case "$1" in
    --no-log)
      WATCH_LOGS=0
      ;;
    --cold-boot)
      COLD_BOOT=1
      ;;
    *)
      printf 'Unknown option: %s\n' "$1" >&2
      printf 'Usage: %s [--cold-boot] [--no-log]\n' "$0" >&2
      exit 1
      ;;
  esac
  shift
done

if [[ -z "${DISPLAY:-}" && -z "${WAYLAND_DISPLAY:-}" ]]; then
  printf 'No GUI display detected. Start a desktop session or use ./scripts/dev-run.sh --headless instead.\n' >&2
  exit 1
fi

if command -v xset >/dev/null 2>&1 && ! xset q >/dev/null 2>&1; then
  printf 'GUI display is set but not accessible from this shell. Use ./scripts/dev-run.sh instead.\n' >&2
  exit 1
fi

if [[ -z "${JAVA_HOME:-}" && -d "/usr/lib/jvm/java-17-openjdk-amd64" ]]; then
  export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
fi

if [[ -z "${ANDROID_HOME:-}" ]]; then
  export ANDROID_HOME="$HOME/Android/Sdk"
fi

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

mkdir -p "$LOG_DIR"

if [[ ! -x "$ANDROID_DIR/gradlew" ]]; then
  printf 'Missing Gradle wrapper at %s/gradlew\n' "$ANDROID_DIR" >&2
  exit 1
fi

if [[ ! -x "$ANDROID_HOME/platform-tools/adb" ]]; then
  printf 'adb not found under %s/platform-tools\n' "$ANDROID_HOME" >&2
  exit 1
fi

if [[ ! -x "$ANDROID_HOME/emulator/emulator" ]]; then
  printf 'emulator not found under %s/emulator\n' "$ANDROID_HOME" >&2
  exit 1
fi

wait_for_install_ready() {
  for _ in $(seq 1 60); do
    if [[ "$(adb get-state 2>/dev/null || true)" != "device" ]]; then
      sleep 5
      continue
    fi

    if [[ "$(adb shell service check package 2>/dev/null | tr -d '\r')" != "Service package: found" ]]; then
      sleep 5
      continue
    fi

    if ! adb shell settings get global device_name >/dev/null 2>&1; then
      sleep 5
      continue
    fi

    if ! adb shell cmd package list packages android >/dev/null 2>&1; then
      sleep 5
      continue
    fi

    return 0
  done

  return 1
}

install_debug_apk() {
  local attempt

  if ! wait_for_install_ready; then
    printf 'Android package manager never became install-ready.\n' >&2
    return 1
  fi

  for attempt in $(seq 1 5); do
    if adb install --no-streaming -r "$APK_PATH"; then
      return 0
    fi

    printf 'Install attempt %s failed. Waiting for package services to settle...\n' "$attempt" >&2
    sleep 15

    if ! wait_for_install_ready; then
      printf 'Android package manager became unavailable after install attempt %s.\n' "$attempt" >&2
    fi
  done

  return 1
}

launch_app() {
  local attempt

  for attempt in $(seq 1 12); do
    if ! adb shell pm path "$APP_ID" >/dev/null 2>&1; then
      sleep 3
      continue
    fi

    if adb shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; then
      return 0
    fi

    if adb shell am start -n "$APP_ID/$MAIN_ACTIVITY" >/dev/null 2>&1; then
      return 0
    fi

    sleep 3
  done

  return 1
}

wait_for_boot() {
  local emulator_pid="$1"

  for _ in $(seq 1 120); do
    if ! kill -0 "$emulator_pid" >/dev/null 2>&1; then
      return 1
    fi

    if [[ "$(adb get-state 2>/dev/null || true)" != "device" ]]; then
      sleep 5
      continue
    fi

    if [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; then
      sleep 5
      continue
    fi

    if [[ "$(adb shell getprop dev.bootcomplete 2>/dev/null | tr -d '\r')" != "1" ]]; then
      sleep 5
      continue
    fi

    if [[ "$(adb shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r')" != "stopped" ]]; then
      sleep 5
      continue
    fi

    if [[ "$(adb shell service check package 2>/dev/null | tr -d '\r')" != "Service package: found" ]]; then
      sleep 5
      continue
    fi

    if ! adb shell settings get global device_name >/dev/null 2>&1; then
      sleep 5
      continue
    fi

    if adb shell pm path android >/dev/null 2>&1; then
      return 0
    fi

    sleep 5
  done
  return 1
}

printf 'Stopping existing emulator instances...\n'
adb emu kill >/dev/null 2>&1 || true
sleep 3

for ATTEMPT in 1 2; do
  ARGS=("-avd" "$AVD_NAME")

  if [[ "$COLD_BOOT" -eq 1 || "$ATTEMPT" -eq 2 ]]; then
    ARGS+=("-no-snapshot-load")
    printf 'Starting GUI emulator %s with cold boot...\n' "$AVD_NAME"
  else
    printf 'Starting GUI emulator %s with quick boot...\n' "$AVD_NAME"
  fi

  nohup emulator "${ARGS[@]}" >/tmp/breathe-emulator-gui.log 2>&1 &
  EMULATOR_PID=$!

  if wait_for_boot "$EMULATOR_PID"; then
    break
  fi

  if [[ "$ATTEMPT" -eq 1 && "$COLD_BOOT" -eq 0 ]]; then
    printf 'Quick boot failed. Retrying GUI emulator with cold boot...\n' >&2
    adb emu kill >/dev/null 2>&1 || true
    pkill -f "emulator.*$AVD_NAME|qemu-system-x86_64.*$AVD_NAME" >/dev/null 2>&1 || true
    sleep 5
    continue
  fi

  printf 'GUI emulator failed to boot. See /tmp/breathe-emulator-gui.log\n' >&2
  exit 1
done

cd "$ANDROID_DIR"

printf 'Building debug APK...\n'
./gradlew assembleDebug

if [[ ! -f "$APK_PATH" ]]; then
  printf 'Debug APK not found at %s\n' "$APK_PATH" >&2
  exit 1
fi

printf 'Installing debug APK with adb...\n'
if ! install_debug_apk; then
  printf 'ADB install failed after multiple attempts.\n' >&2
  exit 1
fi

printf 'Launching app...\n'
if ! launch_app; then
  printf 'Failed to launch %s after install.\n' "$APP_ID" >&2
  exit 1
fi

if [[ "$WATCH_LOGS" -eq 1 ]]; then
  printf 'Watching logcat. Saving logs to %s\n' "$LOG_FILE"
  exec bash -lc 'adb logcat -T 1 -s AndroidRuntime ActivityManager System.err BreatheMessaging | tee "$1"' _ "$LOG_FILE"
fi

printf 'GUI build, install, and launch completed.\n'
