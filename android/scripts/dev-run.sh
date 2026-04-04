#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"

APP_ID="com.breathe"
MAIN_ACTIVITY=".MainActivity"
AVD_NAME="${BREATHE_AVD:-Breathe_Pixel}"
WATCH_LOGS=1
START_EMULATOR=1
HEADLESS=0
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
    *)
      printf 'Unknown option: %s\n' "$1" >&2
      printf 'Usage: %s [--headless] [--no-log] [--no-emulator]\n' "$0" >&2
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

wait_for_boot() {
  adb wait-for-device >/dev/null
  for _ in $(seq 1 60); do
    if [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
      return 0
    fi
    sleep 5
  done
  return 1
}

start_emulator() {
  local args=("-avd" "$AVD_NAME" "-no-snapshot-load")

  if [[ "$HEADLESS" -eq 1 || ( -z "${DISPLAY:-}" && -z "${WAYLAND_DISPLAY:-}" ) ]]; then
    args+=("-no-window" "-no-audio")
  fi

  printf 'Starting emulator %s...\n' "$AVD_NAME"
  nohup emulator "${args[@]}" >/tmp/breathe-emulator.log 2>&1 &

  if ! wait_for_boot; then
    printf 'Emulator failed to boot. See /tmp/breathe-emulator.log\n' >&2
    exit 1
  fi
}

if ! has_device; then
  if [[ "$START_EMULATOR" -eq 1 ]]; then
    start_emulator
  else
    printf 'No Android device detected. Start an emulator or connect a device.\n' >&2
    exit 1
  fi
fi

cd "$ANDROID_DIR"

printf 'Building debug APK...\n'
./gradlew assembleDebug

printf 'Installing debug APK...\n'
./gradlew installDebug

printf 'Launching app...\n'
adb shell am start -n "$APP_ID/$MAIN_ACTIVITY" >/dev/null

if [[ "$WATCH_LOGS" -eq 1 ]]; then
  printf 'Watching logcat. Saving logs to %s\n' "$LOG_FILE"
  exec bash -lc 'adb logcat -T 1 -s AndroidRuntime ActivityManager System.err BreatheMessaging | tee "$1"' _ "$LOG_FILE"
fi

printf 'Build, install, and launch completed.\n'
