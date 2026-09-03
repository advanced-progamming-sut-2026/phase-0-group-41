package gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import controller.CollectionController;
import controller.GreenhouseController;
import controller.LeaderboardController;
import controller.LoginController;
import controller.MainController;
import controller.PlantSelectionController;
import controller.ProfileController;
import controller.QuestController;
import controller.RegisterController;
import controller.SettingsController;
import controller.ShopController;
import model.user.User;
import model.user.UserManager;
import gdx.render.PamAssets;
import gdx.screens.ChapterLevelSelectScreen;
import gdx.screens.CollectionScreen;
import gdx.screens.DialogueScreen;
import gdx.screens.GameScreen;
import gdx.screens.GreenhouseScreen;
import gdx.screens.LeaderboardScreen;
import gdx.screens.LoginScreen;
import gdx.screens.MainMenuScreen;
import gdx.screens.MiniGameScreen;
import gdx.screens.MiniGamesScreen;
import gdx.screens.NewsScreen;
import gdx.screens.PlantSelectionScreen;
import gdx.screens.ProfileScreen;
import gdx.screens.QuestScreen;
import gdx.screens.RegisterScreen;
import gdx.screens.SettingsScreen;
import gdx.screens.ShopScreen;
import gdx.util.SkinFactory;
import model.game.GameSession;
import model.minigame.MiniGameSession;

/**
 * کلاس اصلی برنامه‌ی گرافیکی (جایگزین AppController.run() کنسولی).
 * این کلاس همان کنترلرهای فاز اول را نگه می‌دارد و فقط لایه‌ی نمایش (View)
 * را به Scene2D/libGDX تغییر می‌دهد؛ منطق بازی دست‌نخورده باقی می‌ماند.
 *
 * برای اجرای پروژه‌ی گرافیکی به‌جای برنامه‌ی کنسولی، به‌جای Main.java
 * از یک launcher جدید (LWJGL3 Application) استفاده کنید که create() این کلاس را صدا بزند.
 * نمونه‌ی launcher در انتهای همین فایل به‌صورت کامنت آمده است.
 */
public class PvZGame extends Game {

    private final UserManager userManager = new UserManager();

    private RegisterController registerController;
    private LoginController loginController;
    private MainController mainController;
    private SettingsController settingsController;
    private ProfileController profileController;
    private ShopController shopController;
    private QuestController questController;
    private LeaderboardController leaderboardController;
    private GreenhouseController greenhouseController;
    private CollectionController collectionController;
    private PlantSelectionController plantSelectionController;

    private Skin skin;
    private User loggedInUser;

    @Override
    public void create() {
        skin = SkinFactory.create();

        PamAssets.get();

        registerController = new RegisterController(userManager);
        loginController = new LoginController(userManager);
        mainController = new MainController(userManager);
        settingsController = new SettingsController(userManager);
        profileController = new ProfileController(userManager);
        shopController = new ShopController(userManager);
        questController = new QuestController(userManager);
        leaderboardController = new LeaderboardController(userManager);
        greenhouseController = new GreenhouseController(userManager);
        collectionController = new CollectionController(userManager);
        plantSelectionController = new PlantSelectionController();

        // طبق سند: اگر کاربر قبلاً «Stay logged in» را زده باشد، با باز کردن
        // دوباره‌ی برنامه باید خودکار وارد حساب کاربری‌اش شود.
        String rememberedUsername = userManager.getRememberedUsername();
        if (rememberedUsername != null) {
            loggedInUser = userManager.findByUsername(rememberedUsername);
        }
        if (loggedInUser != null) {
            setScreen(new MainMenuScreen(this));
        } else {
            setScreen(new RegisterScreen(this));
        }
    }

    // ==================== ناوبری بین صفحات ====================

    public void goToRegister() {
        setScreen(new RegisterScreen(this));
    }

    public void goToLogin() {
        setScreen(new LoginScreen(this));
    }

