package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;

public class BarrelRollerZombie extends Zombie {

    private int barrelHealth = 400; // جان دبه
    private boolean barrelBroken = false;

    // ✨ ترفند کلین‌کد: یک پرچم می‌ذاریم تا به onTick خبر بدیم وقت ساختن ایمپه
    private boolean pendingImpSpawn = false;

    public BarrelRollerZombie() {
        super("barrelroller", 250, 0.015, 200, 10);
    }

    // متد دمیج خوردن رو دقیقاً مثل کلاس پدر می‌نویسیم (بدون نیاز به پاس دادن GameSession)
    @Override
    public void takeDamage(int amount) {
        if (!barrelBroken && barrelHealth > 0) {
            barrelHealth -= amount;
            if (barrelHealth <= 0) {
                barrelBroken = true;
                pendingImpSpawn = true; // پرچم رو می‌بریم بالا که ایمپ‌ها تو تیکِ بعدی ساخته بشن

                // انتقال دمیج اضافه به زامبی اصلی
                super.takeDamage(-barrelHealth);
                barrelHealth = 0;
            }
        } else {
            // اگر دبه شکسته بود، دمیج به خود زامبی می‌خوره
            super.takeDamage(amount);
        }
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // --- بخش ۱: بررسی اینکه آیا دبه تازه شکسته و باید ایمپ بسازیم؟ ---
        if (pendingImpSpawn) {
            spawnImps(session);
            pendingImpSpawn = false; // پرچم رو میاریم پایین تا فقط یک بار ایمپ بسازه
        }

        // --- بخش ۲: پیدا کردن هدف (گیاه جلویی) ---
        Board board = session.getBoard();
        int col = (int) Math.floor(getXPosition());
        Tile tile = board.getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // --- بخش ۳: منطق برخورد با گیاه ---
        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {

            setEating(true);
            target.takeDamage(getDamagePerTick()); // گاز گرفتن گیاه

            // اگر گیاه بعد از این گاز گرفتن مرد، برش می‌داریم تا راه باز بشه
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }

        } else {
            // --- بخش ۴: حرکت عادی ---
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }

    // متد ساخت ایمپ‌ها
    private void spawnImps(GameSession session) {
        int dl = session.getUser().getDifficultyLevel();
        Zombie imp1 = ZombieFactory.create("imp", dl);
        Zombie imp2 = ZombieFactory.create("imp", dl);

        // ایمپ‌ها رو یکم جلوتر و یکم عقب‌تر از خود زامبی دبه‌ای میندازیم
        imp1.spawn(getRow(), getXPosition() - 0.2);
        imp2.spawn(getRow(), getXPosition() + 0.2);

        // اضافه کردن به لیست زامبی‌های زنده در همون لحظه
        session.getAliveZombies().add(imp1);
        session.getAliveZombies().add(imp2);
    }

    // --- متدهای کمکی برای بخش View ---
    public boolean isBarrelBroken() {
        return barrelBroken;
    }


    public int getBarrelHealth() {
        return barrelHealth;
    }

    @Override
    public int getBaseHealth() {
        return super.getHealth();
    }

    @Override
    public int getHealth() {
        // وقتی از این زامبی می‌پرسن کل جونت چقدره، جون خودش رو با دبه جمع می‌کنه و میگه
        return super.getHealth() + barrelHealth;
    }
}