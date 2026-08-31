package view;

import controller.AppController;
import controller.MenuController;
import model.menu.MenuType;
import util.CommandLine;

import java.util.List;

/**
 * منوی مینی‌گیم‌ها: از منوی اصلی قابل دسترسی است (menu enter minigames) و
 * لیست مینی‌گیم‌های موجود را همراه با سه سطح هرکدام نشان می‌دهد.
 *
 * دستورات:
 *   show minigames                          -> لیست مینی‌گیم‌های موجود
 *   start minigame -n <name> -l <level>      -> شروع مینی‌گیم با نام و سطح مشخص
 */
public class MiniGamesView {

    private static final List<String> AVAILABLE = List.of("vasebreaker", "wallnutbowling", "izombie", "beghouled");

    private final AppController appController;
    private final ConsoleView consoleView;
    private final MenuController menuController;

    public MiniGamesView(AppController appController, ConsoleView consoleView, MenuController menuController) {
        this.appController = appController;
        this.consoleView = consoleView;
        this.menuController = menuController;
    }

    public boolean checkCommand(List<String> t, CommandLine cmd) {
        if (t.size() >= 2 && t.get(0).equals("show") && t.get(1).equals("minigames")) {
            printMiniGamesList();
            return true;
        }

        if (t.size() >= 2 && t.get(0).equals("start") && t.get(1).equals("minigame")) {
            String name = cmd.get("n");
            String levelStr = cmd.get("l");
            if (name == null) {
                consoleView.printError("نام مینی‌گیم مشخص نشده. مثال: start minigame -n vasebreaker -l 1");
                return true;
            }
            int level = 1;
            if (levelStr != null) {
                try {
                    level = Integer.parseInt(levelStr);
                } catch (NumberFormatException e) {
                    consoleView.printError("سطح باید عددی بین ۱ تا ۳ باشد.");
                    return true;
                }
            }
            if (!AVAILABLE.contains(name.toLowerCase())) {
                consoleView.printError("مینی‌گیم ناشناخته. گزینه‌های معتبر: " + String.join(", ", AVAILABLE));
                return true;
            }
            if (level < 1 || level > 3) {
                consoleView.printError("سطح باید بین ۱ تا ۳ باشد.");
                return true;
            }
            appController.startMiniGame(name.toLowerCase(), level);
            return true;
        }

        return false;
    }

    private void printMiniGamesList() {
        consoleView.printMessage("--- Mini Games ---");
        consoleView.printMessage("1. vasebreaker      (کوزه شکنی)      - levels: 1, 2, 3");
        consoleView.printMessage("2. wallnutbowling    (بولینگ گردویی) - levels: 1, 2, 3");
        consoleView.printMessage("3. izombie           (من زامبی)       - levels: 1, 2, 3");
        consoleView.printMessage("4. beghouled         (ترکیب سه‌تایی) - levels: 1, 2, 3");
        consoleView.printMessage("برای شروع: start minigame -n <name> -l <level>");
        consoleView.printMessage("------------------");
    }
}
