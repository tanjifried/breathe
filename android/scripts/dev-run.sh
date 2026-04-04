#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"

APP_ID="com.breathe"
MAIN_ACTIVITY=".MainActivity"
APK_PATH="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"
AVD_NAME="${BREATHE_AVD:-Breathe_Pixel}"
WATCH_LOGS=1
START_EMULATOR=1
HEADLESS=0
COLD_BOOT=0
TARGET_SERIAL=""
TARGET_SERVER_URL=""
TARGET_SERIAL_OVERRIDE="${BREATHE_DEVICE_SERIAL:-}"
LOG_DIR="$ANDROID_DIR/logs"
LOG_FILE="$LOG_DIR/logcat-$(date +%Y%m%d-%H%M%S).txt"

while (($# > 0)); do
  case "$1" in
    --no-log)
      WATCH_LOGS=0
      ;;
    --no-emulator)
      START_EMULATOR=0
      ;;
    --headless)
      HEADLESS=1
      ;;
    --cold-boot)
      COLD_BOOT=1
      ;;
    --serial)
      shift
      if [[ $# -eq 0 ]]; then
        printf 'Missing value for --serial\n' >&2
        exit 1
      fi
      TARGET_SERIAL_OVERRIDE="$1"
      ;;
    *)
      printf 'Unknown option: %s\n' "$1" >&2
      printf 'Usage: %s [--headless] [--cold-boot] [--serial <adb-serial>] [--no-log] [--no-emulator]\n' "$0" >&2
      exit 1
      ;;
  esac
  shift
done

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

has_device() {
  [[ -n "$(adb devices | awk 'NR>1 && $2 == "device" {print $1}')" ]]
}

has_offline_device() {
  [[ -n "$(adb devices | awk 'NR>1 && $2 == "offline" {print $1}')" ]]
}

resolve_target_device() {
  if [[ -n "$TARGET_SERIAL_OVERRIDE" ]]; then
    if [[ "$(adb -s "$TARGET_SERIAL_OVERRIDE" get-state 2>/dev/null || true)" != "device" ]]; then
      return 1
    fi

    TARGET_SERIAL="$TARGET_SERIAL_OVERRIDE"
  else
    TARGET_SERIAL="$(adb devices | awk 'NR>1 && $2 == "device" {print $1; exit}')"
  fi

  if [[ -z "$TARGET_SERIAL" ]]; then
    return 1
  fi

  if [[ "$TARGET_SERIAL" == emulator-* ]]; then
    TARGET_SERVER_URL="http://10.0.2.2:3000/"
  else
    TARGET_SERVER_URL="http://127.0.0.1:3000/"
  fi

  return 0
}

adb_cmd() {
  if [[ -n "$TARGET_SERIAL" ]]; then
    adb -s "$TARGET_SERIAL" "$@"
  else
    adb "$@"
  fi
}

setup_device_networking() {
  if [[ -z "$TARGET_SERIAL" || "$TARGET_SERIAL" == emulator-* ]]; then
    return 0
  fi

  printf 'Configuring USB reverse for %s...\n' "$TARGET_SERIAL"
  adb_cmd reverse tcp:3000 tcp:3000 >/dev/null
}

wait_for_install_ready() {
  for _ in $(seq 1 60); do
    if [[ "$(adb_cmd get-state 2>/dev/null || true)" != "device" ]]; then
      sleep 5
      continue
    fi

    if [[ "$(adb_cmd shell service check package 2>/dev/null | tr -d '\r')" != "Service package: found" ]]; then
      sleep 5
      continue
    fi

    if ! adb_cmd shell settings get global device_name >/dev/null 2>&1; then
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
    if adb_cmd install --no-streaming -r "$APK_PATH"; then
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
    if ! adb_cmd shell pm path "$APP_ID" >/dev/null 2>&1; then
      sleep 3
      continue
    fi

    if adb_cmd shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; then
      return 0
    fi

    if adb_cmd shell am start -n "$APP_ID/$MAIN_ACTIVITY" >/dev/null 2>&1; then
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

start_emulator() {
  local base_args=("-avd" "$AVD_NAME" "-no-window" "-no-audio")
  local args=()
  local emulator_pid
  local attempt

  for attempt in 1 2; do
    args=("${base_args[@]}")

    if [[ "$COLD_BOOT" -eq 1 || "$attempt" -eq 2 ]]; then
      args+=("-no-snapshot-load")
      printf 'Starting emulator %s with cold boot...\n' "$AVD_NAME"
    else
      printf 'Starting emulator %s with quick boot...\n' "$AVD_NAME"
    fi

    nohup emulator "${args[@]}" >/tmp/breathe-emulator.log 2>&1 &
    emulator_pid=$!

    if wait_for_boot "$emulator_pid"; then
      return 0
    fi

    if [[ "$attempt" -eq 1 && "$COLD_BOOT" -eq 0 ]]; then
      printf 'Quick boot failed. Retrying with cold boot...\n' >&2
      adb emu kill >/dev/null 2>&1 || true
      pkill -f "emulator.*$AVD_NAME|qemu-system-x86_64.*$AVD_NAME" >/dev/null 2>&1 || true
      sleep 5
    fi
  done

  printf 'Emulator failed to boot. See /tmp/breathe-emulator.log\n' >&2
  exit 1
}

adb start-server >/dev/null

if has_offline_device; then
  printf 'ADB reports an offline device. Restarting adb server...\n'
  adb kill-server >/dev/null 2>&1 || true
  adb start-server >/dev/null
fi

if ! has_device; then
  if [[ "$START_EMULATOR" -eq 1 ]]; then
    start_emulator
  else
    printf 'No Android device detected. Start an emulator or connect a device.\n' >&2
    exit 1
  fi
fi

if ! resolve_target_device; then
  printf 'No Android device detected after startup.\n' >&2
  exit 1
fi

setup_device_networking

cd "$ANDROID_DIR"

printf 'Building debug APK...\n'
BREATHE_SERVER_URL="$TARGET_SERVER_URL" ./gradlew assembleDebug

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
  exec bash -lc 'adb -s "$1" logcat -T 1 -s AndroidRuntime ActivityManager System.err BreatheMessaging | tee "$2"' _ "$TARGET_SERIAL" "$LOG_FILE"
fi

printf 'Build, install, and launch completed.\n'
