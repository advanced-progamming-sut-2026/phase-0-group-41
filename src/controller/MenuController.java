package controller;

import model.menu.MenuType;
import model.user.User;
import model.user.UserManager;
import util.CommandLine;
import view.*;

import java.util.List;

public class MenuController {
    private final UserManager userManager;
    private final ConsoleView view;

    private MenuType currentMenu = MenuType.REGISTER;
    private User loggedInUser;

    // ویوهای اختصاصی
    private final RegisterView registerView;
    private final LoginView loginView;
    private final ProfileView profileView;
    private final GreenhouseView greenhouseView;
    private final CollectionView collectionView;

    public MenuController(UserManager userManager, ConsoleView view) {
        this.userManager = userManager;
        this.view = view;

        // ساخت کنترلرها و ویوهای مجزا
        this.registerView = new RegisterView(new RegisterController(userManager), view, this);
        this.loginView = new LoginView(new LoginController(userManager), view, this);
        this.profileView = new ProfileView(new ProfileController(userManager), view);
        this.greenhouseView = new GreenhouseView(new GreenhouseController(userManager), view);
        this.collectionView = new CollectionView(new CollectionController(userManager));
    }

    public MenuType getCurrentMenu() { return currentMenu; }
    public void setCurrentMenu(MenuType menu) { this.currentMenu = menu; }
    public User getLoggedInUser() { return loggedInUser; }
    public void setLoggedInUser(User user) { this.loggedInUser = user; }
    public boolean isLoggedIn() { return loggedInUser != null; }

    public boolean handle(String rawLine, CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) return false;

        if (t.get(0).equals("show") && t.size() >= 2 && t.get(1).equals("greenhouse")) {
            greenhouseView.showGreenhouse(loggedInUser);
            return true;
        }

        if (t.get(0).equals("menu")) {
            return handleMenuCommand(t, cmd);
        }

        switch (currentMenu) {
            case REGISTER:
                return registerView.checkCommand(t, cmd);
            case LOGIN:
                return loginView.checkCommand(t, cmd);
            case PROFILE:
                return profileView.checkCommand(loggedInUser, t, cmd);
            case GREENHOUSE:
                return greenhouseView.checkCommand(loggedInUser, t, cmd);
            case COLLECTION:
                return collectionView.checkCommand(loggedInUser, cmd);
            default:
                return false;
        }
    }

    private boolean handleMenuCommand(List<String> t, CommandLine cmd) {
        if (t.size() >= 2 && t.get(1).equals("enter")) {
            String target = t.size() >= 3 ? t.get(2) : cmd.get("menu");
            return enterMenu(target);
        }
        if (t.size() >= 3 && t.get(1).equals("show") && t.get(2).equals("current")) {
            view.printMessage("Current menu: " + currentMenu);
            return true;
        }
        if (t.size() >= 2 && t.get(1).equals("exit")) {
            exitMenu();
            return true;
        }
        if (t.size() >= 2 && t.get(1).equals("logout")) {
            logout();
            return true;
        }
        if (t.size() >= 3 && t.get(1).equals("settings") && t.get(2).equals("change-difficulty")) {
            changeDifficulty(cmd);
            return true;
        }
        if (t.size() >= 3 && t.get(1).equals("profile")) {
            return profileView.checkCommand(loggedInUser, t, cmd);
        }
        if (t.size() >= 3 && t.get(1).equals("collection")) {
            return collectionView.checkCommand(loggedInUser, cmd);
        }
        if (t.size() >= 2 && t.get(1).equals("greenhouse")) {
            if (t.size() >= 3 && t.get(2).equals("enter")) {
                currentMenu = MenuType.GREENHOUSE;
                view.printMessage("وارد گلخانه شدید.");
            } else {
                greenhouseView.showGreenhouse(loggedInUser);
            }
            return true;
        }
        if (t.size() >= 2 && (t.get(1).equals("travel-log") || t.get(1).equals("leaderboard") || t.get(1).equals("coin-wallet") || t.get(1).equals("gem-wallet"))) {
            view.printMessage("[stub] این بخش در این اسکلت به صورت کامل پیاده نشده...");
            return true;
        }
        if (t.size() >= 3 && t.get(1).equals("news")) {
            view.printMessage("[stub] اخباری برای نمایش وجود ندارد.");
            return true;
        }
        return false;
    }

    private boolean enterMenu(String target) {
        if (target == null) { view.printError("نام منو مشخص نشده است."); return true; }
        MenuType requested = mapMenuName(target);
        if (requested == null) { view.printError("منوی ناشناخته: " + target); return true; }

        boolean isAllowed = false;
        switch (currentMenu) {
            case REGISTER:
                if (requested == MenuType.LOGIN) isAllowed = true;
                break;
            case LOGIN:
                if (requested == MenuType.MAIN && isLoggedIn()) isAllowed = true;
                else if (requested == MenuType.MAIN && !isLoggedIn()) {
                    view.printError("برای رفتن به منوی اصلی ابتدا باید وارد حساب کاربری شوید.");
                    return true;
                }
                break;
            case MAIN:
                if (requested == MenuType.GAME || requested == MenuType.SETTINGS || requested == MenuType.NEWS || requested == MenuType.PROFILE) {
                    isAllowed = true;
                }
                break;
            case GAME:
                if (requested == MenuType.COLLECTION) isAllowed = true;
                break;
            default:
                isAllowed = false;
                break;
        }

        if (!isAllowed) {
            view.printError("امکان ورود به منوی " + requested + " از منوی فعلی (" + currentMenu + ") وجود ندارد.");
            return true;
        }
        currentMenu = requested;
        view.printMessage("وارد منوی " + requested + " شدید.");
        return true;
    }

    private MenuType mapMenuName(String name) {
        switch (name.toLowerCase()) {
            case "register": return MenuType.REGISTER;
            case "login": return MenuType.LOGIN;
            case "main": return MenuType.MAIN;
            case "game": return MenuType.GAME;
            case "settings": return MenuType.SETTINGS;
            case "news": return MenuType.NEWS;
            case "profile": return MenuType.PROFILE;
            case "collection": return MenuType.COLLECTION;
            default: return null;
        }
    }

    private void exitMenu() {
        switch (currentMenu) {
            case REGISTER: view.printMessage("پایان برنامه."); System.exit(0); break;
            case LOGIN: currentMenu = MenuType.REGISTER; break;
            case GAME: case SETTINGS: case NEWS: case PROFILE: currentMenu = MenuType.MAIN; break;
            case COLLECTION: currentMenu = MenuType.GAME; break;
            case MAIN: view.printError("برای خروج از منوی اصلی از دستور 'menu logout' استفاده کنید."); break;
            default: break;
        }
    }

    private void logout() {
        if (currentMenu == MenuType.MAIN) {
            loggedInUser = null;
            currentMenu = MenuType.REGISTER;
            view.printMessage("خارج شدید.");
        }
    }

    private void changeDifficulty(CommandLine cmd) {
        if (!isLoggedIn()) { view.printError("ابتدا وارد شوید."); return; }
        try {
            int level = Integer.parseInt(cmd.get("l"));
            if (level < 1 || level > 5) { view.printError("سطح سختی باید بین ۱ تا ۵ باشد."); return; }
            loggedInUser.setDifficultyLevel(level);
            userManager.save();
            view.printMessage("سطح سختی به " + level + " تغییر کرد.");
        } catch (Exception e) {
            view.printError("مقدار سطح سختی نامعتبر است.");
        }
    }
}