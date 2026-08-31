package controller;

import model.menu.MenuType;
import model.user.User;
import model.user.UserManager;
import util.CommandLine;
import view.*;

import java.util.List;

public class MenuController {
    private final AppController appController;

    private final UserManager userManager;
    private final ConsoleView consoleView;

    private MenuType currentMenu = MenuType.REGISTER;
    private User loggedInUser = null;

    private int currentChapter = 1;
    private int currentLevel = 1;

    private final RegisterView registerView;
    private final LoginView loginView;
    private final MainView mainView;
    private final PlayView playView;
    private final SettingsView settingsView;
    private final NewsView newsView;
    private final ProfileView profileView;
    private final ShopView shopView;
    private final QuestView questView;
    private final CollectionController collectionController;
    private final GreenhouseView greenhouseView;
    private final PlantSelectionView plantSelectionView;
    private final LeaderboardView leaderboardView;
    private final MiniGamesView miniGamesView;
    public MenuController(UserManager userManager, ConsoleView consoleView, AppController appController) {
        this.appController = appController;
        this.userManager = userManager;
        this.consoleView = consoleView;
        this.leaderboardView = new LeaderboardView(new LeaderboardController(userManager), consoleView);
        this.registerView = new RegisterView(new RegisterController(userManager), consoleView, this);
        this.loginView = new LoginView(new LoginController(), consoleView, this);
        this.mainView = new MainView(new MainController(userManager), consoleView, this);
        this.playView = new PlayView(new PlayController(userManager), consoleView, this);
        this.settingsView = new SettingsView(new SettingsController(userManager), consoleView, this);
        this.newsView = new NewsView(new NewsController(), consoleView);
        this.profileView = new ProfileView(new ProfileController(userManager), consoleView);
        this.collectionController = new CollectionController(userManager);
        this.shopView = new ShopView(new ShopController(userManager), consoleView);
        this.questView = new QuestView(new QuestController(userManager), consoleView);
        this.greenhouseView = new GreenhouseView(new GreenhouseController(userManager), consoleView);
        this.plantSelectionView = new PlantSelectionView(new PlantSelectionController(), consoleView, this);
        this.miniGamesView = new MiniGamesView(appController, consoleView, this);
    }

    public boolean handle(String rawLine, CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) return false;

        // ۱. دستور نمایش منوی فعلی
        if (t.size() >= 3 && t.get(0).equals("menu") && t.get(1).equals("show") && t.get(2).equals("current")) {
            consoleView.printMessage(currentMenu.toString().toLowerCase() + " menu");
            return true;
        }

        // ۲. دستور خروج از منو (بازگشت به عقب)
        if (t.size() >= 2 && t.get(0).equals("menu") && t.get(1).equals("exit")) {
            return handleMenuExit();
        }

        // ۳. دستور ورود به منوی جدید
        if (t.size() >= 3 && t.get(0).equals("menu") && t.get(1).equals("enter")
                && !t.get(2).equals("chapter") && !t.get(2).equals("collection")) {
            String targetMenuName = t.get(2).toUpperCase().replace("-", "_");
            return handleMenuEnter(targetMenuName);
        }

