package gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * بارگذاری امن تصاویر.
 * اگر مسیر داده‌شده خالی باشد یا فایل وجود نداشته باشد،
 * به‌جای کرش کردن برنامه، یک بافت (Texture) رنگی جایگزین ساخته می‌شود
 * تا رابط گرافیکی قابل اجرا و تست باشد حتی پیش از اضافه‌شدن Asset های نهایی.
 *
 * پشتیبانی از Atlas: علاوه بر مسیر ساده‌ی یک فایل PNG (مثل "ui/card_bg.png")،
 * می‌توان با فرمت ویژه‌ی "atlas:ATLAS_NAME:REGION_NAME" یک ناحیه (Region) مشخص
 * را از یک TextureAtlas واقعی (که در assets/atlases/ATLAS_NAME.atlas قرار دارد)
 * بارگذاری کرد. این همان فرمتی است که AssetPaths برای منابع رسمی بازی تولید می‌کند.
 */
public final class ImageUtils {

    private static final String ATLAS_PREFIX = "atlas:";
    private static final String ATLAS_DIR = "atlases/";

    private static final Map<String, Texture> CACHE = new HashMap<>();
    private static final Map<String, TextureAtlas> ATLAS_CACHE = new HashMap<>();
    private static Texture placeholderTexture;
    private static TextureRegion placeholderRegionCache;

    private ImageUtils() {
    }

    private static boolean isAtlasPath(String path) {
        return path != null && path.startsWith(ATLAS_PREFIX);
    }

    /** نام اتلس و نام ریجن را از رشته‌ی "atlas:ATLAS_NAME:REGION_NAME" استخراج می‌کند. */
    private static String[] splitAtlasPath(String path) {
        // path = "atlas:ATLAS_NAME:REGION_NAME" -> ["ATLAS_NAME", "REGION_NAME"]
        String rest = path.substring(ATLAS_PREFIX.length());
        int idx = rest.indexOf(':');
        if (idx < 0) {
            return null;
        }
        return new String[]{rest.substring(0, idx), rest.substring(idx + 1)};
    }

    private static TextureAtlas getAtlas(String atlasName) {
        TextureAtlas cached = ATLAS_CACHE.get(atlasName);
        if (cached != null) {
            return cached;
        }
        FileHandle handle = Gdx.files.internal(ATLAS_DIR + atlasName + ".atlas");
        if (!handle.exists()) {
            // نام‌های استخراج‌شده از منابع رسمی بازی معمولاً پسوند "_768_00" دارند
            // (مثلاً PLANTPEASHOOTER_768_00.atlas)؛ اگر مسیر بدون این پسوند پیدا نشد،
            // نسخه‌ی دارای پسوند را هم امتحان می‌کنیم تا مسیرهای کوتاه‌تر در AssetPaths هم کار کنند.
            FileHandle withSuffix = Gdx.files.internal(ATLAS_DIR + atlasName + "_768_00.atlas");
            if (withSuffix.exists()) {
                handle = withSuffix;
            }
        }
        if (!handle.exists()) {
            Gdx.app.log("ImageUtils", "Atlas file not found: " + handle.path());
            return null;
        }
        TextureAtlas atlas = new TextureAtlas(handle);
        ATLAS_CACHE.put(atlasName, atlas);
        return atlas;
    }

    /**
     * ناحیه‌ی مشخص‌شده در مسیر atlas را برمی‌گرداند؛ در نبود اتلس یا ریجن، یک
     * TextureRegion از بافت Placeholder رنگی برگردانده می‌شود.
     */
    private static TextureRegion loadAtlasRegion(String path) {
        String[] parts = splitAtlasPath(path);
        if (parts == null) {
            Gdx.app.log("ImageUtils", "Invalid atlas path format, using placeholder: " + path);
            return getPlaceholderRegion();
        }
        String atlasName = parts[0];
        String regionName = parts[1];
        TextureAtlas atlas = getAtlas(atlasName);
        if (atlas == null) {
            return getPlaceholderRegion();
        }
        TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);
        if (region == null) {
            Gdx.app.log("ImageUtils", "Region '" + regionName + "' not found in atlas '" + atlasName + "', using placeholder");
            return getPlaceholderRegion();
        }
        return region;
    }

    /**
     * بافت را از cache یا دیسک برمی‌گرداند؛ در نبود فایل، Placeholder رنگی می‌سازد.
     * توجه: برای مسیرهای atlas (فرمت "atlas:...")، از loadRegion استفاده کنید، نه از
     * این متد؛ این متد در آن حالت کل بافت زیرین اتلس (تمام صفحه) را برمی‌گرداند که
     * برای رسم مستقیم مناسب نیست.
     */
    public static Texture load(String path) {
        if (path == null || path.isEmpty()) {
            return getPlaceholder();
        }
        if (isAtlasPath(path)) {
            TextureRegion region = loadAtlasRegion(path);
            return region.getTexture();
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

    /**
     * روش صحیح و اصلی برای بارگذاری تصاویر در این پروژه. هم مسیر ساده‌ی یک PNG
     * تک و هم فرمت "atlas:ATLAS_NAME:REGION_NAME" را پشتیبانی می‌کند.
     */
    public static TextureRegion loadRegion(String path) {
        if (path == null || path.isEmpty()) {
            return getPlaceholderRegion();
        }
        if (isAtlasPath(path)) {
            return loadAtlasRegion(path);
        }
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

    private static TextureRegion getPlaceholderRegion() {
        if (placeholderRegionCache == null) {
            placeholderRegionCache = new TextureRegion(getPlaceholder());
        }
        return placeholderRegionCache;
    }

    /** آزادسازی تمام بافت‌ها و اتلس‌های بارگذاری‌شده؛ در dispose() کلاس اصلی بازی صدا زده شود. */
    public static void disposeAll() {
        for (Texture t : CACHE.values()) {
            t.dispose();
        }
        CACHE.clear();
        for (TextureAtlas a : ATLAS_CACHE.values()) {
            a.dispose();
        }
        ATLAS_CACHE.clear();
        if (placeholderTexture != null) {
            placeholderTexture.dispose();
            placeholderTexture = null;
        }
        placeholderRegionCache = null;
    }
}