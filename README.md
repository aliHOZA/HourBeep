# Hour Beep

A small Android hourly chime. One short sound at the top of each hour, with optional quiet hours. No network permission, no ads, no analytics.

Copyright (C) 2026 ali HOSSEINZADEH

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

## Versions

- **1.0** (`v1.0`) is the first release: one soft beep, quiet hours, exact alarms.
- **1.1** (`v1.1`) adds the original double beep, a cuckoo, audio ducking, and a battery-exemption button.
- **1.2** (`v1.2`) adds the real Casio recording and stores the short sounds as Ogg Vorbis. Both ends of a quiet-hours range are silent.
- **1.3** (`v1.3`, current) restores the original cuckoo, adds a loudness slider, and keeps an upgraded install on Beep Beep until the listener picks Casio.

The installable APK for each version is attached to that GitHub release. The private signing key is not in this repository. An APK you build and sign yourself will not update over a copy installed from the releases, unless you sign it with the same key.

## Sounds

| Name | File | Role |
| --- | --- | --- |
| Soft / نرم | `chime.ogg` | the original tone |
| Beep Beep / بیپ بیپ | `beep_beep.ogg` | the short digital double beep from 1.1 |
| Cuckoo / کو کو | `cuckoo.wav` | two descending notes, the original file |
| Casio / کاسیو | `casio.ogg` | the separate watch recording, default on a fresh install |

The three short Ogg files are 23,281 bytes. The cuckoo stays the original 38,852-byte WAV, because the Vorbis copy sounded thinner. A fresh install defaults to Casio. An update from an older install that never picked a sound keeps Beep Beep, which is what that install was already playing.

Quiet hours include both endpoints. From 22:00 to 07:00, the 22:00 chime and the 07:00 chime are both silent. The next chime is 08:00.

## Build

`build.sh` is the whole build. There is no Gradle project and no third-party library.

| Tool | Expected location |
| --- | --- |
| JDK 11 or newer | `javac` on `PATH` |
| Python 3 | `python3` on `PATH` |
| Android platform 36 | `$ANDROID_SDK_ROOT/platforms/android-36/android.jar` |
| build-tools 34.0.0 | `$ANDROID_SDK_ROOT/build-tools/34.0.0/aapt2` and `zipalign` |
| R8 | `$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/r8`, or `$R8` |

```sh
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
sh build.sh
```

The script compiles resources, links them with Ogg and WAV left uncompressed, dexes with R8, then runs `zipalign`. It writes `unsigned.apk` (versionCode 4, versionName 1.3). Sign that file with your own key. Tapping the system alarm icon opens the app. The chime itself is a separate broadcast. The loudness slider scales this app's chime against the phone's alarm volume. It does not change the system alarm volume.

