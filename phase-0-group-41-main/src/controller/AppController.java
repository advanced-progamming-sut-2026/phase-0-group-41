package controller;

import model.game.GameSession;
import model.user.User;
import model.user.UserManager;
import util.CommandLine;
import view.ConsoleView;
import java.util.Scanner;

import java.util.List;

public class AppController {

    private final UserManager userManager = new UserManager();
    private final ConsoleView view = new ConsoleView();
    private final MenuController menuController = new MenuController(userManager, view, this);
    private final GameController gameController = new GameController(view);

    private GameSession activeSession;
    private boolean inGame = false;
    private boolean isRunning = true; // فلگ کنترل حلقه

    private int activeChapter = 1;
    private int activeLevel = 1;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        
        while (isRunning && scanner.hasNextLine()) {
            String rawLine = scanner.nextLine().trim();
            
            if (rawLine.isEmpty()) {
                continue;
            }

            CommandLine cmd = new CommandLine(rawLine);
            dispatch(rawLine, cmd);
        }
        
        scanner.close();
        exitApp();
    }

    public void stopApp() {
        isRunning = false;
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

        // if (!cmd.getTokens().isEmpty() && cmd.getTokens().get(0).equals("start")
        //         && cmd.getTokens().size() >= 2 && cmd.getTokens().get(1).equals("game")) {
        //     startGame();
        //     return;
        // }

        // if (!cmd.getTokens().isEmpty() && cmd.getTokens().get(0).equals("menu")
        //         && cmd.getTokens().size() >= 4 && cmd.getTokens().get(1).equals("enter")
        //         && cmd.getTokens().get(2).equals("chapter")) {
        //     view.printMessage("وارد فصل " + cmd.get("c") + " شدید. گیاهان خود را با 'add plant -t <type>' انتخاب کنید و سپس 'start game' بزنید.");
        //     return;
        // }

        boolean handled = menuController.handle(rawLine, cmd);
        if (!handled) {
            view.printError("دستور نامعتبر یا در این منو غیرقابل استفاده است.");
        }
    }

    public void startGame(List<String> selectedPlants, int chapter, int level) {
        User user = menuController.getLoggedInUser();
        if (user == null) {
            view.printError("ابتدا باید وارد حساب کاربری شوید.");
            return;
        }

        this.activeChapter = chapter;
        this.activeLevel = level;

        // مپ کردن شماره فصل به محیط بازی (مصر، غارها، ساحل، تاریکی)
        model.game.Season season = model.game.Season.NORMAL;
        if (chapter == 1) season = model.game.Season.ANCIENT_EGYPT;
        else if (chapter == 2) season = model.game.Season.FROSTBITE_CAVES;
        else if (chapter == 3) season = model.game.Season.BIG_WAVE_BEACH;
        else if (chapter == 4) season = model.game.Season.DARK_AGES;

        // مپ کردن مراحل ویژه (مثال: مرحله ۲ نوار نقاله، مرحله ۳ جنگ زمان‌دار)
        model.levelrules.LevelMode mode = model.levelrules.LevelMode.NORMAL;
        if (level == 2) mode = model.levelrules.LevelMode.CONVEYOR_BELT;
        else if (level == 3) mode = model.levelrules.LevelMode.TIMED_WAR;

        // ساخت سشن با متغیرهای دینامیک
        activeSession = new model.game.GameSession(user, 5, season, mode);
        inGame = true;
        view.printMessage("بازی شروع شد! [فصل " + chapter + " | مرحله " + level + " | " + season + "]");
    }

    private void finishGame() {
        User user = menuController.getLoggedInUser();
        if (user != null && activeSession != null) {
            user.incrementGamesPlayed();
            
            if (activeSession.isWon()) {
                user.incrementLevelsCompleted();
                user.getQuestContext().setStagesCompleted(user.getLevelsCompleted());

                // ثبت پیشرفت دقیق در پروفایل کاربر (ارتقای مرحله و فصل)
                if (activeChapter > user.getLastCompletedChapter() || 
                (activeChapter == user.getLastCompletedChapter() && activeLevel > user.getLastCompletedLevel())) {
                    
                    user.setLastCompletedLevel(activeLevel);
                    // اگر ۴ مرحله یک فصل تمام شد، فصل جدید باز می‌شود
                    if (activeLevel >= 4) { 
                        user.setLastCompletedChapter(activeChapter);
                        user.setLastCompletedLevel(0); // ریست برای شروع فصل جدید
                        view.printMessage("🏆 تبریک! شما فصل " + activeChapter + " را با موفقیت به پایان رساندید!");
                    }
                }
            }

            model.scoreGame.MeowPoint calculator = new model.scoreGame.MeowPoint();
            int totalMowPoints = calculator.calculateMyuPoints(activeSession.getMeowEvents());
            view.printMessage("امتیاز MeowPoints شما در این مرحله: " + totalMowPoints);

            user.updateMaxMowPoints(totalMowPoints);
            userManager.save(); // ذخیره روی فایل
        }
        inGame = false;
        activeSession = null;
        
        // بازگشت خودکار به منوی اصلی پس از اتمام بازی
        menuController.setCurrentMenu(model.menu.MenuType.MAIN);
        view.printMessage("به منوی اصلی بازگشتید.");
    }
}