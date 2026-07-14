package controller;

import model.game.GameSession;
import model.menu.MenuType;
import model.user.User;
import model.user.UserManager;
import util.CommandLine;
import view.ConsoleView;

import java.util.Scanner;

/**
 * حلقه‌ی اصلی خواندن دستورات از ورودی استاندارد و مسیریابی آن‌ها بین
 * MenuController (منوها/احراز هویت) و GameController (منطق داخل یک مرحله).
 */
public class AppController {

    private final UserManager userManager = new UserManager();
    private final ConsoleView view = new ConsoleView();
    private final MenuController menuController = new MenuController(userManager, view);
    private final GameController gameController = new GameController(view);

    private GameSession activeSession;
    private boolean inGame = false;

    public void run() {
        view.printMessage("به بازی Plants vs Zombies 2 (نسخه‌ی درسی) خوش آمدید.");
        view.printMessage("برای شروع، ثبت‌نام کنید: register -u <username> -p <pass> <pass_confirm> -n <nickname> -e <email> -g <gender>");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) {
                continue;
            }
            if (line.trim().equalsIgnoreCase("exit")) {
                userManager.save();
                break;
            }
            CommandLine cmd = new CommandLine(line);
            dispatch(line, cmd);
        }
        scanner.close();
    }

    private void dispatch(String rawLine, CommandLine cmd) {
        // اگر داخل یک مرحله در حال بازی هستیم، اول به GameController فرصت بده
        if (inGame && activeSession != null) {
            if (gameController.handle(activeSession, rawLine, cmd)) {
                if (activeSession.isGameOver()) {
                    finishGame();
                }
                return;
            }
            if (!cmd.getTokens().isEmpty() && cmd.getTokens().get(0).equals("menu") && cmd.getTokens().size() >= 2
                    && cmd.getTokens().get(1).equals("exit")) {
                finishGame();
                return;
            }
        }

        // دستور شروع بازی از منوی انتخاب گیاه
        if (!cmd.getTokens().isEmpty() && cmd.getTokens().get(0).equals("start")
                && cmd.getTokens().size() >= 2 && cmd.getTokens().get(1).equals("game")) {
            startGame();
            return;
        }

        if (!cmd.getTokens().isEmpty() && cmd.getTokens().get(0).equals("menu")
                && cmd.getTokens().size() >= 4 && cmd.getTokens().get(1).equals("enter")
                && cmd.getTokens().get(2).equals("chapter")) {
            // menu enter chapter -c <chaptername>: مستقیم به مرحله انتخاب گیاه می‌رویم
            view.printMessage("وارد فصل " + cmd.get("c") + " شدید. گیاهان خود را با 'add plant -t <type>' انتخاب کنید و سپس 'start game' بزنید.");
            return;
        }

        boolean handled = menuController.handle(rawLine, cmd);
        if (!handled) {
            view.printError("دستور نامعتبر یا در این منو غیرقابل استفاده است.");
        }
    }

    private void startGame() {
        User user = menuController.getLoggedInUser();
        if (user == null) {
            view.printError("ابتدا باید وارد حساب کاربری شوید.");
            return;
        }
        activeSession = new GameSession(user, 5); // به طور پیش‌فرض ۵ موج برای یک مرحله معمولی
        inGame = true;
        view.printMessage("بازی شروع شد! از دستورات 'advance time -t <n> ticks' و 'plant plant -t <type> -l (<x>, <y>)' استفاده کنید.");
    }

    private void finishGame() {
        User user = menuController.getLoggedInUser();
        if (user != null) {
            user.incrementGamesPlayed();
            if (activeSession != null && activeSession.isWon()) {
                user.incrementLevelsCompleted();
            }
            userManager.save();
        }
        inGame = false;
        activeSession = null;
        view.printMessage("به منوی بازی بازگشتید.");
    }
}
