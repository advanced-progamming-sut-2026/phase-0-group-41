package controller;

import model.menu.MenuType;
import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;
import util.CommandLine;
import util.Validator;
import view.ConsoleView;

import java.util.List;
import java.util.Map;

/** پیاده‌سازی منوهای ثبت‌نام/ورود/اصلی/تنظیمات/پروفایل/کالکشن مطابق داک فاز صفر و یک. */
public class MenuController {

    private final UserManager userManager;
    private final ConsoleView view;

    private MenuType currentMenu = MenuType.REGISTER;
    private User loggedInUser;
    private User pendingForgetPasswordUser; // کاربری که در حال پاسخ به سوال امنیتی است

    public MenuController(UserManager userManager, ConsoleView view) {
        this.userManager = userManager;
        this.view = view;
    }

    public MenuType getCurrentMenu() {
        return currentMenu;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    /** @return true اگر دستور پردازش شد */
    public boolean handle(String rawLine, CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) {
            return false;
        }

        if (t.get(0).equals("menu")) {
            return handleMenuCommand(t, cmd);
        }

        switch (currentMenu) {
            case REGISTER:
                return handleRegisterMenu(t, cmd);
            case LOGIN:
                return handleLoginMenu(t, cmd);
            case SETTINGS:
                return handleSettingsMenu(t, cmd);
            case PROFILE:
                return handleProfileMenu(t, cmd);
            case COLLECTION:
                return handleCollectionMenu(t, cmd);
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
            return handleProfileSubcommand(t, cmd);
        }
        if (t.size() >= 3 && t.get(1).equals("collection")) {
            return handleCollectionSubcommand(t, cmd);
        }
        if (t.size() >= 2 && (t.get(1).equals("greenhouse") || t.get(1).equals("travel-log")
                || t.get(1).equals("leaderboard") || t.get(1).equals("coin-wallet") || t.get(1).equals("gem-wallet"))) {
            view.printMessage("[stub] این بخش (" + t.get(1) + ") در این اسکلت به صورت کامل پیاده نشده؛ نقطه‌ی شروع در پکیج‌های greenhouse/quest/shop آماده است.");
            return true;
        }
        if (t.size() >= 3 && t.get(1).equals("news")) {
            view.printMessage("[stub] اخباری برای نمایش وجود ندارد.");
            return true;
        }
        return false;
    }

    private boolean enterMenu(String target) {
        if (target == null) {
            view.printError("نام منو مشخص نشده است.");
            return true;
        }
        MenuType requested = mapMenuName(target);
        if (requested == null) {
            view.printError("منوی ناشناخته: " + target);
            return true;
        }
        // قوانین دسترسی مطابق داک
        if (currentMenu == MenuType.REGISTER && requested != MenuType.LOGIN) {
            view.printError("از منوی ثبت‌نام فقط می‌توانید به منوی ورود بروید.");
            return true;
        }
        if (currentMenu == MenuType.LOGIN && requested == MenuType.MAIN && !isLoggedIn()) {
            view.printError("برای رفتن به منوی اصلی ابتدا باید وارد حساب کاربری شوید.");
            return true;
        }
        currentMenu = requested;
        view.printMessage("وارد منوی " + requested + " شدید.");
        return true;
    }

    private MenuType mapMenuName(String name) {
        switch (name.toLowerCase()) {
            case "register":
                return MenuType.REGISTER;
            case "login":
                return MenuType.LOGIN;
            case "main":
                return MenuType.MAIN;
            case "game":
                return MenuType.GAME;
            case "settings":
                return MenuType.SETTINGS;
            case "news":
                return MenuType.NEWS;
            case "profile":
                return MenuType.PROFILE;
            case "collection":
                return MenuType.COLLECTION;
            default:
                return null;
        }
    }

