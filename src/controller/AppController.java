package controller;

import model.game.GameSession;
import model.user.User;
import model.user.UserManager;
import util.CommandLine;
import view.ConsoleView;
import java.util.Scanner;

public class AppController {

    private final UserManager userManager = new UserManager();
    private final ConsoleView view = new ConsoleView();
    private final MenuController menuController = new MenuController(userManager, view);
    private final GameController gameController = new GameController(view);

    private GameSession activeSession;
    private boolean inGame = false;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        
        // حلقه بی‌نهایت برای خواندن دستورات کاربر از کنسول
        while (scanner.hasNextLine()) {
            String rawLine = scanner.nextLine().trim();
            
            // اگه کاربر فقط اینتر زد (خط خالی)، نادیده بگیر و برو خط بعدی
            if (rawLine.isEmpty()) {
                continue;
            }

            // ساخت شیء CommandLine از رشته‌ی ورودی
            CommandLine cmd = new CommandLine(rawLine);
            
            // ارسال دستور به بخش پردازش (متدی که خودت از قبل نوشتی)
            dispatch(rawLine, cmd);
        }
        
        scanner.close();
    }
    
    public void exitApp() {
        userManager.save();
    }

    public void dispatch(String rawLine, CommandLine cmd) {
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

        if (!cmd.getTokens().isEmpty() && cmd.getTokens().get(0).equals("start")
                && cmd.getTokens().size() >= 2 && cmd.getTokens().get(1).equals("game")) {
            startGame();
            return;
        }

        if (!cmd.getTokens().isEmpty() && cmd.getTokens().get(0).equals("menu")
                && cmd.getTokens().size() >= 4 && cmd.getTokens().get(1).equals("enter")
                && cmd.getTokens().get(2).equals("chapter")) {
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
        activeSession = new GameSession(user, 5);
        inGame = true;
        view.printMessage("بازی شروع شد! از دستورات 'advance time -t <n> ticks' و 'plant plant -t <type> -l (<x>, <y>)' استفاده کنید.");
    }

    private void finishGame() {
        User user = menuController.getLoggedInUser();
        if (user != null) {
            user.incrementGamesPlayed();
            if (activeSession != null) {
                if(activeSession.isWon()) {
                user.incrementLevelsCompleted();
                user.getQuestContext().setStagesCompleted(user.getLevelsCompleted());
                }

                model.scoreGame.MeowPoint calculator = new model.scoreGame.MeowPoint();
                int totalMowPoints = calculator.calculateMyuPoints(activeSession.getMeowEvents());
                view.printMessage("امتیاز MeowPoints شما در این مرحله: " + totalMowPoints);

                user.updateMaxMowPoints(totalMowPoints);
            }
            userManager.save();
        }
        inGame = false;
        activeSession = null;
        view.printMessage("به منوی بازی بازگشتید.");
    }
}