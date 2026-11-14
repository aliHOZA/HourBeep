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
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends android.app.Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            render();
            handler.postDelayed(this, 1000L);
        }
    };

    private TextView clock;
    private TextView next;
    private TextView exact;
    private Switch enabled;
    private Switch quiet;
    private Button start;
    private Button end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clock = findViewById(R.id.clock);
        next = findViewById(R.id.next);
        exact = findViewById(R.id.exact);
        enabled = findViewById(R.id.enabled);
        quiet = findViewById(R.id.quiet);
        start = findViewById(R.id.start);
        end = findViewById(R.id.end);
        Button test = findViewById(R.id.test);

        enabled.setChecked(Prefs.enabled(this));
        quiet.setChecked(Prefs.quiet(this));

        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                Prefs.get(MainActivity.this).edit().putBoolean(Prefs.ENABLED, isChecked).apply();
                Scheduler.schedule(MainActivity.this);
                render();
            }
        });
        quiet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                Prefs.get(MainActivity.this).edit().putBoolean(Prefs.QUIET, isChecked).apply();
                Scheduler.schedule(MainActivity.this);
                render();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pick(true);
            }
        });
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pick(false);
            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Player.play(MainActivity.this, null);
            }
        });
        exact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askExact();
            }
        });
        Scheduler.schedule(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Scheduler.schedule(this);
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(tick);
        super.onPause();
    }

    private void pick(final boolean startTime) {
        int value = startTime ? Prefs.startMin(this) : Prefs.endMin(this);
        boolean is24 = DateFormat.is24HourFormat(this);
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(android.widget.TimePicker view, int hour, int minute) {
                int mins = hour * 60 + minute;
                Prefs.get(MainActivity.this).edit()
                        .putInt(startTime ? Prefs.START : Prefs.END, mins)
                        .apply();
                Scheduler.schedule(MainActivity.this);
                render();
            }
        }, value / 60, value % 60, is24);
        dialog.show();
    }

    private void askExact() {
        if (Build.VERSION.SDK_INT < 31) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception ignored) {
            try {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            } catch (Exception ignoredAgain) {
            }
        }
    }

    private void render() {
        Calendar now = Calendar.getInstance();
        clock.setText(DateFormat.getTimeFormat(this).format(now.getTime()));
        start.setText(getString(R.string.from) + "  " + formatMin(Prefs.startMin(this)));
        end.setText(getString(R.string.until) + "  " + formatMin(Prefs.endMin(this)));
        if (!Prefs.enabled(this)) {
            next.setText(R.string.next_off);
        } else {
            long when = Scheduler.nextTrigger(this, System.currentTimeMillis());
            next.setText(getString(R.string.next_at, DateFormat.getTimeFormat(this).format(when)));
        }
        boolean allowed = Scheduler.canExact(this);
        exact.setVisibility(allowed ? View.GONE : View.VISIBLE);
        if (!allowed) {
            exact.setText(R.string.exact_off);
        }
    }

    private String formatMin(int minuteOfDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60);
        calendar.set(Calendar.MINUTE, minuteOfDay % 60);
        calendar.set(Calendar.SECOND, 0);
        return DateFormat.getTimeFormat(this).format(calendar.getTime());
    }
}