    public void goToMainMenu() {
        setScreen(new MainMenuScreen(this));
    }

    public void goToSettings() {
        setScreen(new SettingsScreen(this));
    }

    public void goToNews() {
        setScreen(new NewsScreen(this));
    }

    public void goToProfile() {
        setScreen(new ProfileScreen(this));
    }

    public void goToShop() {
        setScreen(new ShopScreen(this));
    }

    public void goToQuests() {
        setScreen(new QuestScreen(this));
    }

    public void goToLeaderboard() {
        setScreen(new LeaderboardScreen(this));
    }

    public void goToGreenhouse() {
        setScreen(new GreenhouseScreen(this));
    }

    public void goToCollection() {
        setScreen(new CollectionScreen(this));
    }

    /** ورود به منوی انتخاب فصل/مرحله (طبق سند فاز یک: دکمه‌ی Play باید ابتدا
     *  این منو را نشان دهد، نه مستقیماً صفحه‌ی انتخاب گیاه یک مرحله‌ی ثابت را). */
    public void goToChapterLevelSelect() {
        setScreen(new ChapterLevelSelectScreen(this));
    }

    public void goToPlantSelection() {
        setScreen(new PlantSelectionScreen(this));
    }

    /** ورود به صفحه‌ی انتخاب گیاه برای فصل/مرحله‌ی مشخص (بعد از انتخاب کاربر
     *  در ChapterLevelSelectScreen). */
    public void goToPlantSelection(int chapter, int level) {
        setScreen(new PlantSelectionScreen(this, chapter, level));
    }

    /** ورود به صفحه‌ی گرافیکی گیم‌پلی اصلی (grid کاشت، زامبی‌ها، خورشید، پرتابه‌ها و ...). */
    public void goToGameScreen(int chapter, int level) {
        // در ابتدای مرحله‌ی اول هر فصل، طبق سند، یک تبادل کوتاه دیالوگ بین
        // شخصیت‌های اصلی نمایش داده می‌شود (نیازی نیست حتماً همان شخصیت‌های
        // خود بازی باشند).
        if (level == 1) {
            DialogueScreen.Line[] lines = chapterIntroDialogue(chapter);
            if (lines.length > 0) {
                setScreen(new DialogueScreen(this, lines, () -> startGameScreen(chapter, level)));
                return;
            }
        }
        startGameScreen(chapter, level);
    }

    private DialogueScreen.Line[] chapterIntroDialogue(int chapter) {
        switch (chapter) {
            case 1:
                return new DialogueScreen.Line[]{
                        new DialogueScreen.Line("Crazy Dave", "We've cracked open an ancient vault... let's see what's inside!"),
                        new DialogueScreen.Line("Penny", "Scanning complete. Hostile organisms detected. Beginning lawn defense.")
                };
            case 2:
                return new DialogueScreen.Line[]{
                        new DialogueScreen.Line("Crazy Dave", "Brr! It's freezing in here. Bundle up, we've got zombies to fight!"),
                        new DialogueScreen.Line("Penny", "Temperature critical. Recommend ice-resistant flora.")
                };
            case 3:
                return new DialogueScreen.Line[]{
                        new DialogueScreen.Line("Crazy Dave", "The tide's coming in fast! Watch your step out there."),
                        new DialogueScreen.Line("Penny", "Aquatic zombies detected. Lily pads advised.")
                };
            case 4:
                return new DialogueScreen.Line[]{
                        new DialogueScreen.Line("Crazy Dave", "Dark times ahead... literally. No sun's getting through this gloom."),
                        new DialogueScreen.Line("Penny", "Solar collection offline. Rely on your sunflowers.")
                };
            default:
                return new DialogueScreen.Line[0]; // Beginner: بدون دیالوگ مقدماتی
        }
    }

