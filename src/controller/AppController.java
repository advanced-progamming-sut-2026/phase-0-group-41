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
    private final MiniGameController miniGameController = new MiniGameController(view);

    private GameSession activeSession;
    private boolean inGame = false;
    private boolean inMiniGame = false; // آیا نشست فعلی یک مینی‌گیم است (نه مرحله‌ی عادی)
    private String activeMiniGameName = null;
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
            // اول دستورات اختصاصی مینی‌گیم (شکستن کوزه، کاشتن گردو، جابجایی و ...) را
            // امتحان می‌کنیم، چون این‌ها دستوراتی هستند که GameController آن‌ها را
            // نمی‌شناسد. اگر نشست فعلی مینی‌گیم نباشد، این متد بلافاصله false برمی‌گرداند.
            if (inMiniGame && miniGameController.handle(activeSession, rawLine, cmd)) {
                if (activeSession.isGameOver()) {
                    finishGame();
                }
                return;
            }
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

    /**
     * شروع یک نشست مینی‌گیم. برخلاف startGame (مراحل عادی adventure)، این متد
     * پیشرفت فصل/مرحله را دستکاری نمی‌کند؛ فقط در پایان، در صورت برد،
     * miniGamesCompleted کاربر را افزایش می‌دهد.
     *
     * @param name  یکی از: vasebreaker, wallnutbowling, izombie, beghouled
     * @param level سطح سختی مینی‌گیم (۱ تا ۳)
     */
    public void startMiniGame(String name, int level) {
        User user = menuController.getLoggedInUser();
        if (user == null) {
            view.printError("ابتدا باید وارد حساب کاربری شوید.");
            return;
        }

        GameSession session;
        switch (name.toLowerCase()) {
            case "vasebreaker":
                session = new model.minigame.VasebreakerSession(user, level);
                break;
            case "wallnutbowling":
                session = new model.minigame.WallnutBowlingSession(user, level);
                break;
            case "izombie":
                session = new model.minigame.IZombieSession(user, level);
                break;
            case "beghouled":
                session = new model.minigame.BeghouledSession(user, level);
                break;
            default:
                view.printError("مینی‌گیم ناشناخته: " + name);
                return;
        }

        this.activeMiniGameName = name.toLowerCase();
        this.activeSession = session;
        this.inGame = true;
        this.inMiniGame = true;
        menuController.setCurrentMenu(model.menu.MenuType.IN_GAME);
        view.printMessage("مینی‌گیم شروع شد! [" + name + " | سطح " + level + "]");
    }

    private void finishGame() {
        User user = menuController.getLoggedInUser();
        if (inMiniGame) {
            finishMiniGame(user);
            return;
        }

        if (user != null && activeSession != null) {
            user.incrementGamesPlayed();
            
            if (activeSession.isWon()) {
                user.incrementLevelsCompleted();
                user.getQuestContext().setStagesCompleted(user.getLevelsCompleted());
                user.getQuestManager().refreshCompletionStatus(user.getQuestContext());

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

    private void finishMiniGame(User user) {
        if (user != null && activeSession != null) {
            if (activeSession.isWon()) {
                user.setMiniGamesCompleted(user.getMiniGamesCompleted() + 1);
                view.printMessage("🏆 مینی‌گیم " + activeMiniGameName + " با موفقیت تمام شد!");
            } else {
                view.printMessage("مینی‌گیم " + activeMiniGameName + " را باختید.");
            }
            userManager.save();
        }
        inGame = false;
        inMiniGame = false;
        activeSession = null;
        activeMiniGameName = null;

        menuController.setCurrentMenu(model.menu.MenuType.MINI_GAMES);
        view.printMessage("به منوی مینی‌گیم‌ها بازگشتید.");
    }
}