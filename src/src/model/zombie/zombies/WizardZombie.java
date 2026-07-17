package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WizardZombie extends Zombie {

    private static final int SPELL_COOLDOWN_TICKS = 30; // هر ۳ ثانیه یک بار
    private int ticksSinceLastSpell = 0;

    // لیست گیاهانی که توسط "این جادوگر خاص" طلسم شده‌اند تا در صورت مرگش آزاد شوند
    private final List<Plant> transfiguredPlants = new ArrayList<>();
    private final Random random = new Random();

    public WizardZombie() {
        // نام، جان، سرعت حرکت، دمیج گاز گرفتن (طبق داک صفر است چون نمی‌خورد)
        super("wizard", 300, 0.01, 0, 12);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        ticksSinceLastSpell++;

        // ۱. بررسی کاشی فعلی: اگر به گیاهی رسیده باشد، فوراً آن را طلسم می‌کند
        Tile currentTile = getCurrentTile(session);
        Plant targetInFront = (currentTile != null) ? currentTile.getPlant() : null;

        if (targetInFront != null && !targetInFront.isDead() && !targetInFront.isTransformedToCat()) {
            // به جای خوردن، گیاه جلوی رویش را به گربه تبدیل می‌کند
            transformPlantToCat(targetInFront);
            ticksSinceLastSpell = 0; // ریست کردن تایمر طلسم رندوم
            setEating(false); // انیمیشن گاز گرفتن ندارد
        }

        // ۲. اگر به گیاهی نرسیده باشد، راه می‌رود و هر چند ثانیه یک بار طلسم رندوم می‌فرستد
        else {
            setEating(false);
            setXPosition(getXPosition() - getSpeed());

            if (ticksSinceLastSpell >= SPELL_COOLDOWN_TICKS) {
                castRandomSpell(session);
                ticksSinceLastSpell = 0;
            }
        }
    }

    // 🌟 جادوی رندوم روی یکی از گیاهان کل زمین بازی
    private void castRandomSpell(GameSession session) {
        Board board = session.getBoard();
        List<Plant> eligiblePlants = new ArrayList<>();

        // گشتن تمام زمین برای پیدا کردن گیاهان زنده و طلسم‌نشده
        for (int r = 0; r < 5; r++) { // فرض بر ۵ سطر بودن زمین بازی
            for (int c = 0; c < 9; c++) { // فرض بر ۹ ستون بودن زمین بازی
                Tile tile = board.getTile(r, c);
                if (tile != null) {
                    Plant p = tile.getPlant();
                    if (p != null && !p.isDead() && !p.isTransformedToCat()) {
                        eligiblePlants.add(p);
                    }
                }
            }
        }

        // انتخاب یک گیاه به صورت کاملاً تصادفی و تبدیل آن به گربه
        if (!eligiblePlants.isEmpty()) {
            Plant randomTarget = eligiblePlants.get(random.nextInt(eligiblePlants.size()));
            transformPlantToCat(randomTarget);
        }
    }

    // 🌟 متد تبدیل کردن گیاه به گربه و ثبت در حافظه جادوگر
    private void transformPlantToCat(Plant plant) {
        plant.setTransformedToCat(true);
        transfiguredPlants.add(plant);
    }

    // 🌟 اگر جادوگر آسیب ببیند و بمیرد، تمام گیاهانی که او طلسم کرده بود آزاد می‌شوند
    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        if (isDead()) {
            releaseAllPlants();
        }
    }

    private void releaseAllPlants() {
        for (Plant plant : transfiguredPlants) {
            if (!plant.isDead()) {
                plant.setTransformedToCat(false); // بازگشت به حالت عادی طبق داکیومنت
            }
        }
        transfiguredPlants.clear();
    }

    private Tile getCurrentTile(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        return session.getBoard().getTile(getRow(), Math.max(col, 0));
    }
}