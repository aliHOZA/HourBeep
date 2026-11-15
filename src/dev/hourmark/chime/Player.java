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
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;

final class Player {
    private Player() {}

    static void play(Context context, final Runnable done) {
        final Context app = context.getApplicationContext();
        final AudioManager audio = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            int mode = audio.getMode();
            if (mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION) {
                finish(done);
                return;
            }
        }

        final PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock lock;
        if (pm != null) {
            lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hourmark:chime");
            lock.setReferenceCounted(false);
            lock.acquire(10000L);
        } else {
            lock = null;
        }

        final AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        final AudioFocusRequest focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attrs)
                .build();
        if (audio != null) {
            audio.requestAudioFocus(focus);
        }

        MediaPlayer player = null;
        try {
            player = new MediaPlayer();
            final MediaPlayer playing = player;
            AssetFileDescriptor fd = app.getResources().openRawResourceFd(Prefs.soundRes(app));
            playing.setAudioAttributes(attrs);
            playing.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            fd.close();
            playing.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    release(mp, lock, audio, focus);
                    finish(done);
                }
            });
            playing.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    release(mp, lock, audio, focus);
                    finish(done);
                    return true;
                }
            });
            playing.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            playing.prepareAsync();
        } catch (Exception error) {
            if (player != null) {
                try {
                    player.release();
                } catch (Exception ignored) {
                }
            }
            if (lock != null && lock.isHeld()) {
                lock.release();
            }
            if (audio != null) {
                audio.abandonAudioFocusRequest(focus);
            }
            finish(done);
        }
    }

    private static void finish(Runnable done) {
        if (done != null) {
            done.run();
        }
    }

    private static void release(MediaPlayer player, PowerManager.WakeLock lock, AudioManager audio, AudioFocusRequest focus) {
        try {
            player.release();
        } catch (Exception ignored) {
        }
        if (lock != null && lock.isHeld()) {
            lock.release();
        }
        if (audio != null && focus != null) {
            audio.abandonAudioFocusRequest(focus);
        }
    }
}
