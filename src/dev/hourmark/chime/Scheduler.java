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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

final class Scheduler {
    private static final int REQUEST = 7;

    private Scheduler() {}

    static long nextTrigger(Context context, long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        if (!Prefs.quiet(context)) {
            return calendar.getTimeInMillis();
        }
        int start = Prefs.startMin(context);
        int end = Prefs.endMin(context);
        for (int i = 0; i < 48; i++) {
            int mins = calendar.get(Calendar.HOUR_OF_DAY) * 60;
            if (!inQuiet(mins, start, end)) {
                return calendar.getTimeInMillis();
            }
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        return calendar.getTimeInMillis();
    }

    static boolean inQuiet(int minuteOfDay, int start, int end) {
        if (start == end) {
            return false;
        }
        if (start < end) {
            return minuteOfDay >= start && minuteOfDay < end;
        }
        return minuteOfDay >= start || minuteOfDay < end;
    }

    static void schedule(Context context) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) {
            return;
        }
        PendingIntent operation = pending(context);
        if (!Prefs.enabled(context)) {
            alarms.cancel(operation);
            return;
        }
        long when = nextTrigger(context, System.currentTimeMillis());
        PendingIntent showIntent = showIntent(context);
        try {
            alarms.setAlarmClock(
                new AlarmManager.AlarmClockInfo(when, showIntent),
                operation
            );
        } catch (SecurityException exactBlocked) {
            try {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, operation);
            } catch (SecurityException ignored) {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, operation);
            }
        }
    }

    static boolean canExact(Context context) {
        if (Build.VERSION.SDK_INT < 31) {
            return true;
        }
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarms != null && alarms.canScheduleExactAlarms();
    }

    private static PendingIntent pending(Context context) {
        Intent intent = new Intent(context, ChimeReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, REQUEST, intent, flags);
    }

    private static PendingIntent showIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, REQUEST, intent, flags);
    }
}
