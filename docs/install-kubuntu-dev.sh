#!/bin/bash
set -e

echo "Starting Breathe Kubuntu Dev Setup..."

echo "Installing base packages..."
sudo apt update
sudo apt install -y build-essential openjdk-17-jdk git curl unzip zip ca-certificates qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils inotify-tools

echo "Adding user to kvm and libvirt groups..."
sudo usermod -aG kvm,libvirt "$USER"

echo "Installing nvm and Node.js LTS..."
curl -fsSL https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
nvm install --lts
nvm use --lts

if ! grep -q "export JAVA_HOME=\"/usr/lib/jvm/java-17-openjdk-amd64\"" ~/.bashrc; then
  echo "Adding JAVA_HOME to .bashrc..."
  echo '' >> ~/.bashrc
  echo '# Java for Android' >> ~/.bashrc
  echo 'export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"' >> ~/.bashrc
  echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
fi
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Setting up Android SDK and Command-Line Tools..."
mkdir -p "$HOME/Android/Sdk/cmdline-tools"
cd "$HOME/Android/Sdk/cmdline-tools"

if [ ! -d "latest" ]; then
  wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
  unzip cmdline-tools.zip
  mv cmdline-tools latest
  rm cmdline-tools.zip
fi

if ! grep -q "export ANDROID_HOME=\"\$HOME/Android/Sdk\"" ~/.bashrc; then
  echo "Adding Android paths to .bashrc..."
  echo '' >> ~/.bashrc
  echo '# Android SDK' >> ~/.bashrc
  echo 'export ANDROID_HOME="$HOME/Android/Sdk"' >> ~/.bashrc
  echo 'export ANDROID_SDK_ROOT="$ANDROID_HOME"' >> ~/.bashrc
  echo 'export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"' >> ~/.bashrc
  echo 'export PATH="$ANDROID_HOME/platform-tools:$PATH"' >> ~/.bashrc
  echo 'export PATH="$ANDROID_HOME/emulator:$PATH"' >> ~/.bashrc
fi

export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
export PATH="$ANDROID_HOME/emulator:$PATH"

echo "Installing Android SDK packages..."
yes | sdkmanager --licenses
sdkmanager "platform-tools" "build-tools;35.0.0" "platforms;android-35" "system-images;android-35;google_apis;x86_64" "emulator"

echo "Creating Breathe_Pixel emulator..."
echo "no" | avdmanager create avd -n Breathe_Pixel -k "system-images;android-35;google_apis;x86_64" --device "pixel_7" --force

echo "Setup complete! Please log out and back in to apply kvm/libvirt group changes and reload your shell."
