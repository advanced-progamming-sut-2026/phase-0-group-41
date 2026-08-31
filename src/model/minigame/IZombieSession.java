package model.minigame;

import model.game.Board;
import model.plant.PlantFactory;
import model.user.User;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IZombieSession extends MiniGameSession {

    public enum PlaceZombieResult {
        SUCCESS, INVALID_LOCATION, BEYOND_RED_LINE, NOT_ENOUGH_SUN, INVALID_ZOMBIE
    }

    private static final int RED_LINE_COL = 5; // زامبی‌ها فقط در ستون ۵ به بعد (سمت راست) کاشته می‌شوند

    private final boolean[] brainsEaten = new boolean[Board.ROWS];
    private boolean isGameWon = false;
    private boolean isGameLost = false;
    private final int cheapestZombieCost;

    // لیستی برای گزارش رخدادها به کنترلر هنگام رد شدن زمان
    private final List<String> recentEvents = new ArrayList<>();

    public IZombieSession(User user) {
        this(user, 1);
    }

    public IZombieSession(User user, int level) {
        super(user, 1, level);

        int startingSun;
        int plantDensityPercent; // چقدر از خانه‌های سمت چپ گیاه دارند (سختی بیشتر = گیاهان مدافع بیشتر)
        switch (getLevel()) {
            case 2:
                startingSun = 130;
                plantDensityPercent = 70;
                cheapestZombieCost = 50;
                break;
            case 3:
                startingSun = 110;
                plantDensityPercent = 90;
                cheapestZombieCost = 50;
                break;
            default:
                startingSun = 150;
                plantDensityPercent = 50;
                cheapestZombieCost = 50;
                break;
        }
        getSunManager().addSun(startingSun);
        setupCardboardPlants(plantDensityPercent);
        setupSunProducerZombies();
    }

    private void setupCardboardPlants(int densityPercent) {
        Random rand = new Random();
        // چیدن تصادفی گیاهان در نیمه چپ زمین
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 1; c < RED_LINE_COL; c++) {
                int chance = rand.nextInt(100);
                if (chance >= densityPercent) {
                    continue; // این خانه خالی می‌ماند
                }
                int subChance = rand.nextInt(100);
                if (subChance < 30) {
                    // آفتابگردان‌ها منبع اصلی درآمد شما هستند (با خورده شدنشان خورشید می‌گیرید)
                    getBoard().getTile(r, c).setPlant(PlantFactory.create("sunflower"));
                } else if (subChance < 60) {
                    getBoard().getTile(r, c).setPlant(PlantFactory.create("peashooter"));
                } else if (subChance < 80) {
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

        try {
            int dl = getUser().getDifficultyLevel();
            Zombie zombie = ZombieFactory.create(type, dl);
            if (getSunManager().spendSun(zombie.getWaveCost())) {
                zombie.spawn(row, col);
                getAliveZombies().add(zombie);
                return PlaceZombieResult.SUCCESS;
            } else {
                return PlaceZombieResult.NOT_ENOUGH_SUN;
            }
        } catch (IllegalArgumentException e) {
            return PlaceZombieResult.INVALID_ZOMBIE;
        }
    }

    @Override
    protected void customMiniGameTick() {
        if (isGameOver() || isGameWon || isGameLost) return;

        getFallingSuns().clear(); // بدون بارش خورشید از آسمان

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

        // ۳. بررسی شرط باخت (زامبی زنده‌ای نمانده و خورشید هم کافی نیست)
        if (getAliveZombies().isEmpty() && getSunManager().getCurrentSun() < cheapestZombieCost) {
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