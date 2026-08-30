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
        super("peashooter", PlantType.SHOOTER, 100, 50, 300, PlantTag.DAY);
    }

    @Override
    public void onTick(GameSession session) {
        if (isFrozenSolid()) {
            handleIceMelting(session);
            return;
        }
        if (isTransformedToCat() || isOctopused()) return;

        if (isFeedActive()) {
            shoot(session); // در حالت فید، هر تیک شلیک می‌کند
            decayFeedEffect(); // کاهش زمان باقیمانده‌ی فید
            return;
        }

        boolean zombieInLane = false;
        for (model.zombie.Zombie z : session.getAliveZombies()) {
            if (!z.isDead() && z.getRow() == getRow() && z.getXPosition() >= getCol()) {
                zombieInLane = true;
                break;
            }
        }

        if (zombieInLane) {
            tickCounter++;
            if (tickCounter >= shootInterval) {
                shoot(session);
                tickCounter = 0;
            }
        } else {
            tickCounter = 0; // اگر زامبی نیست، دستش روی ماشه نمیره
        }
    }

    @Override
    public void shoot(GameSession session) {
        model.projectile.PeaProjectile pea = new model.projectile.PeaProjectile(getRow(), getCol() + 0.5, damage);
        session.spawnProjectile(pea);
        System.out.println(getName() + " یک نخود با دمیج " + damage + "در مختصات" +getCol() + ","+getRow() + " شلیک کرد.");
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