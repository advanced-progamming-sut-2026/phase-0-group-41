package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter; // فرض بر وجود این اینترفیس

public class Peashooter extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15; // 1.5 ثانیه
    private int tickCounter = 0;
    private int currentSunCost = 100;
    private int level = 1;

    public Peashooter() {
        super("peashooter", PlantType.SHOOTER, 100, 5, 300, PlantTag.DAY);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        // منطق Plant Food (شلیک رگباری سریع)
        if (isFeedActive()) {
            shoot(session); // در حالت فید، هر تیک شلیک می‌کند
            decayFeedEffect(); // کاهش زمان باقیمانده‌ی فید
            return;
        }

        // حالت عادی
        tickCounter++;
        if (tickCounter >= shootInterval) {
            shoot(session);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        // اینجا باید متد اسپاون پرتابه موتور بازی خود را صدا بزنید
        // مثال: session.spawnProjectile(new Pea(getRow(), getCol(), damage));
        System.out.println(getName() + " یک نخود با دمیج " + damage + " شلیک کرد.");
    }

    @Override
    public void feed(GameSession session) {
        super.feed(session);
        System.out.println("Plant Food فعال شد: شلیک رگباری Peashooter!");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25; // هزینه 75
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }
}