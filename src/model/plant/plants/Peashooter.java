package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.zombie.Zombie;

/**
 * پیاده‌سازی ساده‌شده (برای فاز اسکلت): به جای شبیه‌سازی حرکت فیزیکی تیر،
 * هر SHOOT_INTERVAL_TICKS در صورت وجود زامبی هم‌ردیف، مستقیماً به نزدیک‌ترین آن آسیب می‌زند.
 */
public class Peashooter extends Plant {

    private static final int SHOOT_INTERVAL_TICKS = 14; // تقریبا هر ۱.۴ ثانیه
    private static final int DAMAGE = 20;
    private int ticksSinceLastShot = 0;

    public Peashooter() {
        super("peashooter", PlantType.SHOOTER, 100, 75, 300);
    }

    @Override
    public void onTick(GameSession session) {
        ticksSinceLastShot++;
        int interval = isFed() && isFeedActive() ? 1 : SHOOT_INTERVAL_TICKS;
        if (ticksSinceLastShot < interval) {
            return;
        }
        Zombie target = findNearestZombieInRow(session);
        if (target != null) {
            target.takeDamage(DAMAGE);
            ticksSinceLastShot = 0;
        }
        decayFeedEffect();
    }

    private Zombie findNearestZombieInRow(GameSession session) {
        Zombie nearest = null;
        double minX = Double.MAX_VALUE;
        for (Zombie z : session.getAliveZombies()) {
            if (z.getRow() == getRow() && z.getXPosition() >= getCol() && z.getXPosition() < minX) {
                minX = z.getXPosition();
                nearest = z;
            }
        }
        return nearest;
    }
}
