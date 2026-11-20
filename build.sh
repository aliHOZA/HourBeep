#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-only
# Build Hour Beep 1.5.
# Needs: JDK 11 or newer, Python 3, Android SDK platform android-36,
# build-tools 34.0.0 (aapt2, zipalign), and R8 at cmdline-tools/latest/bin/r8
# or at the path in $R8.
set -e
SDK="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
BT="$SDK/build-tools/34.0.0"
AAPT="$BT/aapt2"
ZIPALIGN="$BT/zipalign"
JAR="$SDK/platforms/android-36/android.jar"
R8="${R8:-$SDK/cmdline-tools/latest/bin/r8}"
ROOT="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
cd "$ROOT"
rm -rf compiled gen classes dex linked.apk unsigned.apk classes.dex classes.jar
mkdir -p compiled gen classes dex
find res -type f | while read -r f; do
  "$AAPT" compile -o compiled/ "$f"
done
# ogg must stay uncompressed or MediaPlayer cannot read it from the apk
"$AAPT" link -I "$JAR" \
  -0 ogg \
  -0 wav \
  --manifest AndroidManifest.xml \
  --java gen \
  --min-sdk-version 26 \
  --target-sdk-version 36 \
  --version-code 1 \
  --version-name 1.5 \
  -o linked.apk \
  compiled/*
javac --release 11 -classpath "$JAR" -d classes gen/dev/hourbeep/R.java src/dev/hourbeep/*.java
jar cf classes.jar -C classes .
"$R8" --release --min-api 26 --lib "$JAR" --output dex --pg-conf proguard-rules.pro classes.jar
cp dex/classes.dex classes.dex
python3 - << 'PY'
import zipfile
with zipfile.ZipFile("linked.apk", "a") as z:
    z.write("classes.dex", "classes.dex")
PY
"$ZIPALIGN" -f 4 linked.apk unsigned.apk
echo "Built $ROOT/unsigned.apk"
echo "Sign it with your own keystore. The key used for the release APKs is not published."
