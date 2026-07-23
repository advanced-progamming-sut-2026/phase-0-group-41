package controller;

import model.game.GameSession;
import model.minigame.BeghouledSession;
import model.minigame.IZombieSession;
import model.minigame.MiniGameSession;
import model.minigame.VasebreakerSession;
import model.minigame.WallnutBowlingSession;
import util.CommandLine;
import view.ConsoleView;

import java.util.List;

public class MiniGameController {

    private final ConsoleView view;

    public MiniGameController(ConsoleView view) {
        this.view = view;
    }

    public boolean handle(GameSession session, String rawLine, CommandLine cmd) {
        if (!(session instanceof MiniGameSession)) {
            return false;
        }

        List<String> t = cmd.getTokens();
        if (t.isEmpty()) return false;
        String first = t.get(0);

        MiniGameSession miniGame = (MiniGameSession) session;

        if (miniGame instanceof VasebreakerSession) {
            VasebreakerSession vasebreaker = (VasebreakerSession) miniGame;

            // هندل کردن ناپدید شدن بذرها در صورت گذر زمان (advance time)
            if (first.equals("advance") && t.size() >= 3 && t.get(1).equals("time")) {
                printDecayedSeeds(vasebreaker);
                // ما false برمی‌گردانیم تا GameController اصلی بتواند زمان را واقعا جلو ببرد
                return false; 
            }

            if (first.equals("break") && t.size() >= 2 && t.get(1).equals("vase")) {
                int[] loc = parseLocation(cmd, "l");
                if (loc != null) {
                    VasebreakerSession.VaseBreakResult result = vasebreaker.breakVase(loc[1], loc[0]);
                    handleVaseBreakResult(result, loc[0], loc[1]);
                    return true;
                }
            }

            if (first.equals("plant") && t.size() >= 2 && t.get(1).equals("seed")) {
                int[] source = parseLocation(cmd, "s");
                int[] dest = parseLocation(cmd, "d");
                if (source != null && dest != null) {
                    VasebreakerSession.PlantSeedResult result = vasebreaker.plantDroppedSeed(source[1], source[0], dest[1], dest[0]);
                    handlePlantSeedResult(result, dest[0], dest[1]);
                    return true;
                }
            }
        }

        if (miniGame instanceof WallnutBowlingSession) {
            WallnutBowlingSession bowling = (WallnutBowlingSession) miniGame;

            // هندل کردن چاپ رخدادهای برخورد هنگام رد شدن زمان
            if (first.equals("advance") && t.size() >= 3 && t.get(1).equals("time")) {
                printBowlingEvents(bowling);
                return false; // اجازه می‌دهیم GameController اصلی زمان را جلو ببرد
            }

            // دستور مشاهده نوار نقاله: show conveyor-belt
            if (first.equals("show") && t.size() >= 2 && t.get(1).equals("conveyor-belt")) {
                view.printMessage("نوار نقاله: " + bowling.getConveyorBelt().toString());
                return true;
            }

            // دستور کاشت گردو: plant -t <type> -l <x> <y>
            if (first.equals("plant") && t.size() >= 2 && t.get(1).equals("plant")) {
                String type = cmd.get("t");
                int[] loc = parseLocation(cmd, "l");
                
                if (loc != null && type != null) {
                    WallnutBowlingSession.PlantNutResult result = bowling.plantNut(type, loc[1], loc[0]);
                    handleBowlingPlantResult(result, type, loc[0], loc[1]);
                    return true;
                }
            }
        }

        if (miniGame instanceof IZombieSession) {
            IZombieSession iZombie = (IZombieSession) miniGame;

            // هندل کردن چاپ رخدادها (خوردن مغز، برد و باخت) هنگام رد شدن زمان
            if (first.equals("advance") && t.size() >= 3 && t.get(1).equals("time")) {
                printIZombieEvents(iZombie);
                return false; // اجازه می‌دهیم GameController اصلی زمان را جلو ببرد
            }

            // دستور کاشت زامبی: place zombie -t <type> -l <x> <y>
            if (first.equals("place") && t.size() >= 2 && t.get(1).equals("zombie")) {
                String type = cmd.get("t");
                int[] loc = parseLocation(cmd, "l");
                
                if (loc != null && type != null) {
                    IZombieSession.PlaceZombieResult result = iZombie.placeZombie(type, loc[1], loc[0]);
                    handlePlaceZombieResult(result, type, loc[0], loc[1]);
                    return true;
                }
            }
        }

        if (miniGame instanceof BeghouledSession) {
            BeghouledSession beghouled = (BeghouledSession) miniGame;

            // هندل کردن چاپ رخدادهای مچ شدن زنجیره‌ای یا پیروزی
            if (first.equals("advance") && t.size() >= 3 && t.get(1).equals("time")) {
                printBeghouledEvents(beghouled);
                return false; // اجازه می‌دهیم GameController اصلی زمان را جلو ببرد
            }

            // دستور جابجایی: swap -l1 <x1> <y1> -l2 <x2> <y2>
            if (first.equals("swap")) {
                int[] loc1 = parseLocation(cmd, "l1");
                int[] loc2 = parseLocation(cmd, "l2");
                
                if (loc1 != null && loc2 != null) {
                    BeghouledSession.SwapResult result = beghouled.swapPlants(loc1[1], loc1[0], loc2[1], loc2[0]);
                    handleSwapResult(result);
                    printBeghouledEvents(beghouled); // چاپ اتفاقاتی که همون لحظه افتاده
                    return true;
                }
            }

            // دستور ارتقا: upgrade plant -t <old_plant_name>
            if (first.equals("upgrade") && t.size() >= 2 && t.get(1).equals("plant")) {
                String oldPlant = cmd.get("t");
                if (oldPlant != null) {
                    BeghouledSession.UpgradeResult result = beghouled.upgradePlant(oldPlant);
                    handleUpgradeResult(result, oldPlant);
                    printBeghouledEvents(beghouled);
                    return true;
                }
            }
        }

        return false;
    }

