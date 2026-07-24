package model.game;

import model.plant.Plant;
import model.sun.FallingSun;
import model.sun.SunManager;
import model.user.User;
import model.wave.WaveManager;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * یک جلسه‌ی در حال بازی: تخته، خورشیدها، موج‌ها و همه‌ی زامبی/گیاه‌های روی زمین.
 * با «advance time» به جلو برده می‌شود.
 */
public class GameSession {
    private final Season currentSeason; // === بخش فصلی: نگهداری فصل فعلی ===
    private int waterStartColumn = 9;   // === بخش فصلی: برای ساحل موج بزرگ ===

    private final User user;
    private final Board board = new Board();
    private final SunManager sunManager ;
    private final WaveManager waveManager;
    private final List<Zombie> aliveZombies = new ArrayList<>();
    private final List<FallingSun> fallingSuns = new ArrayList<>();
    private final Map<String, Integer> plantCooldowns = new HashMap<>(); // نام گیاه -> تیک باقیمانده
    private final Random random = new Random();

    private long tickCount = 0;
    private int plantFoodCount = 0;
    private boolean gameOver = false;
    private boolean won = false;
    private double waveHealthAtStart = 0;
    private double waveHealthRemaining = 0;

    public GameSession(User user, int totalWaves, Season season) {
        this.user = user;
        this.waveManager = new WaveManager(totalWaves, 50);
        int userDifficulty = user.getDifficultyLevel();
        this.sunManager = new SunManager(userDifficulty);
        this.currentSeason = season; // === بخش فصلی: مقداردهی فصل ===
    }

    public Board getBoard() {
        return board;
    }

    public SunManager getSunManager() {
        return sunManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public User getUser() {
        return user;
    }

    public List<Zombie> getAliveZombies() {
        return aliveZombies;
    }

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public void addPlantFood() {
        if (plantFoodCount < 3) {
            plantFoodCount++;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }
    public List<FallingSun> getFallingSuns() {
        return fallingSuns;
    }
    public boolean isPlantOnCooldown(String plantName) {
        return plantCooldowns.getOrDefault(plantName, 0) > 0;
    }

    public int getPlantCooldownRemaining(String plantName) {
        return plantCooldowns.getOrDefault(plantName, 0);
    }

    public void startPlantCooldown(String plantName, int ticks) {
        plantCooldowns.put(plantName, ticks);
    }

    public void clearAllCooldowns() {
        plantCooldowns.clear();
    }

    /** یک تیک بازی را جلو می‌برد (۱۰ تیک = ۱ ثانیه). */
    public void advanceOneTick() {
        if (gameOver) {
            return;
        }
        tickCount++;

        // کاهش کول‌داون‌های گیاهان
        for (String key : new ArrayList<>(plantCooldowns.keySet())) {
            int remaining = plantCooldowns.get(key) - 1;
            if (remaining <= 0) {
                plantCooldowns.remove(key);
            } else {
                plantCooldowns.put(key, remaining);
            }
        }

        // تیک گیاهان
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant plant = board.getTile(r, c).getPlant();
                if (plant != null && !plant.isDead()) {
                    plant.onTick(this);
                }
            }
        }

        // حرکت / حمله زامبی‌ها
        List<Zombie> deadZombies = new ArrayList<>();
        for (Zombie zombie : aliveZombies) {
            zombie.onTick(this);
            if (zombie.isDead()) {
                deadZombies.add(zombie);
            }
        }
        for (Zombie z : deadZombies) {
            aliveZombies.remove(z);
            System.out.println("Zombie of type " + z.getTypeName() + " is dead at (" + (int) z.getXPosition() + ", " + z.getRow() + ")");
            if (user.getQuestManager() != null) {
                // ۱. ثبت کیل عادی برای کوئست‌ها
                user.getQuestManager().recordZombieKill(user.getQuestContext());
            }

            // ۲. محاسبه زمان زنده بودن زامبی بر حسب میلی‌ثانیه
            int ticksLived = (int)this.tickCount - z.getSpawnTick();
            double timeLivedMs = ticksLived * 1000.0; // فرض: هر تیک ۱۰۰۰ میلی‌ثانیه است

            // ۳. ساخت رویداد کشته شدن سریع و ارسال به سشن
            // حتما کلاس MeowPoint ایمپورت شده باشد
            model.quest.MeowPoint.GameEvent fastKillEvent = new model.quest.MeowPoint.GameEvent(
                    model.quest.MeowPoint.EventType.ZOMBIE_KILLED_FAST,
                    1,
                    timeLivedMs
            );
            dropRandomReward();
        }

        // === بخش فصلی: سقوط خورشید (فقط در صورتی که عصر تاریکی نباشد) ===
        if (currentSeason != Season.DARK_AGES) {
            FallingSun newSun = sunManager.tick(board);
            if (newSun != null) {
                fallingSuns.add(newSun);
                System.out.println("New " + newSun.getKind() + " sun is dropping at position (" + newSun.getCol() + ", " + newSun.getRow() + ")");
            }
        }

        for (FallingSun fs : fallingSuns) {
            boolean wasLanded = fs.isLanded();
            fs.tick();
            if (!wasLanded && fs.isLanded()) {
                System.out.println("Sun reached the ground at position (" + fs.getCol() + ", " + fs.getRow() + ")");
            }
        }

        checkWaveProgress();
        checkGameOverConditions();
    }

