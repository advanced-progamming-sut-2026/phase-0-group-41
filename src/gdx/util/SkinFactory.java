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

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.2f);
        skin.add("default-font", font, BitmapFont.class);

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

        Label.LabelStyle titleStyle = new Label.LabelStyle(font, new Color(1f, 0.85f, 0.2f, 1f));
        skin.add("title", titleStyle);

        Label.LabelStyle errorStyle = new Label.LabelStyle(font, new Color(1f, 0.3f, 0.3f, 1f));
        skin.add("error", errorStyle);

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
