package gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import gdx.assets.AssetPaths;

/**
 * اگر فایل Skin واقعی (JSON + Atlas) در AssetPaths.UI_SKIN_JSON مشخص شده باشد،
 * همان بارگذاری می‌شود. در غیر این صورت یک Skin پایه و کاملاً کاربردی
 * به‌صورت برنامه‌نویسی‌شده ساخته می‌شود تا تمام صفحات بدون نیاز به asset
 * قابل اجرا و تست باشند.
 *
 * این فایل استایل تمام ویجت‌هایی که در صفحات gdx/screens استفاده می‌شوند را
 * پوشش می‌دهد: Label, TextButton, TextField, Window, ProgressBar, ScrollPane,
 * List, SelectBox, CheckBox, Slider — همه با نام "default" (یا
 * "default-horizontal" برای ProgressBar/Slider) ثبت شده‌اند.
 *
 * فونت‌ها: قبلاً همه‌جا از یک new BitmapFont() (فونت پیش‌فرض بسیار کوچک
 * libGDX) استفاده می‌شد. حالا شش فونت واقعی بازی (assets/fonts، از
 * AssetPaths.FONT_* خوانده می‌شوند) هرکدام برای نقش مناسب خودشان با
 * FontManager (gdx-freetype) تولید و به‌عنوان یک LabelStyle/TextButtonStyle
 * جداگانه ثبت می‌شوند تا صفحات بتوانند با new Label(text, skin, "styleName")
 * فونت مناسب را انتخاب کنند. نگاشت کامل نقش← فونت در AssetPaths مستند شده.
 */
public final class SkinFactory {

    private SkinFactory() {
    }

    public static Skin create() {
        if (!AssetPaths.UI_SKIN_JSON.isEmpty()) {
            FileHandle json = Gdx.files.internal(AssetPaths.UI_SKIN_JSON);
            if (json.exists()) {
                return new Skin(json);
            }
        }
        return buildDefaultSkin();
    }

