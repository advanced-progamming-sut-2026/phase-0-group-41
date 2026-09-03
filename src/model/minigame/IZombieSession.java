package model.minigame;

import model.game.Board;
import model.plant.PlantFactory;
import model.sun.SunManager;
import model.user.User;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IZombieSession extends MiniGameSession {

    public enum PlaceZombieResult {
        SUCCESS, INVALID_LOCATION, BEYOND_RED_LINE, NOT_ENOUGH_SUN, INVALID_ZOMBIE, ON_COOLDOWN
    }

    private static final int RED_LINE_COL = 5; // زامبی‌ها فقط در ستون ۵ به بعد (سمت راست) کاشته می‌شوند
    private static final int COOLDOWN_TICKS = 100; // ۱۰ ثانیه‌ی آماده‌سازی مجدد برای هر نوع زامبی

    /** شماره‌ی ستونی که خط قرمز از سمت چپ آن شروع می‌شود (برای رسم خط قرمز
     *  در صفحه‌های couch/آنلاین I, Zombie؛ قبلاً این مقدار private بود و هیچ
     *  صفحه‌ای نمی‌توانست آن را برای رسم خط بخواند). */
    public int getRedLineCol() {
        return RED_LINE_COL;
    }

    private final boolean[] brainsEaten = new boolean[Board.ROWS];
    private boolean isGameWon = false;
    private boolean isGameLost = false;
    private final int cheapestZombieCost;
    // نام نوع زامبی -> تیک باقی‌مانده تا آماده‌سازی مجدد (مطابق الگوی plantCooldowns در GameSession)
    private final Map<String, Integer> zombieCooldowns = new HashMap<>();

    // === رفع باگ: بودجه‌ی مشترک ===
    // قبلاً هم آفتابگردان‌های طرف گیاه و هم زامبی‌های خورشیدزای طرف زامبی هر دو
    // مستقیماً به همان SunManager مشترک (ارث‌بری‌شده از GameSession) اضافه
    // می‌شدند و طرف زامبی هم دقیقاً از همان استخر خورشید برای خرید زامبی خرج
    // می‌کرد. یعنی هیچ بودجه‌ی جداگانه‌ای بین دو طرف وجود نداشت و تشخیص
    // اینکه هرکدام واقعاً چقدر «بودجه»ی خودشان را دارند ممکن نبود. اینجا یک
    // استخر خورشید کاملاً جدا و مستقل فقط برای طرف زامبی ساخته می‌شود؛
    // SunManager قدیمی (ارث‌بری‌شده) از این پس فقط برای طرف گیاه استفاده
    // می‌شود (خورشید حاصل از آفتابگردان‌ها و هزینه‌ی کاشت گیاهان).
    private final SunManager zombieSunManager;

    // لیستی برای گزارش رخدادها به کنترلر هنگام رد شدن زمان
    private final List<String> recentEvents = new ArrayList<>();

    public IZombieSession(User user) {
        this(user, 1);
    }

    public IZombieSession(User user, int level) {
        super(user, 1, level);

        int startingSun;
        int startingZombieSun;
        int plantDensityPercent; // چقدر از خانه‌های سمت چپ گیاه دارند (سختی بیشتر = گیاهان مدافع بیشتر)
        switch (getLevel()) {
            case 2:
                startingSun = 130;
                startingZombieSun = 100;
                plantDensityPercent = 70;
                cheapestZombieCost = 50;
                break;
            case 3:
                startingSun = 110;
                startingZombieSun = 100;
                plantDensityPercent = 90;
                cheapestZombieCost = 50;
                break;
            default:
                startingSun = 150;
                startingZombieSun = 150;
                plantDensityPercent = 50;
                cheapestZombieCost = 50;
                break;
        }
        // خورشید طرف گیاه (برای کاشت گیاهان جدید و رشد از آفتابگردان‌ها)
        getSunManager().addSun(startingSun);
        // بودجه‌ی جداگانه‌ی طرف زامبی؛ همان درجه سختی کاربر روی سرعت تولید
        // زامبی‌های خورشیدزا تاثیر ندارد، فقط روی مقدار جان/دمیج زامبی‌ها.
        this.zombieSunManager = new SunManager(Math.max(1, getUser() == null ? 1 : getUser().getDifficultyLevel()));
        this.zombieSunManager.setCurrentSun(startingZombieSun);
        setupCardboardPlants(plantDensityPercent);
        setupSunProducerZombies();
    }

    /** استخر خورشید مستقل طرف زامبی (تفکیک‌شده از خورشید طرف گیاه). */
    public SunManager getZombieSunManager() {
        return zombieSunManager;
    }

    private void setupCardboardPlants(int densityPercent) {
        Random rand = new Random();
        // === رفع باگ بالانس: تولید خورشید طرف گیاه در برابر طرف زامبی ===
        // طرف زامبی از ۵ زامبیِ خورشیدزای ثابت (یکی در هر ردیف) بهره می‌برد که
        // خودکار و بدون نیاز به کلیک خورشید تولید می‌کنند و با گذشت زمان
        // سریع‌تر هم می‌شوند (هر ۳۰ ثانیه، تا سقف هر ۴ ثانیه). طرف گیاه قبلاً
        // فقط با احتمال ۳۰٪ از میان گیاهانِ به‌طور تصادفی کاشته‌شده صاحب
        // آفتابگردان می‌شد که در سطح ۱ (تراکم ۵۰٪) به‌طور میانگین فقط ۲ تا ۳
        // آفتابگردان در کل زمین می‌شد؛ یعنی نرخ تولید خورشید طرف گیاه (حدود
        // ۶ خورشید در ثانیه در بهترین حالت) در برابر طرف زامبی (حدود ۳۱ خورشید
        // در ثانیه در حالت پایدار) شدیداً کم بود. اینجا سهم آفتابگردان از ۳۰٪
        // به ۵۰٪ افزایش می‌یابد تا میانگین تعداد آفتابگردان‌ها تقریباً دو برابر
        // شود و فاصله‌ی این دو نرخ منطقی‌تر شود؛ عناصر دفاعی (peashooter/squash)
        // هم برای حفظ آرایش تقریبی قبلی کمی کم می‌شوند.
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 1; c < RED_LINE_COL; c++) {
                int chance = rand.nextInt(100);
                if (chance >= densityPercent) {
                    continue; // این خانه خالی می‌ماند
                }
                int subChance = rand.nextInt(100);
                if (subChance < 50) {
                    // آفتابگردان‌ها منبع اصلی درآمد شما هستند (با کلیک روی آن‌ها خورشید جمع می‌کنید)
                    getBoard().getTile(r, c).setPlant(PlantFactory.create("sunflower"));
                } else if (subChance < 75) {
                    getBoard().getTile(r, c).setPlant(PlantFactory.create("peashooter"));
                } else if (subChance < 90) {
                    getBoard().getTile(r, c).setPlant(PlantFactory.create("squash"));
                }
            }
        }
    }

    public PlaceZombieResult placeZombie(String type, int row, int col) {
        if (row < 0 || row >= Board.ROWS || col < 0 || col >= Board.COLS) {
            return PlaceZombieResult.INVALID_LOCATION;
        }
        if (col < RED_LINE_COL) {
            return PlaceZombieResult.BEYOND_RED_LINE; // عبور از خط قرمز
        }
        if (isZombieOnCooldown(type)) {
            return PlaceZombieResult.ON_COOLDOWN;
        }

        try {
            int dl = getUser().getDifficultyLevel();
            Zombie zombie = ZombieFactory.create(type, dl);
            if (zombieSunManager.spendSun(zombie.getWaveCost())) {
                zombie.spawn(row, col);
                getAliveZombies().add(zombie);
                zombieCooldowns.put(type.toLowerCase(), COOLDOWN_TICKS);
                return PlaceZombieResult.SUCCESS;
            } else {
                return PlaceZombieResult.NOT_ENOUGH_SUN;
            }
        } catch (IllegalArgumentException e) {
            return PlaceZombieResult.INVALID_ZOMBIE;
        }
    }

    /** هزینه‌ی خورشیدِ گذاشتن این نوع زامبی؛ برای نمایش قیمت روی کارت در گرافیک/کنسول. */
    public int getZombieCost(String type) {
        try {
            Zombie probe = ZombieFactory.create(type, getUser().getDifficultyLevel());
            return probe.getWaveCost();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public boolean isZombieOnCooldown(String type) {
        return zombieCooldowns.getOrDefault(type.toLowerCase(), 0) > 0;
    }

    /** تیک‌های باقی‌مانده تا آماده‌سازی مجدد این نوع زامبی؛ برای نمایش تایمر روی کارت. */
    public int getZombieCooldownRemaining(String type) {
        return zombieCooldowns.getOrDefault(type.toLowerCase(), 0);
    }

    @Override
    protected void customMiniGameTick() {
        if (isGameOver() || isGameWon || isGameLost) return;

        getFallingSuns().clear(); // بدون بارش خورشید از آسمان

        // کاهش تیک‌های باقی‌مانده‌ی cooldown هر نوع زامبی (مطابق الگوی plantCooldowns)
        for (String key : new ArrayList<>(zombieCooldowns.keySet())) {
            int remaining = zombieCooldowns.get(key) - 1;
            if (remaining <= 0) {
                zombieCooldowns.remove(key);
            } else {
                zombieCooldowns.put(key, remaining);
            }
        }

        // ۱. بررسی رسیدن زامبی‌ها به مغز (ستون 0 یا کمتر)
        List<Zombie> reachedEnd = new ArrayList<>();
        for (Zombie z : getAliveZombies()) {
            if (z.getXPosition() <= 0 && !brainsEaten[z.getRow()]) {
                brainsEaten[z.getRow()] = true;
                recentEvents.add("زامبی شما مغز ردیف " + (z.getRow() + 1) + " را خورد!");
                reachedEnd.add(z);
            }
        }
        getAliveZombies().removeAll(reachedEnd); // زامبی بعد از خوردن مغز ناپدید می‌شود

        // ۲. بررسی شرط پیروزی (تمامی ۵ مغز خورده شده باشند)
        boolean allEaten = true;
        for (boolean b : brainsEaten) {
            if (!b) {
                allEaten = false;
                break;
            }
        }
        if (allEaten) {
            isGameWon = true;
            recentEvents.add("شما تمام مغزهای باغچه را خوردید! برنده شدید!");
            endGame(true);
            return;
        }

        // ۳. بررسی شرط باخت (زامبی زنده‌ای نمانده و خورشید طرف زامبی هم کافی نیست)
        if (getAliveZombies().isEmpty() && zombieSunManager.getCurrentSun() < cheapestZombieCost) {
            isGameLost = true;
            recentEvents.add("شما خورشید کافی برای تولید زامبی جدید ندارید و تمام زامبی‌هایتان از بین رفتند. باختید!");
            endGame(false);
        }
    }

    public List<String> pollRecentEvents() {
        List<String> copy = new ArrayList<>(recentEvents);
        recentEvents.clear();
        return copy;
    }

    private void setupSunProducerZombies() {
        // در ابتدای مرحله در هر ردیف یکی از این زامبی‌ها وجود دارد
        for (int r = 0; r < Board.ROWS; r++) {
            model.zombie.zombies.IZombieSunProducer sunZombie = new model.zombie.zombies.IZombieSunProducer();
            
            // قرار دادن زامبی در انتهایی‌ترین ستون ممکن (ستون ۸)
            sunZombie.spawn(r, Board.COLS - 1);
            // اضافه کردن به لیست زامبی‌های زنده در GameSession
            getAliveZombies().add(sunZombie);
        }
    }
}