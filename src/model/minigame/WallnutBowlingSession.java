package model.minigame;

import model.game.Board;
import model.user.User;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WallnutBowlingSession extends MiniGameSession {

    public enum NutType { NORMAL, EXPLOSIVE, GIANT }

    // کلاس داخلی برای مدیریت گردوهای در حال حرکت (پرتابه‌ها)
    public static class RollingNut {
        public NutType type;
        public int row;
        public double x;
        public int rowDir; // 0 = مستقیم، 1 = پایین، -1 = بالا
        public double speed = 0.1; // سرعت غلتیدن

        public RollingNut(NutType type, int row, double x) {
            this.type = type;
            this.row = row;
            this.x = x;
            this.rowDir = 0; // ابتدا مستقیم می‌رود
        }
    }

    public enum PlantNutResult {
        SUCCESS, BEYOND_RED_LINE, NOT_IN_CONVEYOR, INVALID_LOCATION
    }

    private static final int RED_LINE_COL = 2; // ستون خط قرمز
    private static final int MAX_CONVEYOR_SIZE = 5; // ظرفیت نوار نقاله

    private final LinkedList<NutType> conveyorBelt = new LinkedList<>();
    private final List<RollingNut> activeNuts = new ArrayList<>();
    
    // لیستی برای گزارش رخدادها به کنترلر هنگام رد شدن زمان (advance time)
    private final List<String> recentEvents = new ArrayList<>();
    
    private int tickCounter = 0;

    // تعداد کل زامبی‌هایی که در این مرحله باید ظاهر شوند/کشته شوند تا برد رخ دهد
    private final int totalZombiesToDefeat;
    private int zombiesSpawned = 0;
    private int zombiesDefeated = 0;
    private final int conveyorRefillTicks; // هرچه سطح بالاتر، نوار سریع‌تر پر می‌شود
    private final double zombieSpawnIntervalTicks; // فاصله‌ی اسپاون زامبی جدید
    private double spawnAccumulator = 0;
    private final java.util.Random spawnRandom = new java.util.Random();

    public WallnutBowlingSession(User user) {
        this(user, 1);
    }

    public WallnutBowlingSession(User user, int level) {
        super(user, 1, level);

        switch (getLevel()) {
            case 2:
                totalZombiesToDefeat = 8;
                conveyorRefillTicks = 40;
                zombieSpawnIntervalTicks = 70;
                break;
            case 3:
                totalZombiesToDefeat = 12;
                conveyorRefillTicks = 30;
                zombieSpawnIntervalTicks = 50;
                break;
            default:
                totalZombiesToDefeat = 5;
                conveyorRefillTicks = 50;
                zombieSpawnIntervalTicks = 100;
                break;
        }

        // شروع بازی با چند گردوی اولیه در نوار نقاله
        addRandomNutToConveyor();
        addRandomNutToConveyor();
    }

    public List<NutType> getConveyorBelt() {
        return conveyorBelt;
    }

    /** لیست گردوهای در حال غلتیدن روی زمین؛ برای رسم گرافیکی استفاده می‌شود. */
    public List<RollingNut> getActiveNuts() {
        return activeNuts;
    }

    // متد کاشت گردو از روی نوار نقاله
    public PlantNutResult plantNut(String typeStr, int row, int col) {
        if (row < 0 || row >= Board.ROWS || col < 0 || col >= Board.COLS) {
            return PlantNutResult.INVALID_LOCATION;
        }
        if (col > RED_LINE_COL) {
            return PlantNutResult.BEYOND_RED_LINE; // عبور از خط قرمز
        }

        NutType requestedType = parseNutType(typeStr);
        if (requestedType == null || !conveyorBelt.contains(requestedType)) {
            return PlantNutResult.NOT_IN_CONVEYOR; // گردو در نوار نقاله نیست
        }

        // حذف گردو از نوار نقاله و اضافه کردن به لیست در حال حرکت
        conveyorBelt.remove(requestedType);
        activeNuts.add(new RollingNut(requestedType, row, col));
        return PlantNutResult.SUCCESS;
    }

    @Override
    protected void customMiniGameTick() {
        if (isGameOver()) {
            return;
        }
        getFallingSuns().clear(); // در این مینی‌گیم خورشید از آسمان نمی‌افتد
        tickCounter++;

        // نوار نقاله بر اساس سختی سطح پر می‌شود
        if (tickCounter % conveyorRefillTicks == 0 && conveyorBelt.size() < MAX_CONVEYOR_SIZE) {
            addRandomNutToConveyor();
        }

        // اسپاون تدریجی زامبی‌ها تا رسیدن به هدف کل مرحله
        spawnAccumulator++;
        if (zombiesSpawned < totalZombiesToDefeat && spawnAccumulator >= zombieSpawnIntervalTicks) {
            spawnAccumulator = 0;
            spawnZombieForBowling();
        }

        // حرکت و برخورد گردوها
        processRollingNuts();

        // شمارش زامبی‌هایی که همین تیک کشته شدند (از بین رفتن سلامتی صفر)
        List<Zombie> justDied = new ArrayList<>();
        for (Zombie z : getAliveZombies()) {
            if (z.isDead()) {
                justDied.add(z);
            }
        }
        if (!justDied.isEmpty()) {
            zombiesDefeated += justDied.size();
            getAliveZombies().removeAll(justDied);
        }

        // شرط برد: تمام زامبی‌های مرحله ظاهر شده و کشته شده باشند
        if (zombiesSpawned >= totalZombiesToDefeat && getAliveZombies().isEmpty()
                && zombiesDefeated >= totalZombiesToDefeat) {
            recentEvents.add("تمام زامبی‌ها با گردوها از بین رفتند! برنده شدید!");
            endGame(true);
            return;
        }

        // شرط باخت: یک زامبی به انتهای چپ زمین (خانه‌ی خانه‌ی بازیکن) برسد
        for (Zombie z : getAliveZombies()) {
            if (z.getXPosition() <= 0) {
                recentEvents.add("یک زامبی به خانه رسید! باختید.");
                endGame(false);
                return;
            }
        }
    }

    private void spawnZombieForBowling() {
        int dl = getUser().getDifficultyLevel();
        Zombie z = model.zombie.ZombieFactory.create("normal", dl);
        int row = spawnRandom.nextInt(Board.ROWS);
        z.spawn(row, Board.COLS - 1);
        z.setSpawnTick((int) getTickCount());
        getAliveZombies().add(z);
        zombiesSpawned++;
    }

    private void processRollingNuts() {
        Iterator<RollingNut> it = activeNuts.iterator();
        while (it.hasNext()) {
            RollingNut nut = it.next();
            
            // حرکت گردو در محور ایکس و وای (اریب)
            nut.x += nut.speed;
            
            // اگر گردو در حال حرکت اریب (۴۵ درجه) است، ردیف آن را به تدریج عوض می‌کنیم
            if (nut.rowDir != 0) {
                // برای سادگی فیزیک: هر بار که از مرز یک ستون عبور کرد، ردیفش عوض می‌شود
                if (Math.abs(nut.x - Math.floor(nut.x)) < 0.1) {
                    nut.row += nut.rowDir;
                    
                    // بررسی کمانه کردن از بالا یا پایین صفحه
                    if (nut.row <= 0) {
                        nut.row = 0;
                        nut.rowDir = 1; // کمانه به پایین
                    } else if (nut.row >= Board.ROWS - 1) {
                        nut.row = Board.ROWS - 1;
                        nut.rowDir = -1; // کمانه به بالا
                    }
                }
            }

            // حذف گردو در صورت خروج از صفحه
            if (nut.x > Board.COLS) {
                it.remove();
                continue;
            }

            // بررسی برخورد با زامبی‌ها
            Zombie hitZombie = getHitZombie(nut);
            if (hitZombie != null) {
                if (nut.type == NutType.GIANT) {
                    hitZombie.takeDamage(9999);
                    recentEvents.add("گردوی بزرگ زامبی را در (" + (int)nut.x + "," + nut.row + ") کاملاً له کرد!");
                    // گردوی بزرگ متوقف نمی‌شود و به راهش ادامه می‌دهد
                    
                } else if (nut.type == NutType.EXPLOSIVE) {
                    explodeNut(nut);
                    it.remove(); // گردوی انفجاری بعد از انفجار نابود می‌شود
                    continue;
                    
                } else if (nut.type == NutType.NORMAL) {
                    hitZombie.takeDamage(100); // دمیج استاندارد برخورد
                    recentEvents.add("گردوی بولینگ به زامبی در (" + (int)nut.x + "," + nut.row + ") برخورد کرد و کمانه کرد!");
                    
                    // تغییر مسیر پس از برخورد (کمانه کردن به بالا یا پایین)
                    if (nut.row == 0) {
                        nut.rowDir = 1;
                    } else if (nut.row == Board.ROWS - 1) {
                        nut.rowDir = -1;
                    } else {
                        nut.rowDir = Math.random() < 0.5 ? 1 : -1;
                    }
                }
            }
        }
    }

    private void explodeNut(RollingNut nut) {
        int r = nut.row;
        int c = (int) Math.floor(nut.x);
        recentEvents.add("گردوی انفجاری در (" + c + "," + r + ") منفجر شد! (شعاع ۳x۳)");
        
        for (Zombie z : getAliveZombies()) {
            if (Math.abs(z.getRow() - r) <= 1 && Math.abs(z.getXPosition() - c) <= 1.5) {
                z.takeDamage(1800); // دمیج معادل گیلاس
            }
        }
    }

    private Zombie getHitZombie(RollingNut nut) {
        for (Zombie z : getAliveZombies()) {
            if (z.getRow() == nut.row && !z.isDead() && Math.abs(z.getXPosition() - nut.x) < 0.5) {
                return z;
            }
        }
        return null;
    }

    private void addRandomNutToConveyor() {
        double r = Math.random();
        if (r < 0.6) conveyorBelt.add(NutType.NORMAL);
        else if (r < 0.9) conveyorBelt.add(NutType.EXPLOSIVE);
        else conveyorBelt.add(NutType.GIANT);
    }

    private NutType parseNutType(String typeStr) {
        switch (typeStr.toLowerCase()) {
            case "bowlingwallnut": return NutType.NORMAL;
            case "explodeonut": return NutType.EXPLOSIVE;
            case "giantwallnut": return NutType.GIANT;
            default: return null;
        }
    }

    // متد برای کنترلر جهت گرفتن رخدادهای ذخیره شده
    public List<String> pollRecentEvents() {
        List<String> copy = new ArrayList<>(recentEvents);
        recentEvents.clear();
        return copy;
    }
}