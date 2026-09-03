package gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * تولید BitmapFont از فایل‌های .TTF داخل assets/fonts با gdx-freetype.
 *
 * چرا اینجا و نه یک فایل .fnt/.png از پیش تولید‌شده؟ چون در این پروژه فقط
 * ۶ فونت TTF داریم و هرکدام باید در چند اندازه/رنگ متفاوت استفاده شوند
 * (مثلاً فونت تیتر هم برای عنوان صفحات و هم برای بنر "You Win!" با اندازه‌ی
 * بزرگ‌تر لازم است)؛ تولید در زمان اجرا با FreeTypeFontGenerator ساده‌تر و
 * قابل نگهداری‌تر از نگه‌داشتن چند فایل .fnt از پیش رندرشده است.
 *
 * اگر فایل فونت موجود نباشد (مثلاً هنوز assets کپی نشده)، به‌جای کرش کردن،
 * یک BitmapFont ساده‌ی پیش‌فرض برگردانده می‌شود تا بقیه‌ی صفحات همچنان قابل
 * اجرا/تست باشند؛ دقیقاً همان فلسفه‌ای که در بقیه‌ی SkinFactory دنبال شده.
 */
public final class FontManager {

    private FontManager() {
    }

    /** ساخت یک فونت ساده بدون حاشیه/سایه. */
    public static BitmapFont generate(String ttfPath, int sizePx, Color color) {
        return generate(ttfPath, sizePx, color, 0, null);
    }

    /**
     * ساخت فونت با حاشیه‌ی اختیاری (برای خوانایی روی پس‌زمینه‌های شلوغ بازی؛
     * مثلاً تیترها و اعلان قرمز وسط صفحه).
     */
    public static BitmapFont generate(String ttfPath, int sizePx, Color color,
                                       int borderWidthPx, Color borderColor) {
        FileHandle file = Gdx.files.internal(ttfPath);
        if (ttfPath == null || ttfPath.isEmpty() || !file.exists()) {
            return fallbackFont(color);
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(file);
        try {
            FreeTypeFontParameter param = new FreeTypeFontParameter();
            param.size = sizePx;
            param.color = color;
            param.minFilter = Texture.TextureFilter.Linear;
            param.magFilter = Texture.TextureFilter.Linear;
            if (borderWidthPx > 0 && borderColor != null) {
                param.borderWidth = borderWidthPx;
                param.borderColor = borderColor;
            }
            return generator.generateFont(param);
        } finally {
            // طبق مستندات libGDX، بعد از تولید فونت، خود Generator باید dispose
            // شود؛ BitmapFont تولیدشده مستقل و قابل‌استفاده باقی می‌ماند و مسئولیت
            // dispose آن به‌عهده‌ی SkinFactory/Skin است (اسکین همه‌ی فونت‌های
            // اضافه‌شده با skin.add را در پایان dispose می‌کند).
            generator.dispose();
        }
    }

    private static BitmapFont fallbackFont(Color color) {
        BitmapFont font = new BitmapFont();
        font.setColor(color);
        return font;
    }
}
