# Hour Beep

A small Android hourly chime. One short sound at the top of each hour, with optional quiet hours. No network permission, no ads, no analytics.

Copyright (C) 2026 ali HOSSEINZADEH

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

## Version 1.0

One soft beep, quiet hours, and exact alarms. The installable APK is attached to the `v1.0` release. The private signing key is not in this repository.

## Build

Install Android SDK platform 36 and build-tools 34, then:

```sh
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
sh build.sh
```

Sign `unsigned.apk` with your own key. A build signed with another key will not update over the release APK.
