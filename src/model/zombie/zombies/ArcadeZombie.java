package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class ArcadeZombie extends Zombie {

    // جان دستگاه آرکید (برابر با زامبی سرسطلی یعنی 1100 زره + 200 پایه = 1300)
    private int machineHealth = 1300;
    private boolean hasMachine = true;

    public ArcadeZombie() {
        // نام (arcade)، جان خود زامبی (1300)، سرعت (0.01)، هزینه موج (300)، قدرت ضربه در حالت بدون دستگاه (10)
        super("arcade", 1300, 0.01, 300, 10);
    }

    /**
     * تغییر منطق دمیج خوردن: تا زمانی که دستگاه سالم است، ضربات به دستگاه می‌خورد.
     */
    @Override
    public void takeDamage(int amount) {
        if (hasMachine && machineHealth > 0) {
            machineHealth -= amount; // اول از جان دستگاه کم کن

            if (machineHealth <= 0) {
                // اگر دستگاه خراب شد و دمیج اضافه‌ای ماند، آن را به خود زامبی منتقل کن
                super.takeDamage(-machineHealth);
                machineHealth = 0;
                hasMachine = false; // دستگاه رسماً از بین رفت
            }
        } else {
            // اگر دستگاهی در کار نبود، خود زامبی دمیج می‌خورد
            super.takeDamage(amount);
        }
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // --- بخش ۱: پیدا کردن هدف (تارگت) ---
        Board board = session.getBoard();
        int col = (int) Math.floor(getXPosition());
        Tile tile = board.getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // --- بخش ۲: منطق برخورد با گیاه ---
        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {

            setEating(true);

            if (hasMachine) {
                // طبق داک: اگر حین هل دادن به گیاهی برخورد کند، درجا آن را می‌کشد (دمیج 9999)
                target.takeDamage(9999);
            } else {
                // اگر دستگاه خراب شده باشد، مثل یک زامبی عادی گیاه را گاز می‌گیرد
                target.takeDamage(getDamagePerTick());
            }

            // اگر گیاه از بین رفت، آن را از روی زمین پاک می‌کنیم
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }

        } else {
            // --- بخش ۳: حرکت کردن ---
            // اگر گیاهی جلویش نبود، به سمت چپ حرکت می‌کند
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }

    // متدهای کمکی برای View (تا بعداً بتوانید وضعیت دستگاه را در کنسول یا گرافیک چاپ کنید)
    public boolean isHasMachine() {
        return hasMachine;
    }

    public int getMachineHealth() {
        return machineHealth;
    }

    @Override
    public int getHealth() {
        // برای نوار سلامتی کل، مجموع جان زامبی و دستگاه را برمی‌گردانیم
        return super.getHealth() + machineHealth;
    }
}