        // ۴. ارسال سایر دستورات به منوی مربوطه
        switch (currentMenu) {
            case LEADERBOARD:
                return leaderboardView.checkCommand(cmd);
            case REGISTER:
                return registerView.checkCommand(t, cmd);
            case LOGIN:
                return loginView.checkCommand(t, cmd);
            case MAIN:
                return mainView.checkCommand(t, cmd);
            case GAME:
                return playView.checkCommand(t, cmd);
            case SETTINGS:
                return settingsView.checkCommand(t, cmd);
            case NEWS:
                return newsView.checkCommand(loggedInUser, t, cmd);
            case PROFILE:
                return profileView.checkCommand(loggedInUser, t, cmd);
            case SHOP:
                return shopView.checkCommand(loggedInUser, cmd);
            case TRAVEL_LOG:
                return questView.checkCommand(loggedInUser, cmd);
            case COLLECTION:
                return collectionController.handle(loggedInUser, t, cmd);
            case GREENHOUSE:
                return greenhouseView.checkCommand(loggedInUser, t, cmd);
            case PLANT_SELECTION:
                return plantSelectionView.checkCommand(loggedInUser, t, cmd);
            case MINI_GAMES:
                return miniGamesView.checkCommand(t, cmd);
            case IN_GAME:
                return true;

            default:
                return false;
        }
    }

    private boolean handleMenuEnter(String targetMenu) {
        MenuType target;
        try {
            target = MenuType.valueOf(targetMenu);
        } catch (IllegalArgumentException e) {
            consoleView.printError("منوی درخواستی وجود ندارد.");
            return true;
        }

        boolean canEnter = false;

        switch (currentMenu) {
            case REGISTER:
                if (target == MenuType.LOGIN) canEnter = true;
                break;
            case LOGIN:
                // انتقال از لاگین به اصلی به صورت خودکار در کلاس LoginView انجام می‌شود
                break;
            case MAIN:
                if (target == MenuType.GAME || target == MenuType.SETTINGS ||
                        target == MenuType.NEWS || target == MenuType.PROFILE ||
                        target == MenuType.GREENHOUSE || target == MenuType.SHOP || target == MenuType.TRAVEL_LOG ||
                        target == MenuType.PLANT_SELECTION || target == MenuType.MINI_GAMES) {
                    canEnter = true;
                }
                break;
            case GREENHOUSE:
                if (target == MenuType.SHOP) canEnter = true;
                break;
            case GAME:
                if (target == MenuType.COLLECTION) canEnter = true;
                break;
        }

        if (canEnter) {
            setCurrentMenu(target);
            consoleView.printMessage("وارد منوی " + target.toString().toLowerCase() + " شدید.");
        } else {
            consoleView.printError("شما اجازه ورود به این منو را از منوی فعلی ندارید.");
        }
        return true;
    }

    private boolean handleMenuExit() {
        // پیاده‌سازی دقیق بخش "خروج از منو" طبق داکیومنت اولیه
        switch (currentMenu) {
            case REGISTER:
                consoleView.printMessage("پایان برنامه.");
                appController.stopApp();
                break;
            case LOGIN:
                setCurrentMenu(MenuType.REGISTER);
                consoleView.printMessage("به منوی ثبت‌نام بازگشتید.");
                break;
            case MAIN:
                consoleView.printError("برای خروج از منوی اصلی باید از طریق دستور logout که در ادامه توضیح داده می‌شود، اقدام شود.");
                break;
            case GAME:
            case SETTINGS:
            case NEWS:
            case PROFILE:
            case GREENHOUSE:
            case MINI_GAMES:
                setCurrentMenu(MenuType.MAIN);
                consoleView.printMessage("به منوی اصلی بازگشتید.");
                break;
            case SHOP:
                setCurrentMenu(MenuType.GREENHOUSE);
                consoleView.printMessage("به منوی گلخونه بازگشتید");
                break;
            case COLLECTION:
                setCurrentMenu(MenuType.GAME);
                consoleView.printMessage("به منوی بازی بازگشتید.");
                break;
            case PLANT_SELECTION:
                setCurrentMenu(MenuType.GAME);
                consoleView.printMessage("به منوی بازی بازگشتید.");
                break;
        }
        return true;
    }

    // گترها و سترها
    public MenuType getCurrentMenu() {
        return currentMenu;
    }

    public void setCurrentMenu(MenuType currentMenu) {
        this.currentMenu = currentMenu;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void startGame(List<String> selectedPlants) {
        appController.startGame(selectedPlants, currentChapter, currentLevel);
    }

    public void setCurrentChapterAndLevel(int chapter, int level) {
        this.currentChapter = chapter;
        this.currentLevel = level;
    }
}