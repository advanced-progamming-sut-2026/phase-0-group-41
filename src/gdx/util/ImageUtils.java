package gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * بارگذاری امن تصاویر.
 * اگر مسیر داده‌شده خالی باشد یا فایل وجود نداشته باشد،
 * به‌جای کرش کردن برنامه، یک بافت (Texture) رنگی جایگزین ساخته می‌شود
 * تا رابط گرافیکی قابل اجرا و تست باشد حتی پیش از اضافه‌شدن Asset های نهایی.
 */
public final class ImageUtils {

    private static final Map<String, Texture> CACHE = new HashMap<>();
    private static Texture placeholderTexture;

    private ImageUtils() {
    }

    /** بافت را از cache یا دیسک برمی‌گرداند؛ در نبود فایل، Placeholder رنگی می‌سازد. */
    public static Texture load(String path) {
        if (path == null || path.isEmpty()) {
            return getPlaceholder();
        }
        Texture cached = CACHE.get(path);
        if (cached != null) {
            return cached;
        }
        FileHandle handle = Gdx.files.internal(path);
        if (!handle.exists()) {
            Gdx.app.log("ImageUtils", "Asset file not found, using placeholder: " + path);
            return getPlaceholder();
        }
        Texture texture = new Texture(handle);
        CACHE.put(path, texture);
        return texture;
    }

    public static TextureRegion loadRegion(String path) {
        return new TextureRegion(load(path));
    }

    private static Texture getPlaceholder() {
        if (placeholderTexture == null) {
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0.35f, 0.35f, 0.4f, 1f));
            pixmap.fill();
            pixmap.setColor(Color.WHITE);
            pixmap.drawRectangle(0, 0, 64, 64);
            pixmap.drawLine(0, 0, 63, 63);
            pixmap.drawLine(0, 63, 63, 0);
            placeholderTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return placeholderTexture;
    }

    /** آزادسازی تمام بافت‌های بارگذاری‌شده؛ در dispose() کلاس اصلی بازی صدا زده شود. */
    public static void disposeAll() {
        for (Texture t : CACHE.values()) {
            t.dispose();
        }
        CACHE.clear();
        if (placeholderTexture != null) {
            placeholderTexture.dispose();
            placeholderTexture = null;
        }
    }
}
