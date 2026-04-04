# Android Scaffold

This directory is the initial Android project scaffold for Breathe.

## Layout

- `android/` is the Android project root
- `android/app/` is the app module
- package namespace: `com.breathe`

## Current State

- Clean architecture skeleton is in place
- Compose navigation shell is in place
- Hilt, Room, Retrofit, OkHttp, WorkManager, and FCM dependencies are declared
- Main screens and ViewModels exist as scaffolding
- Gradle wrapper is generated and the debug build succeeds

## Run From VS Code

Do not run `./gradlew adb`. `adb` is an Android SDK command, not a Gradle task.

Use the dev script instead:

```bash
./scripts/dev-run.sh
```

This is the default reliable path. It starts the emulator headless when needed so installs do not depend on GUI display access.

When a physical Android phone is connected over USB, the same script will prefer that device, configure `adb reverse tcp:3000 tcp:3000`, and build the debug app with `http://127.0.0.1:3000/` as the dev server URL.

For a visible emulator window:

```bash
./scripts/dev-run-gui.sh
```

Helpful flags:

```bash
./scripts/dev-run.sh --headless
./scripts/dev-run.sh --cold-boot
./scripts/dev-run.sh --serial <adb-serial>
./scripts/dev-run.sh --no-log
./scripts/dev-run.sh --no-emulator
./scripts/dev-run-gui.sh --cold-boot
./scripts/dev-run-gui.sh --no-log
./scripts/dev-watch-phone.sh
```

For continuous updates on a USB-connected Android phone:

```bash
./scripts/dev-watch-phone.sh
```

This watches `app/src` and rebuilds, reinstalls, and relaunches the app on the connected physical phone after each file change. If more than one device is connected, set:

```bash
export BREATHE_DEVICE_SERIAL=<adb-serial>
./scripts/dev-watch-phone.sh
```

The script will:

- set `JAVA_HOME` to JDK 17 if needed
- set `ANDROID_HOME` to `~/Android/Sdk` if needed
- restart the adb server if a device is stuck in `offline`
- start the `Breathe_Pixel` emulator when no device is connected
- prefer quick boot and retry once with cold boot if emulator startup is unstable
- launch the default emulator workflow headless for reliability
- wait for Android boot and the package manager service to become install-ready
- build the debug APK
- install the APK with `adb install --no-streaming -r` plus readiness retries for better emulator reliability
- launch the app through the launcher intent with retries after install
- tail logcat unless `--no-log` is passed
- save logcat to `android/logs/logcat-YYYYMMDD-HHMMSS.txt`

The phone watch script requires `inotifywait` from `inotify-tools`.

The GUI script will:

- stop any running emulator first
- start a visible `Breathe_Pixel` window
- prefer quick boot and retry once with cold boot if startup fails
- build, install with `adb` retry logic, and launch the app
- tail logcat unless `--no-log` is passed
- save logcat to `android/logs/logcat-YYYYMMDD-HHMMSS.txt`

If the GUI emulator fails with Qt or display-plugin errors, your desktop session is missing emulator GUI support. In that case use:

```bash
./scripts/dev-run.sh --headless
```

The GUI script also requires actual display access from the current shell, not just a `DISPLAY` value in the environment.

If you run `./gradlew installDebug` directly, make sure `adb devices` already shows a target in `device` state. Otherwise Gradle will fail with `No connected devices!` or `device offline`.

On this environment, direct Gradle `installDebug` can still be less reliable than `adb install -r` even after the device is up. The repo scripts use the more reliable `adb` install path.
