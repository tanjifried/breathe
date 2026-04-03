# Kubuntu Dev Setup

This is the practical setup for building Breathe on Kubuntu with OpenCode generating code and VS Code acting as the editor and build runner.

## Identity And Goal

- Identity: Breathe Lead Orchestrator
- Goal: keep the self-hosted server, browser extension, and future Android app moving together without breaking privacy or the offline-first contract

## Execution Steps

1. Check the current repo and environment before changing anything.
2. Implement the smallest useful backend or extension slice that advances the roadmap.
3. Keep server contracts stable enough for the future Android client.
4. Use temp agents for focused work instead of one giant prompt.
5. Validate with real commands in VS Code terminals.

## What To Install On Kubuntu

Install the base packages first:

```bash
sudo apt update
sudo apt install -y build-essential openjdk-17-jdk git curl unzip zip ca-certificates qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils
sudo usermod -aG kvm,libvirt "$USER"
```

Log out and back in after adding the `kvm` and `libvirt` groups.

Install Node with `nvm` so the server toolchain stays easy to upgrade:

```bash
curl -fsSL https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
export NVM_DIR="$HOME/.nvm"
. "$NVM_DIR/nvm.sh"
nvm install --lts
nvm use --lts
```

Set `JAVA_HOME` to JDK 17 for Android builds by adding these to `~/.bashrc`:

```bash
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"
```

## Android Command-Line Tools

Download the Linux `commandlinetools` package from Android Developers and unpack it into `~/Android/Sdk/cmdline-tools/latest`.

Then add these to `~/.bashrc`:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
export PATH="$ANDROID_HOME/emulator:$PATH"
```

Reload your shell and install the SDK packages:

```bash
yes | sdkmanager --licenses
sdkmanager "platform-tools" "build-tools;35.0.0" "platforms;android-35" \
  "system-images;android-35;google_apis;x86_64" "emulator"
```

Create the emulator:

```bash
echo "no" | avdmanager create avd -n Breathe_Pixel -k "system-images;android-35;google_apis;x86_64" --device "pixel_7"
```

## VS Code Extensions

Install these extensions:

- `Kotlin` by JetBrains
- `Gradle for Java` by Microsoft
- `OpenCode` by sst-dev
- `Error Lens`
- `GitLens`
- `REST Client`
- `Better Comments`
- `EditorConfig for VS Code`

## Current Machine Check

At the time this file was added, this workspace already had:

- Node.js
- npm
- VS Code CLI
- Java 21

And it was still missing:

- `sdkmanager`
- `adb`
- `emulator`

For Android work, install JDK 17 even if Java 21 is already present.

## Build Loop

Server:

```bash
cd server
npm install
npm start
```

Android when the app exists:

```bash
./gradlew assembleDebug
emulator -avd Breathe_Pixel
./gradlew installDebug
adb logcat -s Breathe,AndroidRuntime,System.err
```

Extension:

1. Open `brave://extensions`
2. Enable Developer Mode
3. Load the repo root as an unpacked extension
4. Re-test after every content script change
