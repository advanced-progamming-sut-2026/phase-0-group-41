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

        setScreen(new RegisterScreen(this));
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

    public void goToPlantSelection() {
        setScreen(new PlantSelectionScreen(this));
    }

    /** ورود به صفحه‌ی گرافیکی گیم‌پلی اصلی (grid کاشت، زامبی‌ها، خورشید، پرتابه‌ها و ...). */
    public void goToGameScreen(int chapter, int level, int totalWaves) {
        GameSession session = new GameSession(loggedInUser, totalWaves);
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
        goToRegister();
    }

    public void quitApp() {
        userManager.save();
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
        if (getScreen() != null) {
            getScreen().dispose();
        }
        skin.dispose();
        gdx.util.ImageUtils.disposeAll();
        gdx.util.SoundManager.disposeAll();
    }
}

/*
 * نمونه‌ی Launcher برای اجرای دسکتاپ (LWJGL3) — این کلاس را در فایل جدا
 * (مثلاً src/main/java/gdx/DesktopLauncher.java) با محتوای زیر بسازید:
 *
 * package gdx;
 *
 * import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
 * import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
 *
 * public class DesktopLauncher {
 *     public static void main(String[] args) {
 *         Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
 *         config.setTitle("Plants vs Zombies 2 - Phase 2");
 *         config.setWindowedMode(1280, 720);
 *         new Lwjgl3Application(new PvZGame(), config);
 *     }
 * }
 */
