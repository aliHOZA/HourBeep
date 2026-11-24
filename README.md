# Hour Beep

A small Android hourly chime. One short sound at the top of each hour, with optional quiet hours. No network permission, no ads, no analytics.

Copyright (C) 2026 ali HOSSEINZADEH

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

## Donations

The app stays free. If you want to support it:

- [Liberapay](https://liberapay.com/HSNZDH)
- [Open Collective](https://opencollective.com/hsnzdh)

## Versions

- **1.0** (`v1.0`) is the first release: one soft beep, quiet hours, exact alarms.
- **1.1** (`v1.1`) adds the original double beep, a cuckoo, audio ducking, and a battery-exemption button.
- **1.2** (`v1.2`) adds the real Casio recording and stores the short sounds as Ogg Vorbis. Both ends of a quiet-hours range are silent.
- **1.3** (`v1.3`) restores the original cuckoo and adds a loudness slider.
- **1.4** (`v1.4`) drops the sound-migration code. The slider is logarithmic and saves when the finger lifts.
- **1.4.9** (`v1.4.9`) keeps that behavior and replaces the icon with the dial that reads hour and beep.
- **1.5** (`v1.5`) changes the Application ID to `dev.hourbeep`. It does not update over 1.4.x installs.
- **1.6** (`v1.6`, current) adds a formal About dialog with developer name, features, and privacy.

The installable APK for each version is attached to that GitHub release. The private signing key is not in this repository. An APK you build and sign yourself will not update over a copy installed from the releases, unless you sign it with the same key.

## Sounds

| Name | File | Role |
| --- | --- | --- |
| Soft / نرم | `chime.ogg` | the original tone |
| Beep Beep / بیپ بیپ | `beep_beep.ogg` | the short digital double beep from 1.1 |
| Cuckoo / کو کو | `cuckoo.wav` | two descending notes, the original file |
| Casio / کاسیو | `casio.ogg` | the separate watch recording, default on a fresh install |

## Build

There is no Gradle project. Use `build.sh` with the Android SDK (platform android-36 and build-tools 34.0.0).

The script compiles resources, links them with Ogg and WAV left uncompressed, dexes with R8, then runs `zipalign`. It writes `unsigned.apk` (versionCode 2, versionName 1.6). Sign that file with your own key.
