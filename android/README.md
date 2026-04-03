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
- No Gradle wrapper has been generated yet

## Next Bootstrap Step

Once your local Android toolchain is installed, generate the wrapper from inside `android/` with a local Gradle install or Android Studio:

```bash
gradle wrapper
```

After that, the expected build loop is:

```bash
./gradlew assembleDebug
./gradlew installDebug
```
