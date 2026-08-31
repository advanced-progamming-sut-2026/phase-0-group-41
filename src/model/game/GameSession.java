package model.game;

import model.plant.Plant;
import model.plant.PlantType;
import model.sun.FallingSun;
import model.sun.SunManager;
import model.user.User;
import model.wave.WaveManager;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;
import model.levelrules.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;

/**
 * یک جلسه‌ی در حال بازی: تخته، خورشیدها، موج‌ها و همه‌ی زامبی/گیاه‌های روی زمین.
 * با «advance time» به جلو برده می‌شود.
 */
public class GameSession {
    private final Season currentSeason;
    private int waterStartColumn = 9;   // === بخش فصلی: برای ساحل موج بزرگ ===

    private final User user;
    private final Board board = new Board();
    private final SunManager sunManager ;
    private final WaveManager waveManager;
    private final List<Zombie> aliveZombies = new ArrayList<>();
    private final List<FallingSun> fallingSuns = new ArrayList<>();
    private final Map<String, Integer> plantCooldowns = new HashMap<>(); // نام گیاه -> تیک باقیمانده
    private final Random random = new Random();
    private final ILevelRules levelRules;
    private final List<model.projectile.Projectile> activeProjectiles = new ArrayList<>();

    private long tickCount = 0;
    private int plantFoodCount = 0;
    private boolean gameOver = false;
    private boolean won = false;
    private double waveHealthAtStart = 0;
    private double waveHealthRemaining = 0;
    private boolean cooldownsDisabled = false; // برای حالت‌های خاص که کول‌داون‌ها غیرفعال می‌شوند

    private List<model.scoreGame.MeowPoint.GameEvent> meowEvents = new ArrayList<>();
    private boolean plantLostInCurrentWave = false; // برای رویداد WAVE_CLEARED_NO_DAMAGE

    public GameSession(User user, int totalWaves) {
        // پاس دادن کار به سازنده‌ی اصلی با فصل دیفالت
        this(user, totalWaves, Season.NORMAL, LevelMode.NORMAL);
    }

    public GameSession(User user, int totalWaves, Season season, LevelMode levelMode) {
        this(user, totalWaves, 50, season, levelMode);
    }

    /**
     * سازنده‌ی کامل که هزینه‌ی موج اول (baseWaveCost) را هم می‌پذیرد. برای اینکه
     * مرحله‌ی دوم یک فصل از مرحله‌ی اول سخت‌تر باشد (طبق سند فاز یک)، مقدار
     * baseWaveCost باید به ازای هر مرحله‌ی بعدی در همان فصل بیشتر داده شود.
     */
    public GameSession(User user, int totalWaves, double baseWaveCost, Season season, LevelMode levelMode) {
        this.user = user;
        this.waveManager = new WaveManager(totalWaves, baseWaveCost);
        int userDifficulty = user.getDifficultyLevel();
        this.sunManager = new SunManager(userDifficulty);
        this.currentSeason = season;

        switch (levelMode) {
            case DEAD_LINE:
                this.levelRules = new DeadLineRules(2);
                break;
            case SAVE_OUR_SEEDS:
                this.levelRules = new SaveOurSeedsRules();
                break;
            case CONVEYOR_BELT:
                this.levelRules = new ConveyorBeltRules();
                break;
            case TIMED_WAR:
                this.levelRules = new TimedWarRules(100);
                break;
            case NIGHT_OPS:
                this.levelRules = new NightOpsRules();
                break;
            case LOVE_YOUR_PLANTS:
                this.levelRules = new LoveYourPlantsRules(15);
                break;
            case PLANT_WHAT_YOU_GET:
                this.levelRules = new PlantWhatYouGetRules();
                break;
            case LOCKED_PLANTS:
                this.levelRules = new LockedPlantsRules(Arrays.asList("peashooter", "sunflower", "wallnut", "potatomine"));
                break;
            case NORMAL:
            default:
                this.levelRules = new NormalLevelRules();
                break;
        }

        this.levelRules.setupLevel(this);
    }

    public void spawnProjectile(model.projectile.Projectile p) {
        activeProjectiles.add(p);
    }

    public List<model.projectile.Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public long getTickCount() {
        return tickCount;
    }

    public void addMeowEvent(model.scoreGame.MeowPoint.GameEvent event) {
        this.meowEvents.add(event);
    }