    private static Skin buildDefaultSkin() {
        Skin skin = new Skin();

        // --- فونت اصلی رابط کاربری: Avenir Next LT Pro Demi Condensed ---
        // همان فونت متن عمومی PvZ2 اصلی؛ روی تمام Label/Button/TextField/...
        // با نام "default" اعمال می‌شود.
        BitmapFont font = FontManager.generate(AssetPaths.FONT_BODY, 24, Color.WHITE);
        skin.add("default-font", font, BitmapFont.class);

        // --- فونت تیتر: fbUsv8C5eI Black (فونت درشت/توپر) ---
        // برای عنوان هر صفحه (title() در BaseMenuScreen) و زیرتیترهای داخلی
        // مثل "Cheat Codes" و "Daily Offer".
        BitmapFont titleFont = FontManager.generate(AssetPaths.FONT_TITLE, 40,
                new Color(1f, 0.85f, 0.2f, 1f), 2, new Color(0.25f, 0.12f, 0f, 1f));
        skin.add("title-font", titleFont, BitmapFont.class);

        // --- همان فونت تیتر ولی در اندازه‌ی کوچک‌تر، مخصوص نوار وضعیت جمع‌وجور
        // بالای صفحه‌ی بازی (فصل/مرحله/موج در GameScreen.chapterLevelWaveLabel).
        // قبلاً این لیبل هم از استایل "title" (۴۰px) استفاده می‌کرد که در نوار
        // باریک بالای صفحه جا نمی‌شد و از بالای صفحه بیرون می‌زد/کلیپ می‌شد —
        // به همین خاطر متن فصل/مرحله/موج دیده نمی‌شد؛ با این فونت کوچک‌تر (۲۲px،
        // بدون حاشیه‌ی ضخیم) کاملاً داخل یک خط جا می‌شود.
        BitmapFont hudTitleFont = FontManager.generate(AssetPaths.FONT_TITLE, 22,
                new Color(1f, 0.85f, 0.2f, 1f));
        skin.add("hud-title-font", hudTitleFont, BitmapFont.class);

        // --- فونت پیکسلی HUD: Pico12 ---
        // برای شمارنده‌های عددی (سکه، الماس، خورشید، غذای گیاه، موج) که باید
        // در اندازه‌ی کوچک هم کاملاً خوانا بمانند.
        BitmapFont hudFont = FontManager.generate(AssetPaths.FONT_PIXEL_HUD, 20, Color.WHITE);
        skin.add("hud-font", hudFont, BitmapFont.class);

        // --- فونت وحشت: MonsterFonts - House of Terror ---
        // برای اعلان قرمز وسط صفحه (شروع موج/موج نهایی) و بنر "You Lose!".
        BitmapFont horrorFont = FontManager.generate(AssetPaths.FONT_HORROR, 44,
                Color.RED, 2, Color.BLACK);
        skin.add("horror-font", horrorFont, BitmapFont.class);

        // --- فونت دست‌نویس: Ashley Script MT Std ---
        // برای متن دیالوگ داستانی (DialogueScreen).
        BitmapFont scriptFont = FontManager.generate(AssetPaths.FONT_SCRIPT, 30,
                new Color(0.95f, 0.95f, 0.85f, 1f));
        skin.add("script-font", scriptFont, BitmapFont.class);

        // --- فونت بازیگوش: BrianneTod ---
        // برای پیام‌های کوتاه/toast حین بازی و نام گوینده در دیالوگ.
        BitmapFont playfulFont = FontManager.generate(AssetPaths.FONT_PLAYFUL, 22,
                new Color(1f, 0.95f, 0.6f, 1f));
        skin.add("playful-font", playfulFont, BitmapFont.class);

        skin.add("white", solidTexture(Color.WHITE), Texture.class);
        skin.add("panel-bg", solidTexture(new Color(0.12f, 0.12f, 0.16f, 0.92f)), Texture.class);
        skin.add("btn-up", solidTexture(new Color(0.20f, 0.45f, 0.20f, 1f)), Texture.class);
        skin.add("btn-down", solidTexture(new Color(0.12f, 0.30f, 0.12f, 1f)), Texture.class);
        skin.add("btn-disabled", solidTexture(new Color(0.3f, 0.3f, 0.3f, 1f)), Texture.class);
        skin.add("field-bg", solidTexture(new Color(1f, 1f, 1f, 0.9f)), Texture.class);
        skin.add("progress-bg", solidTexture(new Color(0.2f, 0.2f, 0.2f, 1f)), Texture.class);
        skin.add("progress-fill", solidTexture(new Color(0.9f, 0.7f, 0.1f, 1f)), Texture.class);
        skin.add("cursor", solidTexture(Color.BLACK), Texture.class);
        skin.add("selection", solidTexture(new Color(0.3f, 0.5f, 0.9f, 0.5f)), Texture.class);
        skin.add("checkbox-off", solidTexture(new Color(0.3f, 0.3f, 0.35f, 1f)), Texture.class);
        skin.add("checkbox-on", solidTexture(new Color(0.9f, 0.7f, 0.1f, 1f)), Texture.class);

        // Drawable عمومی به نام "default" — دقیقاً با نوع Drawable ثبت می‌شود
        // تا هر جای دیگری از کد (مثل skin.getDrawable("default") در HudBar)
        // بدون خطای "No Drawable ... registered" کار کند.
        Drawable defaultDrawable = new TextureRegionDrawable(new TextureRegion(skin.get("panel-bg", Texture.class)));
        skin.add("default", defaultDrawable, Drawable.class);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        // عنوان هر صفحه/بخش: title(...) در BaseMenuScreen و همه‌ی
        // new Label(text, skin, "title") پراکنده در صفحات از این استفاده می‌کنند.
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, new Color(1f, 0.85f, 0.2f, 1f));
        skin.add("title", titleStyle);

        // نسخه‌ی کوچک تیتر، مخصوص نوار وضعیت «فصل - مرحله | Wave x/y» بالای
        // صفحه‌ی بازی (GameScreen)؛ به‌جای استایل "title" که برای این نوار
        // باریک خیلی بزرگ بود و متن را از دید خارج می‌کرد.
        Label.LabelStyle hudTitleStyle = new Label.LabelStyle(hudTitleFont, new Color(1f, 0.85f, 0.2f, 1f));
        skin.add("hud-title", hudTitleStyle);

