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

For a visible emulator window:

```bash
./scripts/dev-run-gui.sh
```

Helpful flags:

```bash
./scripts/dev-run.sh --headless
./scripts/dev-run.sh --no-log
./scripts/dev-run.sh --no-emulator
./scripts/dev-run-gui.sh --no-log
```

The script will:

- set `JAVA_HOME` to JDK 17 if needed
- set `ANDROID_HOME` to `~/Android/Sdk` if needed
- start the `Breathe_Pixel` emulator when no device is connected
- build the debug APK
- install the app
- launch `com.breathe/.MainActivity`
- tail logcat unless `--no-log` is passed
- save logcat to `android/logs/logcat-YYYYMMDD-HHMMSS.txt`

The GUI script will:

- stop any running emulator first
- start a visible `Breathe_Pixel` window
- build, install, and launch the app
- tail logcat unless `--no-log` is passed
- save logcat to `android/logs/logcat-YYYYMMDD-HHMMSS.txt`

If the GUI emulator fails with Qt or display-plugin errors, your desktop session is missing emulator GUI support. In that case use:

```bash
./scripts/dev-run.sh --headless
```
