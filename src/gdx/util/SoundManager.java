package gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * پخش صداهای کوتاه (Sound FX) و موسیقی پس‌زمینه (Music).
 * دقیقاً هم‌ارز ImageUtils است: بارگذاری تنبل (Lazy) + کش، و در نبود فایل
 * به‌جای کرش کردن فقط یک پیام لاگ می‌زند و کاری انجام نمی‌دهد.
 *
 * حجم صدا از طریق setSoundVolume/setMusicVolume قابل تنظیم است (طبق اسلایدرهای
 * Music/Sound FX در PauseScreen که در سند فاز دو ذکر شده‌اند).
 */
public final class SoundManager {

    private static final Map<String, Sound> SOUND_CACHE = new HashMap<>();
    private static final Map<String, Music> MUSIC_CACHE = new HashMap<>();

    private static Music currentMusic;
    private static String currentMusicPath;

    private static float soundVolume = 1f;
    private static float musicVolume = 1f;

    private SoundManager() {
    }

    /** یک صدای کوتاه (کلیک، شلیک، برخورد و ...) را یک‌بار پخش می‌کند. */
    public static void playSound(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        Sound sound = SOUND_CACHE.get(path);
        if (sound == null) {
            FileHandle handle = Gdx.files.internal(path);
            if (!handle.exists()) {
                Gdx.app.log("SoundManager", "Sound file not found: " + path);
                return;
            }
            sound = Gdx.audio.newSound(handle);
            SOUND_CACHE.put(path, sound);
        }
        sound.play(soundVolume);
    }

    /**
     * موسیقی پس‌زمینه‌ی داده‌شده را به‌صورت حلقه‌ای (Loop) پخش می‌کند.
     * اگر همان مسیر از قبل در حال پخش باشد، کاری تکرار نمی‌شود؛ در غیر این صورت
     * موسیقی قبلی متوقف و آزاد می‌شود (طبق منطق «فقط یک موسیقی پس‌زمینه هم‌زمان»).
     */
    public static void playMusic(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        if (path.equals(currentMusicPath) && currentMusic != null && currentMusic.isPlaying()) {
            return; // از قبل در حال پخش است
        }
        stopMusic();

        Music music = MUSIC_CACHE.get(path);
        if (music == null) {
            FileHandle handle = Gdx.files.internal(path);
            if (!handle.exists()) {
                Gdx.app.log("SoundManager", "Music file not found: " + path);
                return;
            }
            music = Gdx.audio.newMusic(handle);
            MUSIC_CACHE.put(path, music);
        }
        music.setLooping(true);
        music.setVolume(musicVolume);
        music.play();
        currentMusic = music;
        currentMusicPath = path;
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = null;
        currentMusicPath = null;
    }

    /** حجم صداهای کوتاه را تنظیم می‌کند (بازه‌ی ۰ تا ۱، هم‌راستا با اسلایدر Sound FX). */
    public static void setSoundVolume(float volume) {
        soundVolume = clamp01(volume);
    }

    /** حجم موسیقی پس‌زمینه را تنظیم می‌کند (بازه‌ی ۰ تا ۱، هم‌راستا با اسلایدر Music). */
    public static void setMusicVolume(float volume) {
        musicVolume = clamp01(volume);
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public static float getSoundVolume() {
        return soundVolume;
    }

    public static float getMusicVolume() {
        return musicVolume;
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    /** آزادسازی تمام صداها و موسیقی‌های بارگذاری‌شده؛ در dispose() کلاس اصلی بازی صدا زده شود. */
    public static void disposeAll() {
        for (Sound s : SOUND_CACHE.values()) {
            s.dispose();
        }
        SOUND_CACHE.clear();
        for (Music m : MUSIC_CACHE.values()) {
            m.dispose();
        }
        MUSIC_CACHE.clear();
        currentMusic = null;
        currentMusicPath = null;
    }
}
