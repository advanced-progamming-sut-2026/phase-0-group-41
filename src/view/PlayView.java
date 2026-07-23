package view;

import controller.MenuController;
import controller.PlayController;
import model.menu.MenuType;
import model.user.User;
import util.CommandLine;

import java.util.List;

public class PlayView {
    private final PlayController controller;
    private final ConsoleView consoleView;
    private final MenuController menuController;

    public PlayView(PlayController controller, ConsoleView consoleView, MenuController menuController) {
        this.controller = controller;
        this.consoleView = consoleView;
        this.menuController = menuController;
    }

    public boolean checkCommand(List<String> t, CommandLine cmd) {
        if (!t.get(0).equals("menu")) {
            return false;
        }

        User currentUser = menuController.getLoggedInUser();

        // 1. ورود به Chapter: menu enter chapter -c <chaptername>
        if (t.size() >= 3 && t.get(1).equals("enter") && t.get(2).equals("chapter")) {
            doEnterChapter(cmd, currentUser);
            return true;
        }

        // 2. ورود به کلکسیون: menu enter collection
        if (t.size() >= 3 && t.get(1).equals("enter") && t.get(2).equals("collection")) {
            menuController.setCurrentMenu(MenuType.COLLECTION);
            consoleView.printMessage("وارد منوی کلکسیون شدید.");
            return true;
        }

        // 3. ورود به گلخانه: menu greenhouse
        if (t.size() >= 2 && t.get(1).equals("greenhouse")) {
            menuController.setCurrentMenu(MenuType.GREENHOUSE);
            consoleView.printMessage("وارد منوی گلخانه شدید.");
            return true;
        }

        // 4. ورود به کوئست‌ها: menu travel-log
        if (t.size() >= 2 && t.get(1).equals("travel-log")) {
            menuController.setCurrentMenu(MenuType.TRAVEL_LOG);
            consoleView.printMessage("وارد منوی کوئست‌ها (Travel Log) شدید.");
            return true;
        }

        // 5. ورود به لیدربورد: menu leaderboard
        if (t.size() >= 2 && t.get(1).equals("leaderboard")) {
            menuController.setCurrentMenu(MenuType.LEADERBOARD);
            consoleView.printMessage("وارد منوی جدول امتیازات (Leaderboard) شدید.");
            return true;
        }

        // 6. کیف پول سکه: menu coin-wallet
        if (t.size() >= 2 && t.get(1).equals("coin-wallet")) {
            int coins = controller.getCoinBalance(currentUser);
            consoleView.printMessage("موجودی سکه شما: " + coins);
            return true;
        }

        // 7. کیف پول الماس: menu gem-wallet
        if (t.size() >= 2 && t.get(1).equals("gem-wallet")) {
            int gems = controller.getDiamondBalance(currentUser);
            consoleView.printMessage("موجودی الماس شما: " + gems);
            return true;
        }

        // 8. کدهای تقلب: menu cheat add <n> <coin/diamond>
        if (t.size() >= 5 && t.get(1).equals("cheat") && t.get(2).equals("add")) {
            doCheat(t, currentUser);
            return true;
        }

        return false;
    }

    private void doEnterChapter(CommandLine cmd, User user) {
        String chapterName = cmd.get("c");
        String result = controller.enterChapter(user, chapterName);

        switch (result) {
            case "SUCCESS":
                consoleView.printMessage("در حال ورود به قسمت " + chapterName + "...");
                // در فازهای بعدی کلاس GameController در اینجا فراخوانی می‌شود
                break;
            case "ERR_INVALID_CHAPTER":
                consoleView.printError("نام قسمت (Chapter) نامعتبر است.");
                break;
            case "ERR_LOCKED_CHAPTER":
                consoleView.printError("این قسمت هنوز برای شما باز (Unlock) نشده است. ابتدا مراحل قبلی را تمام کنید.");
                break;
        }
    }

    private void doCheat(List<String> t, User user) {
        try {
            int amount = Integer.parseInt(t.get(3));
            String type = t.get(4); // coin یا diamond

            String result = controller.applyCheat(user, amount, type);

            switch (result) {
                case "SUCCESS_COIN":
                    consoleView.printMessage(amount + " سکه با موفقیت به حساب شما اضافه شد!");
                    break;
                case "SUCCESS_DIAMOND":
                    consoleView.printMessage(amount + " الماس با موفقیت به حساب شما اضافه شد!");
                    break;
                case "ERR_INVALID_AMOUNT":
                    consoleView.printError("مقدار وارد شده باید بیشتر از صفر باشد.");
                    break;
                case "ERR_INVALID_CHEAT_TYPE":
                    consoleView.printError("نوع تقلب نامعتبر است. فقط coin یا diamond مجاز است.");
                    break;
            }
        } catch (NumberFormatException e) {
            consoleView.printError("تعداد سکه یا الماس باید یک عدد صحیح باشد.");
        }
    }
}