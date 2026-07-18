package model.zombie.zombies;

import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class AllStarZombie extends Zombie {

    private boolean isCharging = true;
    private static final double CHARGE_SPEED = 0.05;
    private static final double WALK_SPEED = 0.01;

    public AllStarZombie() {
        super("allstar", 600, CHARGE_SPEED, 250, 10);
    }

    @Override
    public double getSpeed() {
        return isCharging ? CHARGE_SPEED : WALK_SPEED;
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0)); // جلوگیری از ارور بیرون زدن از آرایه
        Plant target = (tile != null) ? tile.getPlant() : null;

        // چک می‌کنیم گیاه وجود داشته باشه و نمرده باشه
        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {
            handleCollision(target, tile);
        } else {
            // حرکت به سمت چپ
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }

    // متد رو کمی تغییر دادیم تا tile رو هم بگیره و بتونه گیاه رو پاک کنه
    private void handleCollision(Plant target, Tile tile) {
        if (isCharging) {
            target.takeDamage(9999); // نابودی درجا
            isCharging = false;      // ترمز می‌کنه!
            setEating(false);        // چون تو حرکت له کرده، نیازی به انیمیشن خوردن نیست
        } else {
            target.takeDamage(getDamagePerTick()); // گاز گرفتن عادی
            setEating(true);
        }

        // 🌟 بخش حیاتی: اگر گیاه مرد، از رو زمین برش دار تا راه باز بشه
        if (target.isDead()) {
            tile.setPlant(null);
            setEating(false);
        }
    }
}