    private void exitMenu() {
        switch (currentMenu) {
            case REGISTER:
                view.printMessage("پایان برنامه.");
                System.exit(0);
                break;
            case LOGIN:
                currentMenu = MenuType.REGISTER;
                break;
            case GAME:
            case SETTINGS:
            case NEWS:
            case PROFILE:
                currentMenu = MenuType.MAIN;
                break;
            case COLLECTION:
                currentMenu = MenuType.GAME;
                break;
            case MAIN:
                view.printError("برای خروج از منوی اصلی از دستور 'menu logout' استفاده کنید.");
                break;
            default:
                break;
        }
    }

    private void logout() {
        if (currentMenu == MenuType.MAIN) {
            loggedInUser = null;
            currentMenu = MenuType.REGISTER;
            view.printMessage("خارج شدید.");
        }
    }

    // ---------------- REGISTER ----------------

    private boolean handleRegisterMenu(List<String> t, CommandLine cmd) {
        if (t.get(0).equals("register")) {
            doRegister(cmd);
            return true;
        }
        if (t.get(0).equals("pick") && t.size() >= 2 && t.get(1).equals("question")) {
            pickQuestion(cmd);
            return true;
        }
        return false;
    }

    private User pendingRegisteredUser;

    private void doRegister(CommandLine cmd) {
        String username = cmd.get("u");
        List<String> passwordParts = cmd.getMulti("p");
        String password = passwordParts.isEmpty() ? null : passwordParts.get(0);
        String passwordConfirm = passwordParts.size() > 1 ? passwordParts.get(1) : null;
        String nickname = cmd.get("n");
        String email = cmd.get("e");
        String gender = cmd.get("g");

        if (!Validator.isValidUsername(username)) {
            view.printError("نام کاربری نامعتبر است (فقط حروف، اعداد و -).");
            return;
        }
        if (userManager.usernameExists(username)) {
            view.printError("این نام کاربری قبلا استفاده شده است.");
            return;
        }
        String weakness = Validator.passwordWeaknessReason(password);
        if (weakness != null) {
            view.printError(weakness);
            return;
        }
        if (!password.equals(passwordConfirm)) {
            view.printError("رمز عبور و تکرار آن یکسان نیستند.");
            return;
        }
        if (!Validator.isValidNickname(nickname)) {
            view.printError("نام مستعار باید بین ۳ تا ۳۰ کاراکتر باشد.");
            return;
        }
        if (!Validator.isValidEmail(email)) {
            view.printError("ایمیل نامعتبر است.");
            return;
        }
        if (gender == null || !(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) {
            view.printError("جنسیت باید male یا female باشد.");
            return;
        }
        pendingRegisteredUser = userManager.register(username, password, nickname, email, gender);
        StringBuilder qList = new StringBuilder("سوالات امنیتی:\n");
        for (Map.Entry<Integer, String> e : SecurityQuestions.all().entrySet()) {
            qList.append(e.getKey()).append(") ").append(e.getValue()).append('\n');
        }
        view.printMessage(qList.toString());
        view.printMessage("لطفا با دستور 'pick question -q <شماره> -a <پاسخ> -c <تکرار پاسخ>' یک سوال انتخاب کنید.");
    }

    private void pickQuestion(CommandLine cmd) {
        if (pendingRegisteredUser == null) {
            view.printError("ابتدا باید ثبت‌نام کنید.");
            return;
        }
        int qId;
        try {
            qId = Integer.parseInt(cmd.get("q"));
        } catch (Exception e) {
            view.printError("شماره سوال نامعتبر است.");
            return;
        }
        if (!SecurityQuestions.exists(qId)) {
            view.printError("سوال امنیتی با این شماره وجود ندارد.");
            return;
        }
        String answer = cmd.get("a");
        String confirm = cmd.get("c");
        if (answer == null || !answer.equals(confirm)) {
            view.printError("پاسخ و تکرار آن یکسان نیستند.");
            return;
        }
        pendingRegisteredUser.setSecurityQuestionId(qId);
        pendingRegisteredUser.setSecurityAnswer(answer);
        userManager.save();
        view.printMessage("ثبت‌نام با موفقیت انجام شد. اکنون به منوی ورود بروید: menu enter login");
        pendingRegisteredUser = null;
    }

    // ---------------- LOGIN ----------------

    private boolean handleLoginMenu(List<String> t, CommandLine cmd) {
        if (t.get(0).equals("login")) {
            doLogin(cmd);
            return true;
        }
        if (t.get(0).equals("forget") && t.size() >= 2 && t.get(1).equals("password")) {
            doForgetPassword(cmd);
            return true;
        }
        if (t.get(0).equals("answer")) {
            doAnswerSecurityQuestion(cmd);
            return true;
        }
        return false;
    }

    private void doLogin(CommandLine cmd) {
        String username = cmd.get("u");
        String password = cmd.get("p");
        User user = userManager.findByUsername(username);
        if (user == null || !userManager.checkPassword(user, password)) {
            view.printError("نام کاربری یا رمز عبور اشتباه است.");
            return;
        }
        loggedInUser = user;
        currentMenu = MenuType.MAIN;
        view.printMessage("خوش آمدید " + user.getNickname() + "!");
    }

    private void doForgetPassword(CommandLine cmd) {
        String username = cmd.get("u");
        String email = cmd.get("e");
        User user = userManager.findByUsername(username);
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            view.printError("کاربر با این نام کاربری/ایمیل یافت نشد.");
            return;
        }
        pendingForgetPasswordUser = user;
        view.printMessage(SecurityQuestions.get(user.getSecurityQuestionId()));
        view.printMessage("پاسخ را با دستور 'answer -a <پاسخ>' وارد کنید.");
    }