    private void startGameScreen(int chapter, int level) {
        // فصل بازی (Season)، نوع مرحله (LevelMode) و سختی موج از منبع واحد
        // ChapterPlan خوانده می‌شود تا دقیقاً همان رفتار AppController کنسولی را
        // داشته باشیم (بدون این کار هر مرحله عین مرحله‌ی عادی پیش‌فرض اجرا می‌شد).
        model.game.Season season = model.game.ChapterPlan.seasonFor(chapter);
        model.levelrules.LevelMode mode = model.game.ChapterPlan.levelModeFor(chapter, level);
        int totalWaves = model.game.ChapterPlan.totalWavesFor(chapter, level);
        double baseWaveCost = model.game.ChapterPlan.baseWaveCostFor(chapter, level);

        GameSession session = new GameSession(loggedInUser, totalWaves, baseWaveCost, season, mode, level);
        setScreen(new GameScreen(this, session, chapter, level));
    }

    /** ورود به منوی مینی‌گیم‌ها (طبق سند فاز یک: قابل‌دسترس از منوی اصلی). */
    public void goToMiniGames() {
        setScreen(new MiniGamesScreen(this));
    }

    /**
     * شروع یک نشست مینی‌گیم مشخص با سطح داده‌شده.
     *
     * @param name  یکی از: vasebreaker, wallnutbowling, izombie, beghouled
     * @param level سطح سختی (۱ تا ۳؛ هرچه بیشتر سخت‌تر)
     */
    public void startMiniGame(String name, int level) {
        if (loggedInUser == null) {
            return;
        }
        if (!loggedInUser.isMiniGameLevelUnlocked(name.toLowerCase(), level)) {
            return; // سطح قفل است؛ نباید بدون بردن سطح قبلی شروع شود
        }
        MiniGameSession session;
        switch (name.toLowerCase()) {
            case "vasebreaker":
                session = new model.minigame.VasebreakerSession(loggedInUser, level);
                break;
            case "wallnutbowling":
                session = new model.minigame.WallnutBowlingSession(loggedInUser, level);
                break;
            case "izombie":
                session = new model.minigame.IZombieSession(loggedInUser, level);
                break;
            case "beghouled":
                session = new model.minigame.BeghouledSession(loggedInUser, level);
                break;
            default:
                return;
        }
        setScreen(new MiniGameScreen(this, session, name, level));
    }

    public void logout() {
        mainController.logout(loggedInUser);
        loggedInUser = null;
        userManager.forgetRememberedUser();
        goToRegister();
    }

    public void quitApp() {
        userManager.save();
        // مطابق سند فاز ۳: قبل از خروج نهایی از برنامه، آخرین وضعیت کاربر
        // لاگین‌شده باید روی سرور ذخیره شود.
        network.UserSync.push(loggedInUser);
        com.badlogic.gdx.Gdx.app.exit();
    }

    // ==================== دسترسی‌های مشترک ====================

    public UserManager getUserManager() {
        return userManager;
    }

    public Skin getSkin() {
        return skin;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public RegisterController getRegisterController() {
        return registerController;
    }

    public LoginController getLoginController() {
        return loginController;
    }

    public MainController getMainController() {
        return mainController;
    }

    public SettingsController getSettingsController() {
        return settingsController;
    }

    public ProfileController getProfileController() {
        return profileController;
    }

    public ShopController getShopController() {
        return shopController;
    }

    public QuestController getQuestController() {
        return questController;
    }

    public LeaderboardController getLeaderboardController() {
        return leaderboardController;
    }

    public GreenhouseController getGreenhouseController() {
        return greenhouseController;
    }

    public CollectionController getCollectionController() {
        return collectionController;
    }

    public PlantSelectionController getPlantSelectionController() {
        return plantSelectionController;
    }

    @Override
    public void dispose() {
        userManager.save();
        network.UserSync.push(loggedInUser);
        if (getScreen() != null) {
            getScreen().dispose();
        }
        skin.dispose();
        gdx.util.ImageUtils.disposeAll();
        gdx.util.SoundManager.disposeAll();
    }
}

