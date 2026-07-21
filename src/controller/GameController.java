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

        if (first.equals("advance") && t.size() >= 3 && t.get(1).equals("time")) {
            int count = Integer.parseInt(cmd.get("t"));
            session.advanceTicks(count);
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
        // --- اضافه کردن دستور جدید ---
        if (t.size() >= 3 && t.get(1).equals("zombies") && t.get(2).equals("info")) {
            for (Zombie z : session.getAliveZombies()) {
                view.printZombieInfo(z); // صدا زدن متد در ConsoleView که خودش وصله به ZombieView
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
                // درخواست ساخت زامبی از کارخانه
                Zombie z = ZombieFactory.create(type);

                // قرار دادن زامبی در مختصات درخواستی (loc[1] سطر و loc[0] ستون است)
                z.spawn(loc[1], loc[0]);
                session.getAliveZombies().add(z);

                view.printMessage("زامبی " + type + " با موفقیت در مختصات (" + loc[0] + ", " + loc[1] + ") ظاهر شد.");
            } catch (IllegalArgumentException e) {
                view.printError(e.getMessage()); // اگر اسم زامبی اشتباه بود، ارور چاپ می‌شود
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
        if (!tile.canPlantDirectly()) {
            view.printError("نمی‌توان مستقیم در این خانه کاشت (آب/موانع).");
            return;
        }
        Plant plant = PlantFactory.create(type);
        User user = session.getUser();
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
        Tile tile = session.getBoard().getTile(loc[1], loc[0]);
        if (tile != null && tile.getPlant() instanceof Sunflower) {
            Sunflower sunflower = (Sunflower) tile.getPlant();
            if (sunflower.isSunReady()) {
                sunflower.collectSun();
                session.getSunManager().addSun(25);
                view.printMessage("خورشید برداشت شد. مجموع: " + session.getSunManager().getCurrentSun());
                return;
            }
        }
        view.printError("خورشیدی برای برداشت در این مکان وجود ندارد.");
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
}