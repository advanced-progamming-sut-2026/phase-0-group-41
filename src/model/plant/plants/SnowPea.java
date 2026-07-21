package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class SnowPea extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 150;
    private int chillDuration = 5; // زمان پیش‌فرض کندی (ثانیه)
    private int level = 1;

    public SnowPea() {
        super("snowpea", PlantType.SHOOTER, 150, 5, 300, PlantTag.DAY);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            shootIce(session);
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            shoot(session);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        // پرتابه‌های این گیاه باید پرچم isFrozenBullet = true داشته باشند تا Jester Zombie درست هندل کند
        System.out.println(getName() + " یک نخود برفی با دمیج " + damage + " و زمان کندی " + chillDuration + " ثانیه شلیک کرد.");
    }

    private void shootIce(GameSession session) {
        System.out.println(getName() + " انجماد کل لاین و شلیک رگباری یخی!");
    }

    @Override
    public void feed(GameSession session) {
        super.feed(session);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
        if (level >= 3) this.chillDuration += 2; // زمان انجماد +2 ثانیه
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }
}