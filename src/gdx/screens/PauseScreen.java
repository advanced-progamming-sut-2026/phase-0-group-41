package gdx.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.SoundManager;
import model.game.Board;
import model.game.GameSession;
import model.user.User;
import model.zombie.Zombie;

/**
 * منوی توقف بازی. طبق سند: کاربر باید بتواند با کلیک یا فشردن یک دکمه/کلید
 * بازی را متوقف کند؛ بازی باید کاملاً ثابت بماند (مگر اینکه در فاز یک شبکه
 * پیاده‌سازی شده باشد) و موجودیت‌ها/انیمیشن‌ها حرکتی نداشته باشند.
 * <p>
 * این کلاس فقط منطق UI منوی توقف را دارد و به‌جای صفحه‌ی گیم‌پلی واقعی
 * (که هنوز ساخته نشده) با یک {@link Screen} دلخواه که باید هنگام Resume به آن
 * برگردیم و یک Runnable برای Restart کار می‌کند؛ وقتی GameScreen واقعی ساخته
 * شد، همان‌جا این کلاس با previousScreen/onRestart واقعی صدا زده می‌شود.
 * <p>
 * تمام «تقلب»های حین بازی (فاز یک) طبق درخواست کاربر، همه در یک بخش «Cheats»
 * اینجا جمع شده‌اند تا به‌جای شلوغ کردن صفحه‌ی اصلی بازی، فقط از داخل منوی
 * توقف و در صورت فعال بودن حالت دیباگ در دسترس باشند.
 */
public class PauseScreen extends BaseMenuScreen {

    private final Screen previousScreen;
    private final Runnable onRestart;
    private final GameSession session;
    // === رفع باگ: خروج از مسابقه‌ی آنلاین از داخل منوی توقف ===
    // قبلاً «Save and Exit» همیشه مستقیم game.goToMainMenu() را صدا می‌زد؛
    // برای مسابقه‌ی «من، زامبی» آنلاین این یعنی کلاینت بدون اطلاع سرور
    // ناپدید می‌شد (مسابقه‌ی نیمه‌کاره روی سرور می‌ماند). onLeave اختیاری
    // است: اگر صفحه‌ی فراخواننده منطق خروج تمیز (مثل ترک مسابقه) دارد،
    // آن را همینجا پاس می‌دهد؛ در غیر این صورت (null) رفتار قبلی حفظ می‌شود.
    // این پارامتر کاملاً مستقل از session (که برای بخش Cheats تیم است) است؛
    // هر صفحه‌ای هرکدام از این دو یا هر دو یا هیچ‌کدام را می‌تواند پاس بدهد.
    private final Runnable onLeave;

    public PauseScreen(PvZGame game, Screen previousScreen, Runnable onRestart) {
        this(game, previousScreen, onRestart, (GameSession) null, null);
    }

    /** برای صفحات گیم‌پلی معمولی که بخش Cheats (وابسته به GameSession) لازم دارند. */
    public PauseScreen(PvZGame game, Screen previousScreen, Runnable onRestart, GameSession session) {
        this(game, previousScreen, onRestart, session, null);
    }

    /** برای صفحاتی مثل مسابقه‌ی آنلاین «من، زامبی» که به‌جای Cheats نیاز به یک اکشن خروج تمیز (ترک مسابقه) دارند. */
    public PauseScreen(PvZGame game, Screen previousScreen, Runnable onRestart, Runnable onLeave) {
        this(game, previousScreen, onRestart, (GameSession) null, onLeave);
    }

    public PauseScreen(PvZGame game, Screen previousScreen, Runnable onRestart, GameSession session, Runnable onLeave) {
        super(game);
        this.previousScreen = previousScreen;
        this.onRestart = onRestart;
        this.session = session;
        this.onLeave = onLeave;

        rootTable.add(title("Game Paused")).padBottom(20f).row();

        Table form = new Table();

        Slider musicSlider = new Slider(0f, 1f, 0.05f, false, skin);
        musicSlider.setValue(SoundManager.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundManager.setMusicVolume(musicSlider.getValue());
            }
        });
        form.add(new Label("Music:", skin)).right().padRight(10f).padBottom(10f);
        form.add(musicSlider).width(220f).padBottom(10f).row();

        Slider sfxSlider = new Slider(0f, 1f, 0.05f, false, skin);
        sfxSlider.setValue(SoundManager.getSoundVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundManager.setSoundVolume(sfxSlider.getValue());
            }
        });
        form.add(new Label("Sound FX:", skin)).right().padRight(10f).padBottom(10f);
        form.add(sfxSlider).width(220f).padBottom(10f).row();

        CheckBox hitboxBox = new CheckBox(" Show grid / hitboxes", skin);
        form.add().padRight(10f);
        form.add(hitboxBox).left().padBottom(10f).row();

        rootTable.add(form).padBottom(20f).row();

        User user = game.getLoggedInUser();
        if (session != null && user != null && user.isDebugMode()) {
            rootTable.add(buildCheatSection(user)).padBottom(20f).row();
        }

        Table buttons = new Table();
        addButton(buttons, "Resume", this::doResume);
        addButton(buttons, "Restart", this::doRestart);
        addButton(buttons, "Save and Exit", this::doSaveAndExit);
        rootTable.add(buttons).row();
    }

    /**
     * همه‌ی «تقلب»های حین بازی که در فاز یک داشتیم (افزودن خورشید، غذای گیاه،
     * حذف زمان انتظار کاشت، افزودن سکه/الماس، اسپاون زامبی، کشتن همه‌ی
     * زامبی‌ها) یک‌جا اینجا جمع شده‌اند.
     */
    private Table buildCheatSection(User user) {
        Table section = new Table();
        section.add(new Label("Cheat Codes", skin, "title")).colspan(4).padBottom(10f).row();

        section.add(cheatButton("+ Sun", () -> session.getSunManager().addSun(50))).pad(4f);
        section.add(cheatButton("+ Plant Food", session::addPlantFood)).pad(4f);
        section.add(cheatButton("Remove Cooldown", session::clearAllCooldowns)).pad(4f);
        section.add(cheatButton("Nuke (kill all zombies)", () -> session.getAliveZombies().clear())).pad(4f);
        section.row();
        section.add(cheatButton("+ Coin", () -> user.setCoins(user.getCoins() + 500))).pad(4f);
        section.add(cheatButton("+ Diamond", () -> user.setDiamonds(user.getDiamonds() + 5))).pad(4f);
        section.add(cheatButton("Spawn Zombie", () -> {
            int dl = session.getUser().getDifficultyLevel();
            Zombie z = model.zombie.ZombieFactory.create("normal", dl);
            int row = new java.util.Random().nextInt(Board.ROWS);
            z.spawn(row, Board.COLS - 1);
            z.setSpawnTick((int) session.getTickCount());
            session.getAliveZombies().add(z);
        })).pad(4f);
        section.row();

        return section;
    }

    private TextButton cheatButton(String text, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return button;
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
        if (onLeave != null) {
            onLeave.run();
        } else {
            game.goToMainMenu();
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_SETTINGS;
    }
}