    private void doAnswerSecurityQuestion(CommandLine cmd) {
        if (pendingForgetPasswordUser == null) {
            view.printError("ابتدا 'forget password' را اجرا کنید.");
            return;
        }
        String answer = cmd.get("a");
        if (answer != null && answer.equals(pendingForgetPasswordUser.getSecurityAnswer())) {
            view.printMessage("پاسخ صحیح بود. لطفا رمز عبور جدید را با 'menu profile change-password ...' تنظیم کنید"
                    + " (یا در این اسکلت با ورود مجدد رمز فعلی را نگه دارید).");
        } else {
            view.printError("پاسخ نادرست بود.");
        }
        pendingForgetPasswordUser = null;
    }

    // ---------------- SETTINGS ----------------

    private boolean handleSettingsMenu(List<String> t, CommandLine cmd) {
        return false; // فعلا دستورات تنظیمات فقط از طریق "menu settings change-difficulty" است
    }

    private void changeDifficulty(CommandLine cmd) {
        if (!isLoggedIn()) {
            view.printError("ابتدا وارد شوید.");
            return;
        }
        try {
            int level = Integer.parseInt(cmd.get("l"));
            if (level < 1 || level > 5) {
                view.printError("سطح سختی باید بین ۱ تا ۵ باشد.");
                return;
            }
            loggedInUser.setDifficultyLevel(level);
            userManager.save();
            view.printMessage("سطح سختی به " + level + " تغییر کرد.");
        } catch (Exception e) {
            view.printError("مقدار سطح سختی نامعتبر است.");
        }
    }

    // ---------------- PROFILE ----------------

    private boolean handleProfileMenu(List<String> t, CommandLine cmd) {
        return false;
    }

