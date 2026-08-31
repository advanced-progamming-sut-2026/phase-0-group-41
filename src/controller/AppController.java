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
    private int activeMiniGameLevel = 1;
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
        // ذخیره‌ی محلی (سازگاری با کدهای فاز ۱) + ارسال آخرین وضعیت کاربر به سرور
        // طبق سند فاز ۳ («ذخیره‌سازی اطلاعات باید از سمت کلاینت به سمت سرور منتقل شود»)
        userManager.save();
        network.UserSync.push(menuController.getLoggedInUser());
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
        if (!model.game.ChapterPlan.isValidChapter(chapter) || !model.game.ChapterPlan.isValidLevel(level)) {
            view.printError("شماره‌ی فصل یا مرحله نامعتبر است.");
            return;
        }

        this.activeChapter = chapter;
        this.activeLevel = level;

        // فصل بازی (Season) و نوع مرحله (LevelMode) از منبع واحد ChapterPlan خوانده می‌شود
        // تا کنسول و گرافیک دقیقاً یک رفتار داشته باشند.
        model.game.Season season = model.game.ChapterPlan.seasonFor(chapter);
        model.levelrules.LevelMode mode = model.game.ChapterPlan.levelModeFor(chapter, level);
        int totalWaves = model.game.ChapterPlan.totalWavesFor(chapter, level);
        double baseWaveCost = model.game.ChapterPlan.baseWaveCostFor(chapter, level);

        // ساخت سشن با متغیرهای دینامیک؛ هر مرحله‌ی بعدی در همان فصل (تعداد موج و
        // هزینه‌ی موج اول بیشتر) سخت‌تر از مرحله‌ی قبلی است.
        activeSession = new model.game.GameSession(user, totalWaves, baseWaveCost, season, mode);
        inGame = true;
        view.printMessage("بازی شروع شد! [فصل " + model.game.ChapterPlan.displayName(chapter)
                + " | مرحله " + level + " | " + mode + "]");
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
        if (!user.isMiniGameLevelUnlocked(name.toLowerCase(), level)) {
            view.printError("سطح " + level + " هنوز قفل است. ابتدا باید سطح قبلی این مینی‌گیم را ببرید.");
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
        this.activeMiniGameLevel = level;
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

                if (activeChapter == model.game.ChapterPlan.BEGINNER_CHAPTER) {
                    // فصل Beginner پیشرفت جداگانه‌ای دارد و روی lastCompletedChapter اثر نمی‌گذارد
                    if (activeLevel > user.getBeginnerLastCompletedLevel()) {
                        user.setBeginnerLastCompletedLevel(activeLevel);
                    }
                    if (activeLevel >= model.game.ChapterPlan.LEVELS_PER_CHAPTER) {
                        view.printMessage("🏆 تبریک! شما فصل Beginner را با موفقیت به پایان رساندید!");
                    }
                } else if (activeChapter > user.getLastCompletedChapter() ||
                        (activeChapter == user.getLastCompletedChapter() && activeLevel > user.getLastCompletedLevel())) {

                    user.setLastCompletedLevel(activeLevel);
                    // اگر ۴ مرحله یک فصل تمام شد، فصل جدید باز می‌شود
                    if (activeLevel >= model.game.ChapterPlan.LEVELS_PER_CHAPTER) {
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
            userManager.save(); // ذخیره‌ی محلی (سازگاری با کدهای قبلی)
            network.UserSync.push(user); // ارسال به سرور تا از دستگاه دیگر هم دیده شود
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
                user.recordMiniGameLevelWon(activeMiniGameName, activeMiniGameLevel);
                view.printMessage("🏆 مینی‌گیم " + activeMiniGameName + " (سطح " + activeMiniGameLevel + ") با موفقیت تمام شد!");
                if (activeMiniGameLevel < 3) {
                    view.printMessage("سطح " + (activeMiniGameLevel + 1) + " اکنون باز شد.");
                }
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