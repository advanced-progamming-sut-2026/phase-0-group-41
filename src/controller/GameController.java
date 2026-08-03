package controller;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.plant.plants.Sunflower;
import model.sun.FallingSun;
import model.user.User;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;
import util.CommandLine;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** دستورات داخل یک مرحله در حال بازی: کاشتن، برداشتن، جلو بردن زمان و تقلب‌ها. */
public class GameController {

    private final ConsoleView view;

    public GameController(ConsoleView view) {
        this.view = view;
    }

    public boolean handle(GameSession session, String rawLine, CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) {
            return false;
        }
        String first = t.get(0);

        if (first.equals("advance") && t.size() >= 2 && t.get(1).equals("time")) {
            int baseCount = Integer.parseInt(cmd.get("t"));

            // === اعمال ضریب سختی برای سرعت پیش‌روی بازی ===
            int dl = session.getUser().getDifficultyLevel();
            int actualCount = (int) (baseCount * (dl / 3.0));

            // حالا بازی به جای مقدار پایه، با سرعتِ محاسبه شده جلو می‌رود
            session.advanceTicks(actualCount);

            if (session.isGameOver()) {
                view.printMessage(session.isWon() ? "شما بردید!" : "شما باختید.");
            }
            return true;
        }
        if (first.equals("plant") && t.size() >= 2 && t.get(1).equals("plant")) {
            plantPlant(session, cmd);
            return true;
        }
        if (first.equals("pluck") && t.size() >= 2 && t.get(1).equals("plant")) {
            pluckPlant(session, cmd);
            return true;
        }
        if (first.equals("collect") && t.size() >= 2 && t.get(1).equals("sun")) {
            collectSun(session, cmd);
            return true;
        }
        if (first.equals("feed") && t.size() >= 2 && t.get(1).equals("plant")) {
            feedPlant(session, cmd);
            return true;
        }
        if (first.equals("show")) {
            return handleShow(session, t, cmd);
        }
        if (first.equals("cheat")) {
            return handleCheat(session, t, cmd);
        }
        if (first.equals("release") && t.size() >= 3 && t.get(1).equals("the") && t.get(2).equals("nuke")) {
            session.getAliveZombies().clear();
            view.printMessage("همه‌ی زامبی‌های نقشه از بین رفتند.");
            return true;
        }
        return false;
    }

    private boolean handleShow(GameSession session, List<String> t, CommandLine cmd) {
        if (t.size() >= 2 && t.get(1).equals("map")) {
            view.printMap(session);
            return true;
        }
        if (t.size() >= 3 && t.get(1).equals("sun") && t.get(2).equals("amount")) {
            view.printMessage("Sun: " + session.getSunManager().getCurrentSun());
            return true;
        }
        if (t.size() >= 3 && t.get(1).equals("tile") && t.get(2).equals("status")) {
            showTileStatus(session, cmd);
            return true;
        }

        if (t.size() >= 3 && t.get(1).equals("plants") && t.get(2).equals("status")) {
            showPlantsStatus(session);
            return true;
        }

        if (t.size() >= 3 && t.get(1).equals("zombies") && t.get(2).equals("info")) {
            for (Zombie z : session.getAliveZombies()) {
                view.printZombieInfo(z);
            }
            return true;
        }
        // ------------------------------

        if (t.size() >= 2 && t.get(1).equals("map")) {
            view.printMap(session);
            return true;
        }
        return false;
    }

    private boolean handleCheat(GameSession session, List<String> t, CommandLine cmd) {
        if (t.size() >= 2 && t.get(1).equals("add")) {
            List<String> nValues = cmd.getMulti("n");
            if (nValues.size() >= 2 && nValues.get(1).equalsIgnoreCase("suns")) {
                int n = Integer.parseInt(nValues.get(0));
                session.getSunManager().addSun(n);
                view.printMessage("خورشید اضافه شد. مجموع: " + session.getSunManager().getCurrentSun());
                return true;
            }
        }
        if (t.size() >= 2 && t.get(1).equals("remove-cooldown")) {
            session.clearAllCooldowns();
            view.printMessage("همه‌ی محدودیت‌های cooldown حذف شدند.");
            return true;
        }
        if (t.size() >= 2 && t.get(1).equals("add-plant-food")) {
            session.addPlantFood();
            view.printMessage("یک غذای گیاه اضافه شد.");
            return true;
        }

        // ----------------- بخش اصلاح شده برای اسپاون زامبی -----------------
        if (t.size() >= 2 && t.get(1).equals("spawn-zombie")) {
            String type = cmd.get("t");
            int[] loc = parseLocation(cmd); // استفاده از متد خودتون برای خواندن x و y

            if (type == null || loc == null) {
                view.printError("فرمت دستور اشتباه است. الگو: cheat spawn-zombie -t <type> -l <x,y>");
                return true;
            }

            try {
                // ---> تغییر مهم: ارسال درجه سختی کاربر به کارخانه زامبی‌سازی <---
                int dl = session.getUser().getDifficultyLevel();
                Zombie z = ZombieFactory.create(type, dl);

                // قرار دادن زامبی در مختصات درخواستی (loc[1] سطر و loc[0] ستون است)
                z.spawn(loc[1], loc[0]);
                z.setSpawnTick((int) session.getTickCount());
                session.getAliveZombies().add(z);


                view.printMessage("زامبی " + type + " با موفقیت در مختصات (" + loc[0] + ", " + loc[1] + ") ظاهر شد.");
            } catch (IllegalArgumentException e) {
                view.printError(e.getMessage());
            }
            return true;
        }
        // -------------------------------------------------------------------
        return false;
    }

    private void plantPlant(GameSession session, CommandLine cmd) {
        String type = cmd.get("t");
        int[] loc = parseLocation(cmd);
        if (type == null || loc == null) {
            view.printError("پارامترهای دستور نامعتبر است.");
            return;
        }
        if (!PlantFactory.isKnown(type)) {
            view.printError("گیاه ناشناخته: " + type);
            return;
        }
        if (session.isPlantOnCooldown(type)) {
            view.printError("این گیاه در حال حاضر در cooldown است.");
            return;
        }
        Tile tile = session.getBoard().getTile(loc[1], loc[0]);
        if (tile == null) {
            view.printError("مکان نامعتبر است.");
            return;
        }
        if (!tile.isEmpty()) {
            view.printError("این خانه از قبل گیاه دارد.");
            return;
        }

        Plant plant = PlantFactory.create(type);
        User user = session.getUser();

        if (!tile.canPlant(plant)) {
            view.printError("نمی‌توان مستقیم در این خانه کاشت (آب/موانع).");
            return;
        }
        
        int currentPlantLevel = user.getPlantLevel(type);
        plant.applyUpgradeLevel(currentPlantLevel);

        if (user.hasGreenhouseBoost(type)) {
            plant.setGreenhouseBoosted(true);
            user.consumeGreenhouseBoost(type);
            view.printMessage("بوست گلخانه برای گیاه " + type + " فعال شد.");
        }
        if (!session.getSunManager().spendSun(plant.getSunCost())) {
            view.printError("خورشید کافی برای کاشت این گیاه ندارید.");
            return;
        }
        plant.place(loc[1], loc[0]);
        tile.setPlant(plant);
        session.startPlantCooldown(type, plant.getCooldownTicks());
        view.printMessage("گیاه " + type + " در (" + loc[0] + ", " + loc[1] + ") کاشته شد.");
    }

    private void pluckPlant(GameSession session, CommandLine cmd) {
        int[] loc = parseLocation(cmd);
        if (loc == null) {
            view.printError("پارامتر مکان نامعتبر است.");
            return;
        }
        Tile tile = session.getBoard().getTile(loc[1], loc[0]);
        if (tile == null || tile.isEmpty()) {
            view.printError("گیاهی در این خانه وجود ندارد.");
            return;
        }
        tile.setPlant(null);
        view.printMessage("گیاه از (" + loc[0] + ", " + loc[1] + ") برداشته شد.");
    }

    private void collectSun(GameSession session, CommandLine cmd) {
        int[] loc = parseLocation(cmd);
        if (loc == null) {
            view.printError("پارامتر مکان نامعتبر است.");
            return;
        }
        
        int col = loc[0];
        int row = loc[1];

        // ۱. بررسی خورشیدهای آسمانی (Falling Suns)  
        List<FallingSun> fallingSuns = session.getFallingSuns();
        for (int i = 0; i < fallingSuns.size(); i++) {
            FallingSun fs = fallingSuns.get(i);
            
            if (fs.getRow() == row && fs.getCol() == col) {
                boolean wasLanded = fs.isLanded();
                FallingSun.Kind kind = fs.getKind();
                
                fallingSuns.remove(i);

                // بررسی انفجار خورشید رادیواکتیو (اگر در هوا گرفته شود)
                if (kind == FallingSun.Kind.RADIOACTIVE && !wasLanded) {
                    view.printMessage(" خورشید رادیواکتیو پیش از رسیدن به زمین گرفته شد! انفجار رخ داد!");
                    triggerRadioactiveExplosion(session, row, col);
                    return; // بعد از انفجار، خورشیدی به بانک اضافه نمی‌شود
                }

                // محاسبه مقدار خورشیدی که به بانک می‌رود
                int amountToAdd = kind.getValue();
                // اگر خورشید رادیواکتیو به زمین رسیده باشد، مثل یک خورشید عادی عمل می‌کند
                if (kind == FallingSun.Kind.RADIOACTIVE && wasLanded) {
                    amountToAdd = FallingSun.Kind.NORMAL.getValue();
                }

                session.getSunManager().addSun(amountToAdd);
                view.printMessage("خورشید آسمانی برداشت شد (+" + amountToAdd + "). مجموع: " + session.getSunManager().getCurrentSun());
                return;
            }
        }

        // ۲. بررسی گیاهان تولیدکننده خورشید (Sun Producers)
        Tile tile = session.getBoard().getTile(row, col);
        if (tile != null && tile.getPlant() instanceof model.plant.interfaces.ISunProducer) {
            model.plant.interfaces.ISunProducer producer = (model.plant.interfaces.ISunProducer) tile.getPlant();
            if (producer.isSunReady()) {
                int amountToAdd = producer.getReadySunAmount();
                producer.collectSun(); // وضعیت تولید خورشید گیاه را ریست می‌کند
                session.getSunManager().addSun(amountToAdd);
                view.printMessage("خورشید گیاهی برداشت شد (+" + amountToAdd + "). مجموع: " + session.getSunManager().getCurrentSun());
                return;
            }
        }
        view.printError("خورشیدی برای برداشت در این مکان وجود ندارد.");
    }

    private void triggerRadioactiveExplosion(GameSession session, int centerRow, int centerCol) {
        // اعمال ۱۵۰ دمیج به زامبی‌ها در شعاع ۵x۵
        for (Zombie z : session.getAliveZombies()) {
            int zCol = (int) Math.floor(z.getXPosition());
            if (Math.abs(z.getRow() - centerRow) <= 2 && Math.abs(zCol - centerCol) <= 2) {
                z.takeDamage(150);
            }
        }
        
        // اعمال ۸۰ دمیج به گیاهان در شعاع ۳x۳
        for (int r = centerRow - 1; r <= centerRow + 1; r++) {
            for (int c = centerCol - 1; c <= centerCol + 1; c++) {
                Tile t = session.getBoard().getTile(r, c);
                if (t != null && t.getPlant() != null) {
                    t.getPlant().takeDamage(80);
                    if (t.getPlant().isDead()) {
                        t.setPlant(null); // پاک‌سازی گیاه مرده از روی زمین
                    }
                }
            }
        }
    }

    private void feedPlant(GameSession session, CommandLine cmd) {
        int[] loc = parseLocation(cmd);
        if (loc == null || session.getPlantFoodCount() <= 0) {
            view.printError("غذای گیاه یا مکان نامعتبر است.");
            return;
        }
        Tile tile = session.getBoard().getTile(loc[1], loc[0]);
        if (tile == null || tile.isEmpty()) {
            view.printError("گیاهی در این خانه وجود ندارد.");
            return;
        }
        tile.getPlant().feed(session);
        view.printMessage("گیاه تغذیه شد.");
    }

    private void showTileStatus(GameSession session, CommandLine cmd) {
        int[] loc = parseLocation(cmd);
        if (loc == null) {
            view.printError("پارامتر مکان نامعتبر است.");
            return;
        }
        Tile tile = session.getBoard().getTile(loc[1], loc[0]);
        if (tile == null) {
            view.printError("مکان نامعتبر است.");
            return;
        }
        if (tile.getPlant() != null) {
            Plant p = tile.getPlant();
            view.printMessage("Plant: " + p.getName() + " health=" + p.getHealth() + "/" + p.getMaxHealth());
        }
        for (Zombie z : tile.getZombies()) {
            view.printZombieInfo(z);
        }
        if (tile.getPlant() == null && tile.getZombies().isEmpty()) {
            view.printMessage("این خانه خالی است.");
        }
    }

    private int[] parseLocation(CommandLine cmd) {
        List<String> values = cmd.getMulti("l");
        if (values.isEmpty()) {
            return null;
        }
        String joined = String.join(" ", values);
        String cleaned = joined.replace("(", "").replace(")", "").replace(",", " ").trim();
        String[] parts = cleaned.split("\\s+");
        if (parts.length < 2) {
            return null;
        }
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showPlantsStatus(GameSession session) {
        view.printMessage("--- وضعیت گیاهان ---");
        
        // دریافت لیست گیاهانی که کاربر آنلاک کرده و در این مرحله در دسترسش هستند
        java.util.Set<String> availablePlants = session.getUser().getUnlockedPlants();
        int currentSun = session.getSunManager().getCurrentSun();

        for (String plantName : availablePlants) {
            try {
                // ساخت یک نمونه موقت برای خواندن اطلاعات پایه گیاه
                Plant p = PlantFactory.create(plantName);
                
                // اعمال ارتقاهای کاربر روی گیاه برای محاسبه قیمت دقیق
                int userPlantLevel = session.getUser().getPlantLevel(plantName);
                p.applyUpgradeLevel(userPlantLevel);
                
                int cost = p.getSunCost();
                int cdTicks = session.getPlantCooldownRemaining(plantName);
                
                String status;
                if (cdTicks > 0) {
                    // تبدیل تیک به ثانیه (هر 10 تیک = 1 ثانیه)
                    status = "در حال شارژ (" + (cdTicks / 10.0) + " ثانیه)";
                } else if (currentSun < cost) {
                    status = "خورشید ناکافی";
                } else {
                    status = "آماده کاشت";
                }
                
                // چاپ فرمت‌بندی شده اطلاعات
                view.printMessage(String.format("- %-15s | هزینه: %-4d | وضعیت: %s", plantName, cost, status));
            } catch (Exception e) {
                // نادیده گرفتن نام‌های نامعتبر احتمالی
            }
        }
        view.printMessage("--------------------");
    }

}