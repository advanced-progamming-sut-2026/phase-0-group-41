package gdx.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
<<<<<<< HEAD
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.SoundManager;
=======

import gdx.PvZGame;
import gdx.assets.AssetPaths;
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a

/**
 * منوی توقف بازی. طبق سند: کاربر باید بتواند با کلیک یا فشردن یک دکمه/کلید
 * بازی را متوقف کند؛ بازی باید کاملاً ثابت بماند (مگر اینکه در فاز یک شبکه
 * پیاده‌سازی شده باشد) و موجودیت‌ها/انیمیشن‌ها حرکتی نداشته باشند.
 * <p>
 * این کلاس فقط منطق UI منوی توقف را دارد و به‌جای صفحه‌ی گیم‌پلی واقعی
 * (که هنوز ساخته نشده) با یک {@link Screen} دلخواه که باید هنگام Resume به آن
 * برگردیم و یک Runnable برای Restart کار می‌کند؛ وقتی GameScreen واقعی ساخته
 * شد، همان‌جا این کلاس با previousScreen/onRestart واقعی صدا زده می‌شود.
 */
public class PauseScreen extends BaseMenuScreen {

    private final Screen previousScreen;
    private final Runnable onRestart;

    public PauseScreen(PvZGame game, Screen previousScreen, Runnable onRestart) {
        super(game);
        this.previousScreen = previousScreen;
        this.onRestart = onRestart;

        rootTable.add(title("Game Paused")).padBottom(20f).row();

        Table form = new Table();

        Slider musicSlider = new Slider(0f, 1f, 0.05f, false, skin);
<<<<<<< HEAD
        musicSlider.setValue(SoundManager.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundManager.setMusicVolume(musicSlider.getValue());
            }
        });
=======
        musicSlider.setValue(1f);
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        form.add(new Label("Music:", skin)).right().padRight(10f).padBottom(10f);
        form.add(musicSlider).width(220f).padBottom(10f).row();

        Slider sfxSlider = new Slider(0f, 1f, 0.05f, false, skin);
<<<<<<< HEAD
        sfxSlider.setValue(SoundManager.getSoundVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundManager.setSoundVolume(sfxSlider.getValue());
            }
        });
=======
        sfxSlider.setValue(1f);
>>>>>>> 5d404d1a02ab01c27673ae3e6350a8f1f059068a
        form.add(new Label("Sound FX:", skin)).right().padRight(10f).padBottom(10f);
        form.add(sfxSlider).width(220f).padBottom(10f).row();

        CheckBox hitboxBox = new CheckBox(" Show grid / hitboxes", skin);
        form.add().padRight(10f);
        form.add(hitboxBox).left().padBottom(10f).row();

        rootTable.add(form).padBottom(20f).row();

        Table buttons = new Table();
        addButton(buttons, "Resume", this::doResume);
        addButton(buttons, "Restart", this::doRestart);
        addButton(buttons, "Save and Exit", this::doSaveAndExit);
        rootTable.add(buttons).row();
    }

    private void doResume() {
        if (previousScreen != null) {
            game.setScreen(previousScreen);
        } else {
            game.goToMainMenu();
        }
    }

    private void doRestart() {
        if (onRestart != null) {
            onRestart.run();
        } else if (previousScreen != null) {
            game.setScreen(previousScreen);
        }
    }

    private void doSaveAndExit() {
        game.getUserManager().save();
        game.goToMainMenu();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_SETTINGS;
    }
}