    public List<model.scoreGame.MeowPoint.GameEvent> getMeowEvents() {
        return meowEvents;
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

    /**
     * راهی امن برای زیرکلاس‌ها (مثل مینی‌گیم‌ها) تا بازی را با نتیجه‌ی برد/باخت
     * مشخص پایان دهند، بدون نیاز به شرط‌های عادی مرحله (رسیدن زامبی به خانه یا
     * تمام شدن موج‌ها). بعد از فراخوانی این متد advanceOneTick دیگر کاری انجام
     * نمی‌دهد چون gameOver بررسی می‌شود.
     */
    protected void endGame(boolean playerWon) {
        if (gameOver) {
            return; // یک بار کافی است
        }
        this.won = playerWon;
        this.gameOver = true;
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
        if(!cooldownsDisabled) {
            plantCooldowns.put(plantName, ticks);
        }
    }

    public void clearAllCooldowns() {
        plantCooldowns.clear();
        cooldownsDisabled = true;
    }

    /** یک تیک بازی را جلو می‌برد (۱۰ تیک = ۱ ثانیه). */
    public void advanceOneTick() {
        if (gameOver) {
            return;
        }
        tickCount++;

        levelRules.applySpecialTickRules(this);

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
                Tile tile = board.getTile(r, c);
                Plant plant = tile.getPlant();
                if (plant != null) {
                    if(!plant.isDead()) {
                        plant.onTick(this);
                    }

                    if (plant.isDead()) {
                        tile.setPlant(null); // کاشی دوباره خالی و قابل کشت می‌شود

                        if(this instanceof model.minigame.BeghouledSession) {
                            tile.setTerrainType(model.game.TerrainType.CRATER); // در Beghouled، کاشی به گودال تبدیل می‌شود                    
                        }
                    }
                }
            }
        }

        // تیک پرتابه‌ها
        List<model.projectile.Projectile> deadProjectiles = new ArrayList<>();
        for (model.projectile.Projectile p : activeProjectiles) {
            p.onTick(this);
            if (p.isDead()) {
                deadProjectiles.add(p);
            }
        }
        activeProjectiles.removeAll(deadProjectiles);

        // حرکت / حمله زامبی‌ها
        List<Zombie> deadZombies = new ArrayList<>();
        for(int i = 0; i< aliveZombies.size(); i++) {
            Zombie zombie = aliveZombies.get(i);
            zombie.onTick(this);

            int currentCol = (int) Math.floor(zombie.getXPosition());
            Tile tileUnderZombie = board.getTile(zombie.getRow(), Math.max(0, currentCol));

            if (tileUnderZombie != null && tileUnderZombie.getTerrainType() == TerrainType.ICE_SLIPPERY) {
                Tile.SliderDirection dir = tileUnderZombie.getSliderDirection();
                // لیز خوردن به بالا
                if (dir == Tile.SliderDirection.UP && zombie.getRow() > 0) {
                    zombie.spawn(zombie.getRow() - 1, zombie.getXPosition()); 
                } 
                // لیز خوردن به پایین
                else if (dir == Tile.SliderDirection.DOWN && zombie.getRow() < Board.ROWS - 1) {
                    zombie.spawn(zombie.getRow() + 1, zombie.getXPosition()); 
                }
            }
            
            if (zombie.isDead()) {
                deadZombies.add(zombie);
            }
        }
        for (Zombie z : deadZombies) {
            aliveZombies.remove(z);
            System.out.println("Zombie of type " + z.getTypeName() + " is dead at (" + (int) z.getXPosition() + ", " + z.getRow() + ")");

            if(z.isCarriesPlantFood()) {
                addPlantFood();
                System.out.println("Zombie dropped a plant food! Total plant food: " + getPlantFoodCount());
            }

            if (user.getQuestManager() != null) {
                // ۱. ثبت کیل عادی برای کوئست‌ها
                user.getQuestManager().recordZombieKill(user.getQuestContext(), 0, null);
            }

            // ۲. محاسبه زمان زنده بودن زامبی بر حسب میلی‌ثانیه
            int ticksLived = (int)this.tickCount - z.getSpawnTick();
            double timeLivedMs = ticksLived * 1000.0; // فرض: هر تیک ۱۰۰۰ میلی‌ثانیه است

            // ۳. ساخت رویداد کشته شدن سریع و ارسال به سشن
            model.scoreGame.MeowPoint.GameEvent fastKillEvent = new model.scoreGame.MeowPoint.GameEvent(
                    model.scoreGame.MeowPoint.EventType.ZOMBIE_KILLED_FAST,
                    1,
                    timeLivedMs
            );
            this.addMeowEvent(fastKillEvent);
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

        if (isWaveSystemEnabled()) {
            checkWaveProgress();
            checkGameOverConditions();
        }
    }