    private void handleVaseBreakResult(VasebreakerSession.VaseBreakResult result, int x, int y) {
        switch (result.status) {
            case INVALID_LOCATION:
                view.printError("مختصات نامعتبر است.");
                break;
            case NO_VASE:
                view.printError("کوزه‌ای در این خانه وجود ندارد.");
                break;
            case GREEN_SEED:
                view.printMessage("کوزه سبز در (" + x + "," + y + ") شکست! یک بسته بذر [" + result.contentName + "] افتاد. سریع آن را بکارید!");
                break;
            case PURPLE_GARGANTUAR:
                view.printMessage("کوزه بنفش در (" + x + "," + y + ") شکست! یک غول (Gargantuar) ظاهر شد!");
                break;
            case NORMAL_ZOMBIE:
                view.printMessage("کوزه عادی شکست! زامبی [" + result.contentName + "] بیرون آمد.");
                break;
            case NORMAL_SEED:
                view.printMessage("کوزه عادی شکست! بذر [" + result.contentName + "] افتاد.");
                break;
            case NORMAL_EMPTY:
                view.printMessage("کوزه عادی شکست! داخل آن خالی بود.");
                break;
        }
    }

    private void handlePlantSeedResult(VasebreakerSession.PlantSeedResult result, int x, int y) {
        switch (result) {
            case NO_SEED:
                view.printError("بذر افتاده‌ای در این مختصات یافت نشد یا مهلت کاشت آن تمام شده است.");
                break;
            case INVALID_TARGET:
                view.printError("نمی‌توانید در این خانه گیاه بکارید.");
                break;
            case SUCCESS:
                view.printMessage("گیاه با موفقیت از بذر استخراج شد و در (" + x + ", " + y + ") کاشته شد.");
                break;
        }
    }

    private void printDecayedSeeds(VasebreakerSession session) {
        List<VasebreakerSession.DroppedSeedPacket> decayed = session.pollDecayedSeeds();
        for (VasebreakerSession.DroppedSeedPacket ds : decayed) {
            view.printMessage("توجه: مهلت بذر " + ds.plantType + " در مختصات (" + ds.col + ", " + ds.row + ") تمام شد و ناپدید گردید.");
        }
    }

