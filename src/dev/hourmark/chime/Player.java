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
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.PowerManager;

final class Player {
    private Player() {}

    static void play(Context context, final Runnable done) {
        final Context app = context.getApplicationContext();
        final PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock lock;
        if (pm != null) {
            lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hourmark:chime");
            lock.setReferenceCounted(false);
            lock.acquire(8000L);
        } else {
            lock = null;
        }
        try {
            final MediaPlayer player = new MediaPlayer();
            android.content.res.AssetFileDescriptor fd = app.getResources().openRawResourceFd(R.raw.chime);
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            fd.close();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    release(mp, lock);
                    if (done != null) {
                        done.run();
                    }
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    release(mp, lock);
                    if (done != null) {
                        done.run();
                    }
                    return true;
                }
            });
            player.prepare();
            player.start();
        } catch (Exception error) {
            if (lock != null && lock.isHeld()) {
                lock.release();
            }
            if (done != null) {
                done.run();
            }
        }
    }

    private static void release(MediaPlayer player, PowerManager.WakeLock lock) {
        try {
            player.release();
        } catch (Exception ignored) {
        }
        if (lock != null && lock.isHeld()) {
            lock.release();
        }
    }
}