    public void advanceTicks(int count) {
        for (int i = 0; i < count; i++) {
            advanceOneTick();
            if (gameOver) {
                break;
            }
        }
    }

    private void checkWaveProgress() {
        if (gameOver) {
            return;
        }
        if (!waveManager.isWaveActive()) {
            if (waveManager.allWavesDone()) {
                won = true;
                gameOver = true;
                System.out.println("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                return;
            }
            spawnNextWave();
            return;
        }
        // بررسی اینکه ۷۵٪ سلامتی موج قبلی از بین رفته یا خیر
        double totalRemaining = 0;
        for (Zombie z : aliveZombies) {
            totalRemaining += z.getHealth();
        }
        waveHealthRemaining = totalRemaining;
        if (waveHealthAtStart > 0 && waveHealthRemaining <= waveHealthAtStart * 0.25) {
            waveManager.setWaveActive(false);
        }
    }

    private void spawnNextWave() {
        double cost = waveManager.startNextWaveAndGetCost();

        // === بخش فصلی: اتفاقاتی که در ابتدای هر موج می‌افتد ===
        if (currentSeason == Season.BIG_WAVE_BEACH) {
            // آب تصادفی جلو یا عقب می‌رود (مثلاً ستون ۵ تا ۹)
            waterStartColumn = 5 + random.nextInt(5);
            applyTideLevel();
            System.out.println("Tide level changed! Water starts at column " + waterStartColumn);
        }
        else if (currentSeason == Season.FROSTBITE_CAVES) {
            // باد یخی به صورت تصادفی می‌وزد
            if (random.nextBoolean()) {
                applyIceWind();
                System.out.println("Ice wind is blowing!");
            }
        }
        else if (currentSeason == Season.DARK_AGES) {
            // در عصر تاریکی ممکنه اول هر موج قبر جدید ظاهر بشه
            spawnRandomGraves(2);
        }
        // ==========================================================

        if (waveManager.isFinalWave()) {
            System.out.println("The final wave has come.");
        } else {
            System.out.println("Wave " + waveManager.getCurrentWave() + " started.");
        }

        double remainingCost = cost;
        double totalHealth = 0;

        // === بخش فصلی: بررسی گردباد برای موج آخر مصر باستان ===
        boolean isTornadoWave = (currentSeason == Season.ANCIENT_EGYPT && waveManager.isFinalWave());

        while (remainingCost > 0) {
            Zombie z = ZombieFactory.randomBasicZombie(user.getDifficultyLevel());
            int lane = random.nextInt(Board.ROWS);

            int spawnCol = Board.COLS - 1;
            // اگر گردباد فعال باشد، زامبی ۱ تا ۴ ستون جلوتر می‌آید
            if (isTornadoWave) {
                spawnCol -= (1 + random.nextInt(4));
            }

            z.spawn(lane, spawnCol);
            aliveZombies.add(z);
            totalHealth += z.getHealth();
            System.out.println("Zombie " + z.getTypeName() + " spawned at wave " + waveManager.getCurrentWave()
                    + " in lane " + lane + " which costed " + z.getWaveCost() + ".");
            remainingCost -= z.getWaveCost();
        }
        waveHealthAtStart = totalHealth;
    }

    private void dropRandomReward() {
        double roll = random.nextDouble();
        if (roll < 0.10) {
            double kindRoll = random.nextDouble();
            if (kindRoll < 0.34) {
                user.addCoins(50);
                System.out.println("A zombie dropped a coin; you have " + user.getCoins() + " coins now.");
            } else if (kindRoll < 0.67) {
                user.addDiamonds(1);
                System.out.println("A zombie dropped a diamond; you have " + user.getDiamonds() + " diamonds now.");
            } else {
                System.out.println("A zombie dropped a pot; you have a new greenhouse pot now.");
            }
        }
    }

    private void checkGameOverConditions() {
        // اگر زامبی به انتهای ردیف برسد (x <= 0) و ماشین چمن‌زنی فعال نشود، بازی باخته می‌شود
        List<Zombie> reachedEnd = new ArrayList<>();
        for (Zombie z : aliveZombies) {
            if (z.getXPosition() <= 0) {
                reachedEnd.add(z);
            }
        }
        for (Zombie z : reachedEnd) {
            int row = z.getRow();
            if (board.triggerLawnMower(row)) {
                // ماشین چمن‌زنی همه زامبی‌های همان ردیف را می‌کشد
                List<Zombie> rowZombies = new ArrayList<>();
                for (Zombie other : aliveZombies) {
                    if (other.getRow() == row) {
                        rowZombies.add(other);
                    }
                }
                aliveZombies.removeAll(rowZombies);
                System.out.println("The lawn mower in the row " + row + " is triggered and killed these zombies.");
            } else {
                System.out.println("The zombie ate your brain; LOSER!!!");
                gameOver = true;
                won = false;
                return;
            }
        }
    }

    // =========================================================================
    // === بخش فصلی: متدهای کمکی برای اعمال ویژگی‌های فصل‌ها (در انتهای کلاس) ===
    // =========================================================================

    private void applyTideLevel() {
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                boolean isWater = (c >= waterStartColumn);

                // توجه: متد setWater را باید در کلاس Tile بسازید
                // tile.setWater(isWater);

                Plant p = tile.getPlant();
                // فرض بر این است که متد hasTag و تگ AQUATIC در کلاس Plant شما پیاده‌سازی شده باشد
                // اگر نشده است، می‌توانید فعلاً فقط از بررسی نام lilypad استفاده کنید
                if (isWater && p != null && !p.getName().toLowerCase().equals("lilypad")) {
                    p.takeDamage(9999);
                    System.out.println(p.getName() + " drowned!");
                }
            }
        }
    }

    private void applyIceWind() {
        int row1 = random.nextInt(Board.ROWS);
        int row2 = random.nextInt(Board.ROWS);

        for (int c = 0; c < Board.COLS; c++) {
            Plant p1 = board.getTile(row1, c).getPlant();
            // توجه: متد applyFreezeWind را باید در کلاس Plant بسازید
            // if (p1 != null) p1.applyFreezeWind();

            Plant p2 = board.getTile(row2, c).getPlant();
            // if (p2 != null) p2.applyFreezeWind();
        }
    }

    private void spawnRandomGraves(int count) {
        for (int i = 0; i < count; i++) {
            int r = random.nextInt(Board.ROWS);
            int c = random.nextInt(Board.COLS);
            Tile t = board.getTile(r, c);

            // توجه: باید کلاس Grave را بسازید و متدهای hasGrave و setGrave را به Tile اضافه کنید
            /*
            if (t.isEmpty() && !t.hasGrave()) {
                t.setGrave(new Grave(random.nextBoolean(), false));
                System.out.println("A new grave spawned at (" + c + ", " + r + ")");
            }
            */
        }
    }
}