    /**
     * آیا سامانه‌ی موج/چمن‌زن/شرط باخت مرحله‌ی عادی برای این نشست فعال باشد؟
     * مینی‌گیم‌ها (که شرط برد/باخت کاملاً متفاوتی دارند و نباید موج زامبی
     * تصادفی مرحله‌ی عادی روی زمینشان اسپاون شود) این متد را false می‌کنند.
     */
    protected boolean isWaveSystemEnabled() {
        return true;
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

            for (int r = 0; r < Board.ROWS; r++) {
                for (int c = 0; c < Board.COLS; c++) {
                    Tile tile = board.getTile(r, c);
                    
                    // اگر تایل خاصیت نکرومنسی دارد و روی آن قبر وجود دارد
                    if (tile.isNecromancyTile() && tile.hasGrave()) {
                        
                        // تولید یک زامبی پایه (Basic) بر اساس درجه سختی کاربر
                        Zombie necromancyZombie = ZombieFactory.randomBasicZombie(user.getDifficultyLevel());
                        
                        // قرار دادن زامبی دقیقاً در همان مختصات قبر
                        necromancyZombie.spawn(r, c);
                        necromancyZombie.setSpawnTick((int) this.tickCount);
                        
                        // اضافه کردن زامبی به لیست زامبی‌های زنده در صفحه
                        aliveZombies.add(necromancyZombie);
                        
                        System.out.println("نکرومنسی! یک زامبی از زیر قبر در مختصات (" + c + ", " + r + ") بیرون آمد!");
                    }
                }
            }
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
            //داکیومنت گفته ممکن است. پس ما یک احتمال 40 درصدی هم قائل شدیم
            if (isTornadoWave && random.nextDouble() < 0.4) {
                spawnCol -= (1 + random.nextInt(4));
            }

            z.spawn(lane, spawnCol);
            aliveZombies.add(z);
            z.setSpawnTick((int) this.tickCount);
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
                user.addPendingGreenhousePots(1);
                System.out.println("A zombie dropped a pot; you have" + user.getPendingGreenhousePots() + " greenhouse pots now.");
            }
        }
    }

    private void checkGameOverConditions() {
        if (!levelRules.checkCustomLossConditions(this)) {
            System.out.println("شما در این مرحله ویژه باختید؛ LOSER!!!");
            gameOver = true;
            won = false;
            return;
        }

        // اگر زامبی به انتهای ردیف برسد (x <= 0) و ماشین چمن‌زنی فعال نشود، بازی باخته می‌شود
        List<Zombie> reachedEnd = new ArrayList<>();
        for (Zombie z : aliveZombies) {
            if (z.getXPosition() <= 0) {
                reachedEnd.add(z);
            }
        }
        for (Zombie z : reachedEnd) {
            //اگر داخل یه تیک همزمان دوتا زامبی رد شدن، فقط یکی از اونها ماشین چمن‌زنی رو فعال می‌کنه و اون یکی باعث باخت نمیشه  
            if (!aliveZombies.contains(z)) {
                continue;
            }
            
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

    public void triggerFamilyPlantFood(PlantType targetFamily, int durationBonusTicks) {
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                if (tile != null && tile.getPlant() != null) {
                    Plant plant = tile.getPlant();
                    // اگر گیاه زنده بود و نوع (خانواده) آن با نعناع یکی بود
                    if (!plant.isDead() && plant.getType() == targetFamily) {
                        plant.feed(this); // فعال‌سازی افکت پلنت فود
                        
                    }
                }
            }
        }
    }

    private void applyTideLevel() {
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                boolean isWater = (c >= waterStartColumn);
                tile.setWater(isWater);

                Plant p = tile.getPlant();

                if (isWater && p != null && !tile.isHasLilyPad() && !p.getName().toLowerCase().equals("lilypad") && !p.hasTag(model.plant.PlantTag.WATER)) {
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

            if (t.isEmpty() && t.getTerrainType() == TerrainType.NORMAL) {
                boolean hasSun = random.nextBoolean();
                boolean hasFood = !hasSun && random.nextBoolean(); // اگر خورشید نبود، ممکنه غذا باشه

                t.setTerrainType(TerrainType.GRAVE);
                t.setGrave(new Grave(hasSun, hasFood));
                System.out.println("A new grave spawned at (" + c + ", " + r + ")");
            }
        }
    }
}