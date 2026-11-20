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

package dev.hourbeep;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import java.util.concurrent.atomic.AtomicBoolean;

final class Player {
    private static final long GIVE_UP_MS = 5000L;

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
            lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hourbeep:chime");
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

        final float gain = gainFor(Prefs.volume(app));
        final AtomicBoolean once = new AtomicBoolean(false);
        final Handler handler = new Handler(Looper.getMainLooper());
        final MediaPlayer[] held = new MediaPlayer[1];
        final Runnable giveUp = new Runnable() {
            @Override
            public void run() {
                finishOnce(once, handler, null, held, lock, audio, focus, done);
            }
        };
        handler.postDelayed(giveUp, GIVE_UP_MS);

        AssetFileDescriptor fd = null;
        try {
            MediaPlayer player = new MediaPlayer();
            held[0] = player;
            fd = app.getResources().openRawResourceFd(Prefs.soundRes(app));
            player.setAudioAttributes(attrs);
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            fd.close();
            fd = null;
            player.setVolume(gain, gain);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    finishOnce(once, handler, giveUp, held, lock, audio, focus, done);
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    finishOnce(once, handler, giveUp, held, lock, audio, focus, done);
                    return true;
                }
            });
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (once.get()) {
                        return;
                    }
                    mp.setVolume(gain, gain);
                    mp.start();
                }
            });
            player.prepareAsync();
        } catch (Exception error) {
            if (fd != null) {
                try {
                    fd.close();
                } catch (Exception ignored) {
                }
            }
            finishOnce(once, handler, giveUp, held, lock, audio, focus, done);
        }
    }

    private static void finishOnce(
            AtomicBoolean once,
            Handler handler,
            Runnable giveUp,
            MediaPlayer[] held,
            PowerManager.WakeLock lock,
            AudioManager audio,
            AudioFocusRequest focus,
            Runnable done) {
        if (!once.compareAndSet(false, true)) {
            return;
        }
        if (giveUp != null) {
            handler.removeCallbacks(giveUp);
        }
        MediaPlayer player = held[0];
        held[0] = null;
        if (player != null) {
            try {
                player.release();
            } catch (Exception ignored) {
            }
        }
        if (lock != null && lock.isHeld()) {
            lock.release();
        }
        if (audio != null && focus != null) {
            audio.abandonAudioFocusRequest(focus);
        }
        finish(done);
    }

    private static float gainFor(int percent) {
        if (percent <= 0) {
            return 0f;
        }
        if (percent >= 100) {
            return 1f;
        }
        double db = (percent - 100) * 0.4;
        return (float) Math.pow(10.0, db / 20.0);
    }

    private static void finish(Runnable done) {
        if (done != null) {
            done.run();
        }
    }
}
