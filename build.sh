#!/bin/sh
# Build Hour Beep 1.0. Requires Android SDK build-tools 34 and platform android-36.
set -e
SDK="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
AAPT="$SDK/build-tools/34.0.0/aapt2"
JAR="$SDK/platforms/android-36/android.jar"
R8="$SDK/cmdline-tools/latest/bin/r8"
ROOT="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
cd "$ROOT"
rm -rf compiled gen classes dex unsigned.apk classes.dex classes.jar
mkdir -p compiled gen classes dex
find res -type f | while read -r f; do
  "$AAPT" compile -o compiled/ "$f"
done
"$AAPT" link -I "$JAR" \
  --manifest AndroidManifest.xml \
  --java gen \
  --min-sdk-version 26 \
  --target-sdk-version 36 \
  --version-code 1 \
  --version-name 1.0 \
  -o unsigned.apk \
  compiled/*
javac --release 11 -classpath "$JAR" -d classes gen/dev/hourmark/chime/R.java src/dev/hourmark/chime/*.java
jar cf classes.jar -C classes .
"$R8" --release --min-api 26 --lib "$JAR" --output dex --pg-conf proguard-rules.pro classes.jar
cp dex/classes.dex classes.dex
python3 - << 'PY'
import zipfile
with zipfile.ZipFile("unsigned.apk", "a") as z:
    z.write("classes.dex", "classes.dex")
PY
echo "Built $ROOT/unsigned.apk"
echo "Sign it with your own keystore. Do not publish the private key."
