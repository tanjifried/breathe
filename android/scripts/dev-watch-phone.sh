#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"

if ! command -v inotifywait >/dev/null 2>&1; then
  printf 'inotifywait is required. Install it with: sudo apt install inotify-tools\n' >&2
  exit 1
fi

if [[ -z "${ANDROID_HOME:-}" ]]; then
  export ANDROID_HOME="$HOME/Android/Sdk"
fi

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

resolve_phone_serial() {
  adb devices | awk 'NR>1 && $2 == "device" && $1 !~ /^emulator-/ {print $1; exit}'
}

PHONE_SERIAL="${BREATHE_DEVICE_SERIAL:-$(resolve_phone_serial)}"

if [[ -z "$PHONE_SERIAL" ]]; then
  printf 'No physical Android phone detected. Connect one with USB debugging enabled.\n' >&2
  exit 1
fi

printf 'Watching Android files for phone %s...\n' "$PHONE_SERIAL"

while true; do
  BREATHE_DEVICE_SERIAL="$PHONE_SERIAL" "$SCRIPT_DIR/dev-run.sh" --no-log --no-emulator --serial "$PHONE_SERIAL" || true
  printf 'Waiting for file changes...\n'
  inotifywait -qq -r -e close_write,create,delete,move "$ANDROID_DIR/app/src" "$ANDROID_DIR/app/build.gradle.kts"
done