    private boolean handleProfileSubcommand(List<String> t, CommandLine cmd) {
        if (!isLoggedIn()) {
            view.printError("ابتدا وارد شوید.");
           return true;
        }
        if (t.size() < 3) {
            return false;
        }
        String sub = t.get(2);
        switch (sub) {
            case "change-username": {
                String newUsername = cmd.get("u");
                if (newUsername.equals(loggedInUser.getUsername())) {
                    view.printError("نام کاربری جدید با نام فعلی یکسان است.");
                    return true;
                }
                if (userManager.usernameExists(newUsername)) {
                    view.printError("این نام کاربری قبلا گرفته شده.");
                    return true;
                }
                view.printMessage("[محدودیت اسکلت] تغییر username به دلیل استفاده از آن به عنوان کلید نگاشت پیاده نشده؛ می‌توانید در UserManager افزودن متد rename پیاده‌سازی کنید.");
                return true;
            }
            case "change-nickname": {
                String newNickname = cmd.get("u");
                if (newNickname.equals(loggedInUser.getNickname())) {
                    view.printError("نام مستعار جدید با نام فعلی یکسان است.");
                    return true;
                }
                loggedInUser.setNickname(newNickname);
                userManager.save();
                view.printMessage("نام مستعار تغییر کرد.");
                return true;
            }
            case "change-email": {
                String newEmail = cmd.get("e");
                if (newEmail.equalsIgnoreCase(loggedInUser.getEmail())) {
                    view.printError("ایمیل جدید با ایمیل فعلی یکسان است.");
                    return true;
                }
                if (!Validator.isValidEmail(newEmail)) {
                    view.printError("ایمیل نامعتبر است.");
                    return true;
                }
                loggedInUser.setEmail(newEmail);
                userManager.save();
                view.printMessage("ایمیل تغییر کرد.");
                return true;
            }
            case "change-password": {
                String newPassword = cmd.get("p");
                String oldPassword = cmd.get("o");
                if (!userManager.checkPassword(loggedInUser, oldPassword)) {
                    view.printError("رمز عبور فعلی اشتباه است.");
                    return true;
                }
                if (newPassword.equals(oldPassword)) {
                    view.printError("رمز جدید نباید با رمز فعلی یکسان باشد.");
                    return true;
                }
                String weakness = Validator.passwordWeaknessReason(newPassword);
                if (weakness != null) {
                    view.printError(weakness);
                    return true;
                }
                userManager.changePassword(loggedInUser, newPassword);
                view.printMessage("رمز عبور تغییر کرد.");
                return true;
            }
            case "show-info": {
                view.printMessage("Username: " + loggedInUser.getUsername());
                view.printMessage("Nickname: " + loggedInUser.getNickname());
                view.printMessage("GamesPlayed: " + loggedInUser.getGamesPlayed());
                view.printMessage("Coins: " + loggedInUser.getCoins());
                view.printMessage("Diamonds: " + loggedInUser.getDiamonds());
                view.printMessage("LevelsCompleted: " + loggedInUser.getLevelsCompleted());
                view.printMessage("MaxMowPoints: " + loggedInUser.getMaxMowPoints());
                return true;
            }
            default:
                return false;
        }
    }

    // ---------------- COLLECTION ----------------

    private boolean handleCollectionMenu(List<String> t, CommandLine cmd) {
        return false;
    }

    private boolean handleCollectionSubcommand(List<String> t, CommandLine cmd) {
        if (!isLoggedIn()) {
            view.printError("ابتدا وارد شوید.");
            return true;
        }
        if (t.size() < 3) {
            return false;
        }
        String sub = t.get(2);
        switch (sub) {
            case "show-plants":
                view.printMessage("گیاهان کسب‌شده: " + loggedInUser.getUnlockedPlants());
                return true;
            case "show-all-plants":
                view.printMessage("همه گیاهان تعریف‌شده: "
                        + ir.sharif.pvz.model.plant.PlantFactory.allPlantNames());
                return true;
            case "show-zombies":
                view.printMessage("زامبی‌های مشاهده‌شده: " + loggedInUser.getSeenZombies());
                return true;
            case "purchase-plant": {
                String plantName = cmd.get("p");
                if (!loggedInUser.spendCoins(2000)) {
                    view.printError("سکه کافی برای خرید این گیاه ندارید.");
                    return true;
                }
                loggedInUser.getUnlockedPlants().add(plantName);
                userManager.save();
                view.printMessage("گیاه " + plantName + " خریداری شد.");
                return true;
            }
            default:
                view.printMessage("[stub] این زیربخش کالکشن (" + sub + ") در اسکلت به صورت خلاصه پیاده شده است.");
                return true;
        }
    }
}
