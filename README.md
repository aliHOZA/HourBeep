# Hour Beep

A small Android hourly chime. One short sound at the top of each hour, with optional quiet hours. No network permission, no ads, no analytics.

Copyright (C) 2026 ali HOSSEINZADEH

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

## Versions

- **1.0** (`v1.0`) is the first release: one soft beep, quiet hours, exact alarms.
- **1.1** (`v1.1`, current) adds a Casio-style double beep, a cuckoo, audio ducking, and a battery-exemption button.

The installable APK for each version is attached to that GitHub release. The private signing key is not in this repository. An APK you build and sign yourself will not update over a copy installed from the releases, unless you sign it with the same key.

## Sounds

- Soft beep, the original tone
- Casio beep, two short digital beeps, the default in 1.1
- Cuckoo, two descending notes

They are generated tones, not recordings taken from another app.

## Build

Install Android SDK platform 36 and build-tools 34, then:

```sh
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
sh build.sh
```

Sign `unsigned.apk` with your own key.
