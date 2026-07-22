package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class PepperPult extends Plant implements IShooter {
    private int damage = 50;
    private int warmthRadius = 1;
    private double actionInterval = 29.0;
    private double tickCounter = 0;
    private int currentSunCost = 200;
    private int currentCooldown = 5;
    private int level = 1;

    public PepperPult() {
        super("pepperpult", PlantType.LOBBER, 200, 5, 300, PlantTag.FIRE, PlantTag.AOE);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;
        if (isFeedActive()) {
            System.out.println("Plant Food: پرتاب فلفل‌های بزرگ به ۳ زامبی تصادفی!");
            decayFeedEffect();
            return;
        }
        tickCounter += 1.0;
        if (tickCounter >= actionInterval) {
            shoot(session);
            tickCounter -= actionInterval;
        }
    }

    @Override
    public void shoot(GameSession session) {
        System.out.println(getName() + " فلفل پرتاب کرد! (دمیج: " + damage + " | گرم‌کننده شعاع " + warmthRadius + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 15;
        if (level >= 3) this.warmthRadius += 1;
        if (level >= 4) this.currentSunCost -= 25;
    }
    @Override
    public int getSunCost() { return currentSunCost; }
    @Override
    public int getCooldownTicks() { return currentCooldown; }
}