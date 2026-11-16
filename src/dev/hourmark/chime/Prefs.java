/*
 * Hour Beep
 * Copyright (C) 2026 ali HOSSEINZADEH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.hourmark.chime;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    static final String NAME = "hourmark";
    static final String ENABLED = "enabled";
    static final String QUIET = "quiet";
    static final String START = "start";
    static final String END = "end";
    static final String SOUND = "sound";
    static final String VOLUME = "volume";

    static final String SOUND_SOFT = "soft";
    static final String SOUND_BEEP_BEEP = "beep_beep";
    static final String SOUND_CASIO = "casio";
    static final String SOUND_CUCKOO = "cuckoo";

    private Prefs() {}

    static SharedPreferences get(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    static boolean enabled(Context context) {
        return get(context).getBoolean(ENABLED, true);
    }

    static boolean quiet(Context context) {
        return get(context).getBoolean(QUIET, true);
    }

    static int startMin(Context context) {
        return get(context).getInt(START, 22 * 60);
    }

    static int endMin(Context context) {
        return get(context).getInt(END, 7 * 60);
    }

    static int volume(Context context) {
        return get(context).getInt(VOLUME, 100);
    }

    static String sound(Context context) {
        return get(context).getString(SOUND, SOUND_CASIO);
    }

    static int soundRes(Context context) {
        String id = sound(context);
        if (SOUND_CUCKOO.equals(id)) {
            return R.raw.cuckoo;
        }
        if (SOUND_SOFT.equals(id)) {
            return R.raw.chime;
        }
        if (SOUND_BEEP_BEEP.equals(id)) {
            return R.raw.beep_beep;
        }
        return R.raw.casio;
    }
}