    private int[] parseLocation(CommandLine cmd, String flag) {
        List<String> values = cmd.getMulti(flag);
        if (values.isEmpty()) return null;
        String joined = String.join(" ", values).replace("(", "").replace(")", "").replace(",", " ").trim();
        String[] parts = joined.split("\\s+");
        if (parts.length < 2) return null;
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void handleBowlingPlantResult(WallnutBowlingSession.PlantNutResult result, String type, int x, int y) {
        switch (result) {
            case INVALID_LOCATION:
                view.printError("مختصات نامعتبر است.");
                break;
            case BEYOND_RED_LINE:
                view.printError("شما فقط می‌توانید پشت خط قرمز (ستون ۰ تا ۲) گردو بکارید!");
                break;
            case NOT_IN_CONVEYOR:
                view.printError("گردوی " + type + " در حال حاضر در نوار نقاله موجود نیست.");
                break;
            case SUCCESS:
                view.printMessage("گردوی " + type + " در (" + x + "," + y + ") کاشته شد و شروع به غلتیدن کرد!");
                break;
        }
    }

    private void printBowlingEvents(WallnutBowlingSession session) {
        List<String> events = session.pollRecentEvents();
        for (String event : events) {
            view.printMessage(">>> " + event);
        }
    }

    private void handlePlaceZombieResult(IZombieSession.PlaceZombieResult result, String type, int x, int y) {
        switch (result) {
            case INVALID_LOCATION:
                view.printError("مختصات نامعتبر است.");
                break;
            case BEYOND_RED_LINE:
                view.printError("شما فقط می‌توانید در سمت راست خط قرمز زامبی قرار دهید.");
                break;
            case INVALID_ZOMBIE:
                view.printError("نام زامبی ناشناخته است: " + type);
                break;
            case NOT_ENOUGH_SUN:
                view.printError("خورشید کافی برای خرید زامبی " + type + " ندارید.");
                break;
            case SUCCESS:
                view.printMessage("زامبی " + type + " با موفقیت در (" + x + "," + y + ") قرار داده شد.");
                break;
        }
    }

    private void printIZombieEvents(IZombieSession session) {
        List<String> events = session.pollRecentEvents();
        for (String event : events) {
            view.printMessage(">>> " + event);
        }
    }

    private void handleSwapResult(BeghouledSession.SwapResult result) {
        switch (result) {
            case INVALID_LOCATION:
                view.printError("مختصات وارد شده خارج از زمین بازی است.");
                break;
            case NOT_ADJACENT:
                view.printError("فقط گیاهان مجاور (چپ، راست، بالا، پایین) قابل جابجایی هستند.");
                break;
            case EMPTY_TILE:
                view.printError("یکی از خانه‌ها خالی است (گودال). جابجایی ممکن نیست.");
                break;
            case NO_MATCH:
                view.printError("این جابجایی ترکیب سه‌تایی ایجاد نکرد (بازگشت به حالت قبل).");
                break;
            case SUCCESS:
                view.printMessage("جابجایی با موفقیت انجام شد!");
                break;
        }
    }

    private void handleUpgradeResult(BeghouledSession.UpgradeResult result, String plantName) {
        switch (result) {
            case INVALID_UPGRADE:
                view.printError("امکان ارتقای گیاه " + plantName + " وجود ندارد یا این گیاه در زمین نیست.");
                break;
            case NOT_ENOUGH_SUN:
                view.printError("خورشید کافی برای این ارتقا ندارید.");
                break;
            case SUCCESS:
                view.printMessage("تمامی گیاهان " + plantName + " در سطح زمین و آسمان با موفقیت ارتقا یافتند!");
                break;
        }
    }

    private void printBeghouledEvents(BeghouledSession session) {
        List<String> events = session.pollRecentEvents();
        for (String event : events) {
            view.printMessage(">>> " + event);
        }
    }
    
}