        Label.LabelStyle errorStyle = new Label.LabelStyle(font, new Color(1f, 0.3f, 0.3f, 1f));
        skin.add("error", errorStyle);

        // شمارنده‌های عددی HUD (سکه/الماس/خورشید/غذای گیاه/موج).
        Label.LabelStyle hudNumberStyle = new Label.LabelStyle(hudFont, Color.WHITE);
        skin.add("hud-number", hudNumberStyle);

        // اعلان قرمز وسط صفحه‌ی بازی (شروع موج، موج نهایی) و بنر "You Lose!".
        Label.LabelStyle horrorStyle = new Label.LabelStyle(horrorFont, Color.RED);
        skin.add("horror", horrorStyle);

        // متن دیالوگ داستانی.
        Label.LabelStyle dialogueTextStyle = new Label.LabelStyle(scriptFont, new Color(0.95f, 0.95f, 0.85f, 1f));
        skin.add("dialogue-text", dialogueTextStyle);

        // نام گوینده در دیالوگ + پیام‌های toast کوتاه حین بازی (جمع‌آوری سکه/
        // غذای گیاه/گلدان و ...).
        Label.LabelStyle playfulStyle = new Label.LabelStyle(playfulFont, new Color(1f, 0.95f, 0.6f, 1f));
        skin.add("playful", playfulStyle);
        skin.add("toast", playfulStyle);
        skin.add("dialogue-speaker", playfulStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = new TextureRegionDrawable(new TextureRegion(skin.get("btn-up", Texture.class)));
        buttonStyle.down = new TextureRegionDrawable(new TextureRegion(skin.get("btn-down", Texture.class)));
        buttonStyle.disabled = new TextureRegionDrawable(new TextureRegion(skin.get("btn-disabled", Texture.class)));
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.disabledFontColor = new Color(0.7f, 0.7f, 0.7f, 1f);
        skin.add("default", buttonStyle);

        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle();
        fieldStyle.font = font;
        fieldStyle.fontColor = Color.BLACK;
        fieldStyle.background = new TextureRegionDrawable(new TextureRegion(skin.get("field-bg", Texture.class)));
        fieldStyle.cursor = new TextureRegionDrawable(new TextureRegion(skin.get("cursor", Texture.class)));
        fieldStyle.selection = new TextureRegionDrawable(new TextureRegion(skin.get("selection", Texture.class)));
        skin.add("default", fieldStyle);

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = font;
        windowStyle.titleFontColor = Color.WHITE;
        windowStyle.background = new TextureRegionDrawable(new TextureRegion(skin.get("panel-bg", Texture.class)));
        skin.add("default", windowStyle);

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = new TextureRegionDrawable(new TextureRegion(skin.get("progress-bg", Texture.class)));
        barStyle.knobBefore = new TextureRegionDrawable(new TextureRegion(skin.get("progress-fill", Texture.class)));
        skin.add("default-horizontal", barStyle);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollStyle);

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = font;
        listStyle.fontColorSelected = Color.YELLOW;
        listStyle.fontColorUnselected = Color.WHITE;
        listStyle.selection = new TextureRegionDrawable(new TextureRegion(skin.get("selection", Texture.class)));
        skin.add("default", listStyle);

        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = font;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.background = new TextureRegionDrawable(new TextureRegion(skin.get("btn-up", Texture.class)));
        selectBoxStyle.scrollStyle = scrollStyle;
        selectBoxStyle.listStyle = listStyle;
        skin.add("default", selectBoxStyle);

        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.font = font;
        checkBoxStyle.fontColor = Color.WHITE;
        checkBoxStyle.checkboxOff = new TextureRegionDrawable(new TextureRegion(skin.get("checkbox-off", Texture.class)));
        checkBoxStyle.checkboxOn = new TextureRegionDrawable(new TextureRegion(skin.get("checkbox-on", Texture.class)));
        skin.add("default", checkBoxStyle);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = new TextureRegionDrawable(new TextureRegion(skin.get("progress-bg", Texture.class)));
        sliderStyle.knob = new TextureRegionDrawable(new TextureRegion(skin.get("progress-fill", Texture.class)));
        skin.add("default-horizontal", sliderStyle);

        return skin;
    }

    private static Texture solidTexture(Color color) {
        Pixmap pixmap = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
